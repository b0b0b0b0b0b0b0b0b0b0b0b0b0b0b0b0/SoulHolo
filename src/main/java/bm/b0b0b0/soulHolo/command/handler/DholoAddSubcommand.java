package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.CommandArgs;
import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DholoAddSubcommand extends AbstractPlayerSubcommand {

    public DholoAddSubcommand(boolean admin) {
        super(admin);
    }

    @Override
    public Collection<String> aliases() {
        return List.of("add");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        String holoName = admin ? context.arg(2) : null;
        if (admin && context.args().length < 3) {
            context.messages().send(context.sender(), "usage-admin-add");
            return true;
        }
        if (!admin && context.args().length < 1) {
            context.messages().send(player, "usage-add");
            return true;
        }
        int textFrom = admin ? 3 : 1;
        String text = context.args().length > textFrom ? CommandArgs.join(context.args(), textFrom) : "";
        HologramFailure failure = context.actions().addLine(player, text, admin, holoName);
        if (failure != HologramFailure.NONE) {
            context.failures().send(context.sender(), failure, holoName, 0);
            return true;
        }
        String line = admin ? "?" : String.valueOf(context.hologramService().activeLineCount(player));
        context.messages().send(context.sender(), "line-added", Map.of("line", line));
        return true;
    }

    @Override
    protected List<String> tabCompletePlayer(DholoCommandContext context, int completingArgIndex) {
        if (admin && completingArgIndex == 2) {
            return filterPrefix(allHologramNames(context), context.arg(completingArgIndex));
        }
        return List.of();
    }
}
