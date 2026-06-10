package bm.b0b0b0.soulHolo.command;

import bm.b0b0b0.soulHolo.SoulHolo;
import bm.b0b0b0.soulHolo.config.PluginConfigHolder;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.service.HologramService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public final class DholoCommandContext {

    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final String[] args;
    private final PluginConfigHolder config;
    private final MessageService messages;
    private final HologramService hologramService;
    private final DholoPlayerActions actions;
    private final HologramFailureMessages failures;
    private final SoulHolo plugin;

    public DholoCommandContext(CommandSender sender,
                               Command command,
                               String label,
                               String[] args,
                               PluginConfigHolder config,
                               MessageService messages,
                               HologramService hologramService,
                               DholoPlayerActions actions,
                               HologramFailureMessages failures,
                               SoulHolo plugin) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
        this.config = config;
        this.messages = messages;
        this.hologramService = hologramService;
        this.actions = actions;
        this.failures = failures;
        this.plugin = plugin;
    }

    public CommandSender sender() {
        return sender;
    }

    public Command command() {
        return command;
    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public PluginConfigHolder config() {
        return config;
    }

    public MessageService messages() {
        return messages;
    }

    public HologramService hologramService() {
        return hologramService;
    }

    public DholoPlayerActions actions() {
        return actions;
    }

    public HologramFailureMessages failures() {
        return failures;
    }

    public SoulHolo plugin() {
        return plugin;
    }

    public String root() {
        return arg(0);
    }

    public String arg(int index) {
        if (index < 0 || index >= args.length) {
            return null;
        }
        return args[index];
    }

    public String rootLower() {
        String value = root();
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public Optional<Player> player() {
        return sender instanceof Player player ? Optional.of(player) : Optional.empty();
    }

    public boolean hasUsePermission() {
        return sender.hasPermission(config.pluginConfig().usePermission());
    }

    public boolean hasAdminPermission() {
        return sender.hasPermission(config.pluginConfig().adminPermission());
    }
}
