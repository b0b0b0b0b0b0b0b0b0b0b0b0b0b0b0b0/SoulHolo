package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.CommandArgs;
import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DholoRemoveSubcommand extends AbstractPlayerSubcommand {

    public DholoRemoveSubcommand(boolean admin) {
        super(admin);
    }

    @Override
    public Collection<String> aliases() {
        return List.of("remove");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        String holoName = admin ? context.arg(2) : null;
        if (admin && context.args().length < 4) {
            context.messages().send(context.sender(), "usage-admin-remove");
            return true;
        }
        if (!admin && context.args().length < 2) {
            context.messages().send(player, "usage-remove");
            return true;
        }
        int line = CommandArgs.parseLine(admin ? context.arg(3) : context.arg(1));
        HologramFailure failure = context.actions().removeLine(player, line, admin, holoName);
        if (failure != HologramFailure.NONE) {
            context.failures().send(context.sender(), failure, holoName, line);
            return true;
        }
        context.messages().send(context.sender(), "line-removed", Map.of("line", String.valueOf(line)));
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
