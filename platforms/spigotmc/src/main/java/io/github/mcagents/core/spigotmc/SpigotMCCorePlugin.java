package io.github.mcagents.core.spigotmc;

import io.github.mcagents.core.bukkit.AbstractCorePlugin;

/**
 * The SpigotMC entry point.
 *
 * <p>Everything this plugin does lives in {@link AbstractCorePlugin}. This class
 * exists so the SpigotMC build has a {@code main} class to name in its
 * manifest.</p>
 */
public final class SpigotMCCorePlugin extends AbstractCorePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String platformName() {
        return "SpigotMC";
    }
}
