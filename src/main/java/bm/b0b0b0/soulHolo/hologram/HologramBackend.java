package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface HologramBackend {

    String id();

    boolean available();

    Set<DisplaySettingKey> supportedSettings();

    void spawn(PrivateHologram hologram, List<String> renderedLines);

    void update(PrivateHologram hologram, List<String> renderedLines);

    void remove(PrivateHologram hologram);

    default void relocate(PrivateHologram hologram, List<String> renderedLines) {
        remove(hologram);
        spawn(hologram, renderedLines);
    }

    String formatLine(Player viewer, String line);

    default boolean supports(DisplaySettingKey key) {
        return supportedSettings().contains(key);
    }
}
