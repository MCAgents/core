package io.github.mcagents.core.bukkit;

import io.github.mcagents.core.api.llm.LlmVendor;
import io.github.mcagents.core.api.token.TokenState;
import io.github.mcagents.core.common.TokenHandles;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code /agents} command: see credential health, add and remove tokens, and
 * reload the file.
 *
 * <pre>
 * /agents
 * /agents reload
 * /agents &lt;platform&gt; token add &lt;token&gt;
 * /agents &lt;platform&gt; token remove &lt;handle or token&gt;
 * </pre>
 *
 * <p>This plugin is a service, so its command exists for the server owner rather
 * than for a player — there is nothing here to do with a world, an entity, or an
 * inventory.</p>
 *
 * <h2>Tab completion never reveals a token</h2>
 *
 * <p>Completing {@code remove} on the stored values would push live API keys
 * into every client's suggestion list, where a stream, a screenshot, or a shared
 * screen leaks them. It completes on {@link TokenHandles} instead — {@code
 * #2:a3f9}, a position and the last four characters — which is enough to pick
 * the right key and far too little to reconstruct one. The full value is still
 * accepted as an argument, for someone revoking a key they have in their
 * clipboard.</p>
 */
public final class CoreCommand implements CommandExecutor, TabCompleter {

    /**
     * The permission every subcommand requires.
     *
     * <p>A single permission rather than one per subcommand: all of them are
     * administrative, and splitting them would imply a role that can inspect
     * credential health but not change it, which is not a role anybody has.</p>
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

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            return status(sender);
        }
        if (args[0].equalsIgnoreCase("reload")) {
            return reload(sender);
        }

        // Everything else is /agents <platform> token <add|remove> <value>
        Optional<LlmVendor> vendor = vendor(args[0]);
        if (vendor.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown platform: " + args[0]);
            return usage(sender, label);
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("token")) {
            return usage(sender, label);
        }

        return switch (args[2].toLowerCase(Locale.ROOT)) {
            case "add" -> add(sender, vendor.get(), args[3]);
            case "remove" -> remove(sender, vendor.get(), args[3]);
            default -> usage(sender, label);
        };
    }

    /**
     * Stores a credential and puts it into use.
     *
     * @param sender Who ran the command.
     * @param vendor The platform the credential belongs to.
     * @param token The credential.
     * @return Always {@code true}; the handler has reported its own outcome.
     */
    private boolean add(CommandSender sender, LlmVendor vendor, String token) {
        if (!plugin.addToken(vendor, token)) {
            sender.sendMessage(ChatColor.YELLOW + "That token is already stored for "
                    + vendor.code() + ", or the file could not be written.");
            return true;
        }

        // Deliberately reports the handle, not the token. A command echoing a
        // secret back is how it ends up in a screenshot.
        List<String> tokens = plugin.tokens(vendor);
        sender.sendMessage(ChatColor.GREEN + "Added " + TokenHandles.of(tokens.size() - 1, token.trim())
                + " to " + vendor.code() + ". " + tokens.size() + " token(s) stored.");
        warnIfEchoed(sender);
        return true;
    }

    /**
     * Removes a credential named by handle or by value.
     *
     * @param sender Who ran the command.
     * @param vendor The platform the credential belongs to.
     * @param typed The handle or the credential itself.
     * @return Always {@code true}.
     */
    private boolean remove(CommandSender sender, LlmVendor vendor, String typed) {
        List<String> tokens = plugin.tokens(vendor);
        Optional<String> target = TokenHandles.resolve(tokens, typed);

        if (target.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No stored token matches " + typed + " for " + vendor.code()
                    + ". Run /agents " + vendor.code() + " token remove and use tab completion.");
            return true;
        }

        plugin.removeToken(vendor, target.get());
        sender.sendMessage(ChatColor.GREEN + "Removed a token from " + vendor.code() + ". "
                + plugin.tokens(vendor).size() + " token(s) remain.");
        return true;
    }

    /**
     * Re-reads the credential file and reports the result.
     *
     * @param sender Who ran the command.
     * @return Always {@code true}.
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
     * Reports every platform's credential state and handles.
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
            List<String> handles = TokenHandles.all(plugin.tokens(vendor));
            String detail = handles.isEmpty() ? "" : ChatColor.DARK_GRAY + "  " + String.join(" ", handles);
            sender.sendMessage(ChatColor.GRAY + "  " + vendor.code() + ": "
                    + describe(plugin.credentialState(vendor)) + detail);
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
     * Shows the command forms.
     *
     * @param sender Who ran the command.
     * @param label How the command was invoked.
     * @return Always {@code true}.
     */
    private boolean usage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GRAY + "/" + label + ChatColor.DARK_GRAY
                + " — credential status for every platform");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " reload" + ChatColor.DARK_GRAY
                + " — re-read the credential file");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " <platform> token add <token>");
        sender.sendMessage(ChatColor.GRAY + "/" + label + " <platform> token remove <handle>"
                + ChatColor.DARK_GRAY + " — tab complete the handle");
        return true;
    }

    /**
     * Reminds a player that what they just typed is in their chat history.
     *
     * <p>Only for a player: a console has no scrollback anyone else can read,
     * and no client-side command history to clear.</p>
     *
     * @param sender Who ran the command.
     */
    private void warnIfEchoed(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            sender.sendMessage(ChatColor.YELLOW + "That token is now in your client's command history. "
                    + "Adding tokens from the server console avoids that.");
        }
    }

    /**
     * Resolves a platform name.
     *
     * @param name What was typed.
     * @return The vendor, or empty when the name matches none.
     */
    private Optional<LlmVendor> vendor(String name) {
        try {
            return Optional.of(LlmVendor.fromCode(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Completes platform names, then {@code token}, then {@code add} or
     * {@code remove}, and finally — for {@code remove} only — the masked handles
     * of the stored credentials. {@code add} completes nothing: the argument is
     * a secret the administrator supplies.</p>
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION) && !sender.isOp()) {
            return List.of();
        }

        return switch (args.length) {
            case 1 -> {
                List<String> options = new ArrayList<>(List.of("status", "reload"));
                for (LlmVendor vendor : LlmVendor.values()) {
                    options.add(vendor.code());
                }
                yield matching(options, args[0]);
            }
            case 2 -> vendor(args[0]).isPresent() ? matching(List.of("token"), args[1]) : List.of();
            case 3 -> vendor(args[0]).isPresent() && args[1].equalsIgnoreCase("token")
                    ? matching(List.of("add", "remove"), args[2])
                    : List.of();
            case 4 -> {
                if (!args[1].equalsIgnoreCase("token") || !args[2].equalsIgnoreCase("remove")) {
                    // Never suggest anything for add: its argument is a secret.
                    yield List.of();
                }
                yield vendor(args[0])
                        .map(vendor -> matching(TokenHandles.all(plugin.tokens(vendor)), args[3]))
                        .orElse(List.of());
            }
            default -> List.of();
        };
    }

    /**
     * Filters options by what has been typed so far.
     *
     * @param options Everything that could be suggested.
     * @param prefix What the sender has typed.
     * @return The options that start with the prefix, ignoring case.
     */
    private List<String> matching(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
