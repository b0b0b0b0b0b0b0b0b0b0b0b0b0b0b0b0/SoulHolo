package bm.b0b0b0.soulHolo.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class DholoCommand implements CommandExecutor, TabCompleter {

    private final DholoCommandRegistry registry;

    public DholoCommand(DholoCommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DholoCommandContext context = registry.context(sender, command, label, args);
        if (args.length == 0) {
            context.messages().send(sender, "unknown-subcommand");
            return true;
        }
        return registry.find(args[0])
                .map(subcommand -> subcommand.execute(context))
                .orElseGet(() -> {
                    context.messages().send(sender, "unknown-subcommand");
                    return true;
                });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return registry.tabComplete(sender, args);
    }
}
