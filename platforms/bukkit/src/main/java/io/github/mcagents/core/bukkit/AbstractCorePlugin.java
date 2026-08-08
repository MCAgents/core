package io.github.mcagents.core.bukkit;

import io.github.mcagents.core.api.llm.LlmCredentials;
import io.github.mcagents.core.api.llm.LlmVendor;
import io.github.mcagents.core.api.token.TokenState;
import io.github.mcagents.core.common.MCAgentsProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

/**
 * The shared enable and disable lifecycle for every Bukkit family entry point.
 *
 * <p>This plugin is a <strong>service</strong>, not a feature. It registers no
 * commands, no permissions, and no listeners, and a player will never see it do
 * anything. Its whole job is to stand up a {@link MCAgentsProvider} and keep it
 * alive so consumer plugins — MCAgents Chat and whatever comes after it — have
 * something to bind to.</p>
 *
 * <p>Consumers reach the provider through {@link MCAgentsProvider#instance},
 * resolved reflectively against this plugin's classloader. That is why this
 * class must install the provider during {@code onEnable} and must not clear it
 * afterwards: a consumer's {@code depend} declaration guarantees this plugin
 * enables first, and the instance has to be there when it looks.</p>
 *
 * <p>A concrete platform supplies nothing but its own name. The Bukkit family
 * differs in scheduling, and this plugin schedules nothing — so unlike a feature
 * plugin, there is genuinely no per-platform behavior to override.</p>
 */
public abstract class AbstractCorePlugin extends JavaPlugin {

    /**
     * Creates the plugin.
     *
     * <p>Nothing is initialised here. Bukkit constructs a plugin long before the
     * server is ready to be asked anything, so the provider is built in
     * {@link #onEnable()} instead.</p>
     */
    protected AbstractCorePlugin() {
        // Intentionally empty — see the class comment on the enable lifecycle.
    }

    /**
     * Names the platform this entry point serves, for the startup log line.
     *
     * @return A short platform name such as {@code "SpigotMC"}.
     */
    protected abstract String platformName();

    /**
     * The shortest request timeout worth configuring.
     *
     * <p>Below this even a fast model will not have answered, so a smaller
     * value would fail every request rather than protect anything.</p>
     */
    private static final int MIN_TIMEOUT_SECONDS = 5;

    /**
     * The longest request timeout worth configuring.
     *
     * <p>Ten minutes. Past that a player has walked away, and the request is
     * holding a connection for nobody.</p>
     */
    private static final int MAX_TIMEOUT_SECONDS = 600;

    /**
     * The credential file, held so a reload can re-read it.
     */
    private YamlTokenStore store;

    /**
     * How long one request may take before it is abandoned.
     *
     * <p>Defined here and nowhere else. Consumer plugins do not carry a timeout
     * setting, do not pass one, and cannot override this — a request either
     * comes back or fails within this window, which is what lets a consumer rely
     * on its future always completing without owning a timer of its own.</p>
     */
    private Duration requestTimeout = LlmCredentials.DEFAULT_TIMEOUT;

    /**
     * {@inheritDoc}
     *
     * <p>Builds the provider, reads this plugin's own {@code config.yml}, and
     * registers a credential store for every vendor configured there.</p>
     *
     * <p>Credentials live here, once, for the whole server. A consumer plugin
     * never sees a token, never has a token section in its own configuration,
     * and never decides whether one is dead — it asks the provider and gets an
     * answer. That is the point of putting them here: one file to secure, one
     * place to rotate, and one implementation of the rejected-versus-rate-limited
     * decision that would otherwise be repeated, differently, in every
     * consumer.</p>
     */
    @Override
    public void onEnable() {
        MCAgentsProvider provider = MCAgentsProvider.create();
        saveDefaultConfig();
        this.store = new YamlTokenStore(this);
        this.requestTimeout = readTimeout();

        int configured = 0;
        for (LlmVendor vendor : LlmVendor.values()) {
            if (registerVendor(provider, vendor) == TokenState.READY) {
                configured++;
            }
        }

        CoreCommand command = new CoreCommand(this);
        if (getCommand("agents") != null) {
            getCommand("agents").setExecutor(command);
            getCommand("agents").setTabCompleter(command);
        } else {
            getLogger().severe("The 'agents' command is missing from plugin.yml, so /agents will not work.");
        }

        getLogger().info("MCAgents core ready on " + platformName()
                + ". Requests time out after " + requestTimeout.toSeconds() + "s.");
        if (configured == 0) {
            getLogger().warning("No API tokens are configured. Add one to "
                    + store.describe() + ", then run /agents reload.");
        } else {
            getLogger().info(configured + " platform(s) have a usable token.");
        }
    }

