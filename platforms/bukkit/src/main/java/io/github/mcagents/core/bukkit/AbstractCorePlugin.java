package io.github.mcagents.core.bukkit;

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
     * {@inheritDoc}
     *
     * <p>Builds the provider and installs it as
     * {@link MCAgentsProvider#instance}. No vendor is registered and no
     * connection is opened — credentials belong to the consumer plugin that has
     * a configuration file, not to this one.</p>
     */
    @Override
    public void onEnable() {
        MCAgentsProvider.create();

        getLogger().info("MCAgents core ready on " + platformName() + ".");
        getLogger().info("This plugin provides the agent API. It has no commands and no configuration; "
                + "install a consumer plugin such as MCAgentsChat to use it.");
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
