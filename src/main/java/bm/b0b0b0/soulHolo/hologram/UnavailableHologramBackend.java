package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class UnavailableHologramBackend implements HologramBackend {

    @Override
    public String id() {
        return "unavailable";
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public Set<DisplaySettingKey> supportedSettings() {
        return Collections.emptySet();
    }

    @Override
    public void spawn(PrivateHologram hologram, List<String> renderedLines) {
    }

    @Override
    public void update(PrivateHologram hologram, List<String> renderedLines) {
    }

    @Override
    public void remove(PrivateHologram hologram) {
    }

    @Override
    public String formatLine(Player viewer, String line) {
        return line == null ? "" : line;
    }
}
