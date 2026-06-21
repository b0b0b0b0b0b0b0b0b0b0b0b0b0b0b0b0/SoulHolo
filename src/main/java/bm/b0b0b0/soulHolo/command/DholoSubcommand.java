package bm.b0b0b0.soulHolo.command;

import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public interface DholoSubcommand {

    Collection<String> aliases();

    boolean execute(DholoCommandContext context);

    List<String> tabComplete(DholoCommandContext context, int completingArgIndex);

    default List<String> filterPrefix(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return options;
        }
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lower))
                .toList();
    }

    default List<String> ownedHologramNames(DholoCommandContext context) {
        return context.player()
                .map(player -> context.hologramService().ownedHolograms(player).stream()
                        .map(h -> h.name())
                        .toList())
                .orElse(List.of());
    }

    default List<String> allHologramNames(DholoCommandContext context) {
        return context.hologramService().hologramNames();
    }

    default List<String> tabForSender(DholoCommandContext context, int relativeIndex, List<String> options) {
        if (relativeIndex != 0) {
            return List.of();
        }
        return filterPrefix(options, context.arg(context.args().length - 1));
    }

    default boolean denyUnlessAdmin(DholoCommandContext context) {
        if (context.hasAdminPermission()) {
            return false;
        }
        context.messages().send(context.sender(), "no-permission");
        return true;
    }

    default boolean denyUnlessPlayer(DholoCommandContext context) {
        if (context.player().isPresent()) {
            return false;
        }
        context.messages().send(context.sender(), "player-only");
        return true;
    }

    default boolean denyUnlessAccess(DholoCommandContext context) {
        if (context.hasAccess()) {
            return false;
        }
        context.messages().send(context.sender(), "hologram-limit-denied");
        return true;
    }

    default List<String> playerRootSuggestions(CommandSender sender) {
        return List.of("create", "add", "remove", "edit", "move", "line", "setting", "gui");
    }
}
