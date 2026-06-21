package bm.b0b0b0.soulHolo.hologram;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.UUID;

public final class HologramEntityKeys {

    public static final NamespacedKey HOLOGRAM_ID = new NamespacedKey("soulholo", "hologram_id");

    private HologramEntityKeys() {
    }

    public static Optional<UUID> read(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        String raw = entity.getPersistentDataContainer().get(HOLOGRAM_ID, PersistentDataType.STRING);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
