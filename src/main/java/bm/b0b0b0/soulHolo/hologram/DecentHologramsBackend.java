package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class DecentHologramsBackend implements HologramBackend {

    private static final Set<DisplaySettingKey> SUPPORTED = EnumSet.of(
            DisplaySettingKey.ENABLED,
            DisplaySettingKey.BILLBOARD
    );

    @Override
    public String id() {
        return "decent";
    }

    @Override
    public boolean available() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("DecentHolograms");
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public Set<DisplaySettingKey> supportedSettings() {
        return SUPPORTED;
    }

    @Override
    public void spawn(PrivateHologram hologram, List<String> renderedLines) {
        if (!hologram.displaySettings().enabled()) {
            remove(hologram);
            return;
        }
        String backendId = backendId(hologram);
        hologram.setBackendId(backendId);
        if (DHAPI.getHologram(backendId) != null) {
            DHAPI.removeHologram(backendId);
        }
        List<String> lines = renderedLines == null ? List.of() : new ArrayList<>(renderedLines);
        if (lines.isEmpty()) {
            lines.add(" ");
        }
        Hologram created = DHAPI.createHologram(backendId, hologram.location(), false, lines);
        DecentDisplayApplier.apply(created, hologram);
    }

    @Override
    public void update(PrivateHologram hologram, List<String> renderedLines) {
        if (!hologram.displaySettings().enabled()) {
            remove(hologram);
            return;
        }
        String backendId = backendId(hologram);
        Hologram existing = DHAPI.getHologram(backendId);
        if (existing == null) {
            spawn(hologram, renderedLines);
            return;
        }
        DHAPI.setHologramLines(existing, renderedLines == null ? List.of() : new ArrayList<>(renderedLines));
        DecentDisplayApplier.apply(existing, hologram);
    }

    @Override
    public void remove(PrivateHologram hologram) {
        String backendId = backendId(hologram);
        if (DHAPI.getHologram(backendId) != null) {
            DHAPI.removeHologram(backendId);
        }
    }

    @Override
    public String formatLine(Player viewer, String line) {
        return line == null ? "" : line;
    }

    private String backendId(PrivateHologram hologram) {
        if (hologram.backendId() != null && !hologram.backendId().isBlank()) {
            return hologram.backendId();
        }
        return "soulholo_" + hologram.id().toString().replace("-", "");
    }
}