    /**
     * Re-reads {@code config.yml} and reinstalls every vendor's credentials.
     *
     * <p>What backs the reload command: a token pasted into the file becomes
     * usable without restarting the server.</p>
     *
     * @return How many platforms have a usable token afterwards.
     */
    public int reloadCredentials() {
        MCAgentsProvider provider = MCAgentsProvider.instance;
        if (provider == null) {
            return 0;
        }

        reloadConfig();
        store.reload();
        this.requestTimeout = readTimeout();

        // Re-register rather than reloadTokens(): the timeout may have changed,
        // and it travels with the credential template rather than the pool.
        int ready = 0;
        for (LlmVendor vendor : LlmVendor.values()) {
            if (registerVendor(provider, vendor) == TokenState.READY) {
                ready++;
            }
        }
        return ready;
    }

    /**
     * Returns how long one request may take before it is abandoned.
     *
     * @return The configured request timeout.
     */
    public Duration requestTimeout() {
        return requestTimeout;
    }

    /**
     * Registers one vendor's store together with the configured timeout.
     *
     * @param provider The provider to register with.
     * @param vendor The vendor to register.
     * @return The credential state afterwards.
     */
    private TokenState registerVendor(MCAgentsProvider provider, LlmVendor vendor) {
        // The template carries everything except the key: the endpoint and the
        // timeout. The pool swaps only the key, so the timeout survives every
        // rotation.
        LlmCredentials template = LlmCredentials
                .of(vendor, "placeholder")
                .withTimeout(requestTimeout);
        return provider.registerStore(vendor, store, template);
    }

    /**
     * Reads the request timeout from configuration, clamping an unusable value
     * rather than refusing to start.
     *
     * @return The timeout to apply.
     */
    private Duration readTimeout() {
        int seconds = getConfig().getInt("request_timeout_seconds",
                (int) LlmCredentials.DEFAULT_TIMEOUT.toSeconds());

        if (seconds < MIN_TIMEOUT_SECONDS || seconds > MAX_TIMEOUT_SECONDS) {
            int fallback = (int) LlmCredentials.DEFAULT_TIMEOUT.toSeconds();
            getLogger().warning("config.yml sets request_timeout_seconds: " + seconds
                    + ", which is outside " + MIN_TIMEOUT_SECONDS + "-" + MAX_TIMEOUT_SECONDS
                    + ". Using " + fallback + ".");
            return Duration.ofSeconds(fallback);
        }
        return Duration.ofSeconds(seconds);
    }

    /**
     * Stores a credential and puts it into use immediately.
     *
     * @param vendor The platform the credential belongs to.
     * @param token The credential to store.
     * @return {@code true} when it was stored and the pool reloaded.
     */
    public boolean addToken(LlmVendor vendor, String token) {
        if (store == null || !store.add(vendor.code(), token)) {
            return false;
        }
        reloadCredentials();
        return true;
    }

    /**
     * Removes a stored credential and stops using it immediately.
     *
     * @param vendor The platform the credential belongs to.
     * @param token The credential to remove.
     */
    public void removeToken(LlmVendor vendor, String token) {
        if (store == null) {
            return;
        }
        store.evict(vendor.code(), token);
        reloadCredentials();
    }

    /**
     * Returns a vendor's stored credentials, in the order they are tried.
     *
     * <p>For the command's masked handles only. The caller must not print these
     * values — see {@link io.github.mcagents.core.common.TokenHandles}.</p>
     *
     * @param vendor The platform to read.
     * @return The stored credentials, never {@code null}.
     */
    public java.util.List<String> tokens(LlmVendor vendor) {
        return store == null ? java.util.List.of() : store.load(vendor.code());
    }

    /**
     * Returns the credential state for a vendor, for the status command.
     *
     * @param vendor The vendor to report on.
     * @return Its credential state.
     */
    public TokenState credentialState(LlmVendor vendor) {
        MCAgentsProvider provider = MCAgentsProvider.instance;
        return provider == null ? TokenState.NOT_SET : provider.tokenState(vendor);
    }

    /**
     * Describes where credentials are stored, for the status command.
     *
     * @return The configuration file path, never a credential.
     */
    public String storeDescription() {
        return store == null ? "not initialised" : store.describe();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the provider, which shuts down every vendor client it holds.
     * {@link MCAgentsProvider#instance} is deliberately left pointing at the
     * closed provider: a consumer that outlives this plugin's disable then gets
     * a clear "this provider has been closed" failure rather than a
     * {@code NullPointerException} from a field that vanished underneath it.</p>
     */
    @Override
    public void onDisable() {
        MCAgentsProvider provider = MCAgentsProvider.instance;
        if (provider != null) {
            provider.close();
        }
        getLogger().info("MCAgents core stopped.");
    }
}
