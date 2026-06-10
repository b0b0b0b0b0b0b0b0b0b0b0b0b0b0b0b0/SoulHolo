package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.CommandArgs;
import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.command.DholoPlayerActions;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DholoLineMoveSubcommand extends AbstractPlayerSubcommand {

    public DholoLineMoveSubcommand(boolean admin) {
        super(admin);
    }

    @Override
    public Collection<String> aliases() {
        return List.of("line");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        String holoName;
        String shiftRaw;
        String lineRaw;
        if (admin) {
            if (context.args().length < 5) {
                context.messages().send(context.sender(), "usage-admin-line");
                return true;
            }
            holoName = context.arg(2);
            shiftRaw = context.arg(3);
            lineRaw = context.arg(4);
        } else {
            if (context.args().length < 3) {
                context.messages().send(player, "usage-line");
                return true;
            }
            holoName = null;
            shiftRaw = context.arg(1);
            lineRaw = context.arg(2);
        }
        int direction = DholoPlayerActions.parseLineShiftDirection(shiftRaw);
        if (direction == 0) {
            context.messages().send(context.sender(), admin ? "usage-admin-line" : "usage-line");
            return true;
        }
        int line = CommandArgs.parseLine(lineRaw);
        HologramFailure failure = context.actions().moveLine(player, line, direction, holoName, admin);
        if (failure != HologramFailure.NONE) {
            context.failures().send(context.sender(), failure, holoName, line);
            return true;
        }
        context.messages().send(context.sender(), "line-moved", Map.of("line", String.valueOf(line)));
        return true;
    }

    @Override
    protected List<String> tabCompletePlayer(DholoCommandContext context, int completingArgIndex) {
        if (admin) {
            if (completingArgIndex == 2) {
                return filterPrefix(allHologramNames(context), context.arg(completingArgIndex));
            }
            if (completingArgIndex == 3) {
                return filterPrefix(List.of("up", "down"), context.arg(completingArgIndex));
            }
            return List.of();
        }
        if (completingArgIndex == 1) {
            return filterPrefix(List.of("up", "down"), context.arg(completingArgIndex));
        }
        return List.of();
    }
}
