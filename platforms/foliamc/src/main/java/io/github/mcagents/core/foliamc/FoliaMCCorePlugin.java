package io.github.mcagents.core.foliamc;

import io.github.mcagents.core.bukkit.AbstractCorePlugin;

/**
 * The Folia entry point.
 *
 * <p>Everything this plugin does lives in {@link AbstractCorePlugin}. This class
 * exists so the Folia build has a {@code main} class to name in its
 * manifest.</p>
 */
public final class FoliaMCCorePlugin extends AbstractCorePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String platformName() {
        return "Folia";
    }
}
