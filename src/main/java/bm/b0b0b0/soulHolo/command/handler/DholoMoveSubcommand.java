package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.CommandArgs;
import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.command.DholoPlayerActions;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DholoMoveSubcommand extends AbstractPlayerSubcommand {

    public DholoMoveSubcommand(boolean admin) {
        super(admin);
    }

    @Override
    public Collection<String> aliases() {
        return List.of("move");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        String holoName;
        String directionRaw;
        if (admin) {
            if (context.args().length < 4) {
                context.messages().send(context.sender(), "usage-admin-move");
                return true;
            }
            holoName = context.arg(2);
            directionRaw = context.arg(3);
        } else {
            if (context.args().length < 2) {
                context.messages().send(player, "usage-move");
                return true;
            }
            holoName = null;
            directionRaw = context.arg(1);
        }
        RelativeMoveDirection direction = DholoPlayerActions.parseMoveDirection(directionRaw);
        if (direction == null) {
            context.messages().send(context.sender(), admin ? "usage-admin-move" : "usage-move");
            return true;
        }
        HologramFailure failure = context.actions().movePosition(player, direction, holoName, admin);
        if (failure != HologramFailure.NONE) {
            context.failures().send(context.sender(), failure, holoName, 0);
            return true;
        }
        context.messages().send(context.sender(), "position-moved", Map.of(
                "direction", direction.name().toLowerCase(Locale.ROOT)
        ));
        return true;
    }

    @Override
    protected List<String> tabCompletePlayer(DholoCommandContext context, int completingArgIndex) {
        if (admin) {
            if (completingArgIndex == 2) {
                return filterPrefix(allHologramNames(context), context.arg(completingArgIndex));
            }
            if (completingArgIndex == 3) {
                return filterPrefix(List.of("up", "down", "left", "right"), context.arg(completingArgIndex));
            }
            return List.of();
        }
        if (completingArgIndex == 1) {
            return filterPrefix(List.of("up", "down", "left", "right"), context.arg(completingArgIndex));
        }
        return List.of();
    }
}
