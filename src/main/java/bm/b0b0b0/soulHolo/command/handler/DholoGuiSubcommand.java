package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class DholoGuiSubcommand extends AbstractPlayerSubcommand {

    @Override
    public Collection<String> aliases() {
        return List.of("gui", "menu");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        String name = context.args().length >= 2 ? context.arg(1) : null;
        HologramFailure failure = context.actions().openGui(player, name);
        if (failure != HologramFailure.NONE) {
            context.failures().send(player, failure, name, 0);
            return true;
        }
        if (name == null || name.isBlank()) {
            context.messages().send(player, "gui-opened");
        }
        return true;
    }

    @Override
    protected List<String> tabCompletePlayer(DholoCommandContext context, int completingArgIndex) {
        if (completingArgIndex == 1) {
            return filterPrefix(ownedHologramNames(context), context.arg(completingArgIndex));
        }
        return List.of();
    }
}
