package io.github.mcagents.core.bukkit;

import io.github.mcagents.core.api.llm.LlmVendor;
import io.github.mcagents.core.api.token.TokenState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The {@code /mcagents} command: reload credentials, and see which platforms
 * have a usable one.
 *
 * <p>Two subcommands and nothing else. This plugin is a service, so its command
 * exists for the server owner's benefit rather than a player's — there is
 * nothing here to do with a world, an entity, or an inventory.</p>
 *
 * <p>Both subcommands are administrative: one rewrites which credentials are
 * live, and the other reports on secrets the owner configured. Neither ever
 * prints a token.</p>
 */
public final class CoreCommand implements CommandExecutor, TabCompleter {

    /**
     * The permission both subcommands require.
     *
     * <p>A single permission rather than one each: both are administrative, and
     * splitting them would imply a role that can inspect credential health but
     * not refresh it, which is not a role anybody has.</p>
     */
    public static final String ADMIN_PERMISSION = "mcagents.admin";

    /**
     * The plugin whose credentials this command manages.
     */
    private final AbstractCorePlugin plugin;

    /**
     * Creates the command handler.
     *
     * @param plugin The owning plugin.
     * @throws NullPointerException When {@code plugin} is {@code null}.
     */
    public CoreCommand(AbstractCorePlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION) && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage MCAgents.");
            return true;
        }

        String sub = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "reload" -> reload(sender);
            case "status" -> status(sender);
            default -> {
                sender.sendMessage(ChatColor.GRAY + "/" + label + " status"
                        + ChatColor.DARK_GRAY + " — show which platforms have a usable token");
                sender.sendMessage(ChatColor.GRAY + "/" + label + " reload"
                        + ChatColor.DARK_GRAY + " — re-read config.yml");
                yield true;
            }
        };
    }

    /**
     * Re-reads the credential file and reports the result.
     *
     * @param sender Who ran the command.
     * @return Always {@code true}; the handler has reported its own outcome.
     */
    private boolean reload(CommandSender sender) {
        try {
            int ready = plugin.reloadCredentials();
            sender.sendMessage(ChatColor.GREEN + "MCAgents credentials reloaded.");
            sender.sendMessage(ChatColor.GRAY + "  " + ready + " platform(s) have a usable token.");
        } catch (RuntimeException e) {
            // A reload that throws must not take the plugin down with it — the
            // credentials already in memory are still live and still working.
            sender.sendMessage(ChatColor.RED + "Reload failed: " + e.getMessage());
            plugin.getLogger().warning("Reloading MCAgents credentials failed: " + e.getMessage());
        }
        return true;
    }

    /**
     * Reports the credential state of every platform.
     *
     * @param sender Who ran the command.
     * @return Always {@code true}.
     */
    private boolean status(CommandSender sender) {
        // getDescription() rather than getPluginMeta(): the latter is Paper only,
        // and this class is compiled against the Spigot API so it runs on all
        // three server platforms.
        sender.sendMessage(ChatColor.AQUA + "MCAgents " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "  credentials: " + ChatColor.WHITE + plugin.storeDescription());

        for (LlmVendor vendor : LlmVendor.values()) {
            TokenState state = plugin.credentialState(vendor);
            sender.sendMessage(ChatColor.GRAY + "  " + vendor.code() + ": " + describe(state));
        }
        return true;
    }

    /**
     * Renders a credential state as a coloured phrase.
     *
     * <p>Deliberately different wording for the two "no usable token" states:
     * one asks the owner to add a key, the other tells them their keys stopped
     * working, and conflating them wastes their time.</p>
     *
     * @param state The state to describe.
     * @return The phrase to show.
     */
    private String describe(TokenState state) {
        return switch (state) {
            case READY -> ChatColor.GREEN + "ready";
            case NOT_SET -> ChatColor.GRAY + "not configured";
            case EXPIRED -> ChatColor.RED + "expired — every token was rejected and removed";
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || (!sender.hasPermission(ADMIN_PERMISSION) && !sender.isOp())) {
            return List.of();
        }

        List<String> options = new ArrayList<>();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        for (String option : List.of("status", "reload")) {
            if (option.startsWith(prefix)) {
                options.add(option);
            }
        }
        return options;
    }
}
