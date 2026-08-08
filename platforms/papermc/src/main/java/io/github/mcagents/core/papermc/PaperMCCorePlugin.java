package io.github.mcagents.core.papermc;

import io.github.mcagents.core.bukkit.AbstractCorePlugin;

/**
 * The PaperMC entry point.
 *
 * <p>Everything this plugin does lives in {@link AbstractCorePlugin}. This class
 * exists so the PaperMC build has a {@code main} class to name in its
 * manifest.</p>
 */
public final class PaperMCCorePlugin extends AbstractCorePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String platformName() {
        return "PaperMC";
    }
}
