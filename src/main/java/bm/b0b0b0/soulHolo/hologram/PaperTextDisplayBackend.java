package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.message.HologramLineParser;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PaperTextDisplayBackend implements HologramBackend {

    private static final NamespacedKey HOLOGRAM_ID_KEY = new NamespacedKey("soulholo", "hologram_id");

    private static final Set<DisplaySettingKey> SUPPORTED = EnumSet.allOf(DisplaySettingKey.class);

    private final Map<UUID, UUID> displayByHologram = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "paper";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public Set<DisplaySettingKey> supportedSettings() {
        return SUPPORTED;
    }

    @Override
    public void spawn(PrivateHologram hologram, List<String> renderedLines) {
        remove(hologram);
        if (!hologram.displaySettings().enabled()) {
            return;
        }
        World world = hologram.location() == null ? null : hologram.location().getWorld();
        if (world == null) {
            return;
        }
        List<String> lines = renderedLines == null ? List.of() : new ArrayList<>(renderedLines);
        if (lines.isEmpty()) {
            lines.add(" ");
        }
        Component text = joinLines(lines);
        TextDisplay display = world.spawn(hologram.location(), TextDisplay.class, entity -> {
            entity.text(text);
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.getPersistentDataContainer().set(HOLOGRAM_ID_KEY, PersistentDataType.STRING, hologram.id().toString());
            PaperDisplayApplier.apply(entity, hologram);
        });
        displayByHologram.put(hologram.id(), display.getUniqueId());
        hologram.setBackendId(display.getUniqueId().toString());
    }

    @Override
    public void update(PrivateHologram hologram, List<String> renderedLines) {
        if (!hologram.displaySettings().enabled()) {
            remove(hologram);
            return;
        }
        TextDisplay display = findDisplay(hologram);
        if (display == null || !display.isValid()) {
            spawn(hologram, renderedLines);
            return;
        }
        List<String> lines = renderedLines == null ? List.of() : new ArrayList<>(renderedLines);
        if (lines.isEmpty()) {
            lines.add(" ");
        }
        display.text(joinLines(lines));
        PaperDisplayApplier.apply(display, hologram);
    }

    @Override
    public void relocate(PrivateHologram hologram, List<String> renderedLines) {
        if (!hologram.displaySettings().enabled()) {
            remove(hologram);
            return;
        }
        TextDisplay display = findDisplay(hologram);
        Location target = hologram.location();
        if (display == null || !display.isValid() || target == null) {
            spawn(hologram, renderedLines);
            return;
        }
        display.teleport(target);
        List<String> lines = renderedLines == null ? List.of() : new ArrayList<>(renderedLines);
        if (lines.isEmpty()) {
            lines.add(" ");
        }
        display.text(joinLines(lines));
        PaperDisplayApplier.apply(display, hologram);
    }

    @Override
    public void remove(PrivateHologram hologram) {
        UUID tracked = displayByHologram.remove(hologram.id());
        if (tracked != null) {
            removeEntity(tracked);
        }
        if (hologram.backendId() != null && !hologram.backendId().isBlank()) {
            try {
                removeEntity(UUID.fromString(hologram.backendId()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public String formatLine(Player viewer, String line) {
        return line == null ? "" : line;
    }

    private TextDisplay findDisplay(PrivateHologram hologram) {
        UUID entityId = displayByHologram.get(hologram.id());
        if (entityId == null && hologram.backendId() != null && !hologram.backendId().isBlank()) {
            try {
                entityId = UUID.fromString(hologram.backendId());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        if (entityId == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(entityId);
        if (entity instanceof TextDisplay textDisplay) {
            displayByHologram.put(hologram.id(), entityId);
            return textDisplay;
        }
        return null;
    }

    private static void removeEntity(UUID entityId) {
        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null) {
            entity.remove();
        }
    }

    private static Component joinLines(List<String> lines) {
        Component combined = Component.empty();
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                combined = combined.append(Component.newline());
            }
            combined = combined.append(HologramLineParser.parse(lines.get(index)));
        }
        return combined;
    }
}
