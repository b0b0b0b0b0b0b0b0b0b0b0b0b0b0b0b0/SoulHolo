package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public final class HologramPositionGuiService {

    private final HologramService hologramService;
    private final MessageService messages;
    private final DisplaySettingAccess access;

    public HologramPositionGuiService(HologramService hologramService,
                                      MessageService messages,
                                      DisplaySettingAccess access) {
        this.hologramService = hologramService;
        this.messages = messages;
        this.access = access;
    }

    public boolean canMove(Player player) {
        return access.canMovePosition(player);
    }

    public boolean move(Player player, PrivateHologram hologram, float yaw, RelativeMoveDirection direction) {
        if (!canMove(player)) {
            messages.send(player, "gui-position-no-permission");
            return false;
        }
        HologramFailure failure = hologramService.shiftPosition(
                player,
                hologram,
                yaw,
                direction,
                access.isAdmin(player)
        );
        if (failure == HologramFailure.NONE) {
            return true;
        }
        sendFailure(player, failure, hologram);
        return false;
    }

    public double step() {
        return hologramService.positionStep();
    }

    public String formatCoordinate(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void sendFailure(Player player, HologramFailure failure, PrivateHologram hologram) {
        if (failure == HologramFailure.OUTSIDE_REGION) {
            messages.send(player, "position-outside-region", Map.of("region", hologram.regionId()));
            return;
        }
        if (failure == HologramFailure.NOT_OWNED) {
            messages.send(player, "hologram-not-owned");
            return;
        }
        messages.send(player, "unknown-subcommand");
    }
}
