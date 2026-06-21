package bm.b0b0b0.soulHolo.command.handler;

import bm.b0b0b0.soulHolo.command.DholoCommandContext;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DholoCreateSubcommand extends AbstractPlayerSubcommand {

    public DholoCreateSubcommand(boolean admin) {
        super(admin);
    }

    @Override
    public Collection<String> aliases() {
        return List.of("create");
    }

    @Override
    protected boolean run(DholoCommandContext context, Player player) {
        if (admin) {
            return runAdmin(context);
        }
        if (context.args().length < 2) {
            context.messages().send(player, "usage-create");
            return true;
        }
        if (!context.hasAdminPermission() && !context.hologramService().hasHologramSlot(player)) {
            context.failures().send(player, HologramFailure.HOLOGRAM_LIMIT_DENIED, context.arg(1), 0);
            return true;
        }
        return finishCreate(context, player, player, context.arg(1), context.hasAdminPermission());
    }

    private boolean runAdmin(DholoCommandContext context) {
        if (context.args().length < 3) {
            context.messages().send(context.sender(), "usage-admin-create");
            return true;
        }
        org.bukkit.entity.Player owner;
        if (context.args().length >= 4) {
            owner = org.bukkit.Bukkit.getPlayerExact(context.arg(3));
            if (owner == null) {
                context.messages().send(context.sender(), "player-not-found", Map.of("player", context.arg(3)));
                return true;
            }
        } else {
            if (context.player().isEmpty()) {
                context.messages().send(context.sender(), "player-only");
                return true;
            }
            owner = context.player().get();
        }
        org.bukkit.entity.Player actor = context.player().orElse(owner);
        return finishCreate(context, actor, owner, context.arg(2), true);
    }

    private boolean finishCreate(DholoCommandContext context,
                               org.bukkit.entity.Player actor,
                               org.bukkit.entity.Player owner,
                               String rawName,
                               boolean bypassLimits) {
        HologramFailure failure = context.hologramService().create(actor, rawName, owner, bypassLimits);
        if (failure != HologramFailure.NONE) {
            context.failures().send(context.sender(), failure, rawName, 0);
            return true;
        }
        if (admin) {
            context.messages().send(context.sender(), "admin-created", Map.of(
                    "name", rawName.toLowerCase(Locale.ROOT),
                    "owner", owner.getName()
            ));
        } else {
            context.messages().send(context.sender(), "created", Map.of(
                    "name", rawName.toLowerCase(Locale.ROOT),
                    "region", context.hologramService().regionForFailure(owner)
            ));
        }
        return true;
    }
}
