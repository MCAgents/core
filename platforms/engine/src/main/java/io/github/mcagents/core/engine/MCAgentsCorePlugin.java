package io.github.mcagents.core.engine;

import io.github.mcagents.core.bukkit.AbstractCorePlugin;

/**
 * The universal entry point: one jar that runs on SpigotMC, PaperMC, and Folia.
 *
 * <p>Which server is running is detected at enable time rather than by shipping
 * a different artifact per platform, so a server owner installs one file and
 * does not have to know which fork they run.</p>
 *
 * <p>Detection here is for the log line only. Unlike a feature plugin, this one
 * schedules nothing and touches no game state, so no behavior depends on the
 * answer — which is exactly why {@code folia-supported: true} is safe to
 * declare.</p>
 */
public final class MCAgentsCorePlugin extends AbstractCorePlugin {

    /**
     * The class Folia adds and Paper does not.
     *
     * <p>Detection is by class presence rather than by a server version string:
     * a version string is a formatted name that forks rewrite freely, while this
     * class exists exactly when the regionised schedulers do.</p>
     */
    private static final String FOLIA_MARKER = "io.papermc.paper.threadedregions.RegionizedServer";

    /**
     * The class Paper adds and Spigot does not.
     */
    private static final String PAPER_MARKER = "io.papermc.paper.configuration.Configuration";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String platformName() {
        if (isPresent(FOLIA_MARKER)) {
            return "Folia";
        }
        return isPresent(PAPER_MARKER) ? "PaperMC" : "SpigotMC";
    }

    /**
     * Reports whether a class is on the server's classpath.
     *
     * @param className The fully qualified class name to look for.
     * @return {@code true} when the class resolves.
     */
    private boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
