/**
 * Shared Bukkit platform code.
 *
 * <p>Everything the SpigotMC, PaperMC, and Folia entry points have in common
 * lives here, compiled against the Spigot API — the lowest platform of the
 * three, so this code runs on all of them. Code that needs a Paper only or
 * Folia only type belongs in that platform's own module instead.</p>
 *
 * <p>Folia has no single main thread, so nothing in this package may assume
 * one or reach for the legacy {@code BukkitScheduler}.</p>
 */
package io.github.mcagents.core.bukkit;
