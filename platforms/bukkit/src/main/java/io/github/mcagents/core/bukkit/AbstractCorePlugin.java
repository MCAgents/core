package io.github.mcagents.core.bukkit;

import io.github.mcagents.core.api.llm.LlmVendor;
import io.github.mcagents.core.api.token.TokenState;
import io.github.mcagents.core.common.MCAgentsProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
     * The credential file, held so a reload can re-read it.
     */
    private YamlTokenStore store;

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
        this.store = new YamlTokenStore(this);

        int configured = 0;
        for (LlmVendor vendor : LlmVendor.values()) {
            TokenState state = provider.registerStore(vendor, store);
            if (state == TokenState.READY) {
                configured++;
            }
        }

        CoreCommand command = new CoreCommand(this);
        if (getCommand("mcagents") != null) {
            getCommand("mcagents").setExecutor(command);
            getCommand("mcagents").setTabCompleter(command);
        } else {
            getLogger().severe("The 'mcagents' command is missing from plugin.yml, so /mcagents will not work.");
        }

        getLogger().info("MCAgents core ready on " + platformName() + ".");
        if (configured == 0) {
            getLogger().warning("No API tokens are configured. Add one to "
                    + store.describe() + ", then run /mcagents reload.");
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

        store.reload();
        provider.reloadTokens();

        int ready = 0;
        for (LlmVendor vendor : LlmVendor.values()) {
            if (provider.tokenState(vendor) == TokenState.READY) {
                ready++;
            }
        }
        return ready;
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
