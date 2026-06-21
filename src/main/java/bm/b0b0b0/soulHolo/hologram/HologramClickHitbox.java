package bm.b0b0b0.soulHolo.hologram;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

final class HologramClickHitbox {

    private HologramClickHitbox() {
    }

    static Interaction spawn(World world,
                             Location anchor,
                             UUID hologramId,
                             float width,
                             float height) {
        Location location = anchor.clone().add(0.0, height * 0.35, 0.0);
        return world.spawn(location, Interaction.class, entity -> {
            entity.setInteractionWidth(width);
            entity.setInteractionHeight(height);
            entity.setResponsive(true);
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setGravity(false);
            entity.getPersistentDataContainer().set(
                    HologramEntityKeys.HOLOGRAM_ID,
                    PersistentDataType.STRING,
                    hologramId.toString()
            );
        });
    }

    static void sync(Interaction interaction, Location anchor, float width, float height) {
        if (interaction == null || !interaction.isValid()) {
            return;
        }
        interaction.teleport(anchor.clone().add(0.0, height * 0.35, 0.0));
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
    }

    static float resolveWidth(float configuredWidth, float scale) {
        return Math.max(1.0f, configuredWidth * Math.max(0.5f, scale));
    }

    static float resolveHeight(float baseHeight, float heightPerLine, int lineCount, float scale) {
        int lines = Math.max(1, lineCount);
        float scaled = (baseHeight + (lines - 1) * heightPerLine) * Math.max(0.5f, scale);
        return Math.max(0.8f, scaled);
    }
}
