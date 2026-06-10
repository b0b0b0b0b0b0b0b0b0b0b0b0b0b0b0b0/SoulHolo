package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class FancyHologramsBackend implements HologramBackend {

    private static final Set<DisplaySettingKey> SUPPORTED = EnumSet.allOf(DisplaySettingKey.class);

    @Override
    public String id() {
        return "fancy";
    }

    @Override
    public boolean available() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("FancyHolograms");
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
        FancyHologramsPlugin fancyPlugin = FancyHologramsPlugin.get();
        if (fancyPlugin == null) {
            return;
        }
        String backendId = backendId(hologram);
        hologram.setBackendId(backendId);
        var manager = fancyPlugin.getHologramManager();
        Optional<Hologram> existing = manager.getHologram(backendId);
        existing.ifPresent(value -> manager.removeHologram(value));
        List<String> lines = renderedLines == null ? List.of() : new ArrayList<>(renderedLines);
        if (lines.isEmpty()) {
            lines.add(" ");
        }
        TextHologramData data = new TextHologramData(backendId, hologram.location());
        data.setText(lines);
        FancyDisplayApplier.apply(data, hologram);
        Hologram created = manager.create(data);
        manager.addHologram(created);
    }

    @Override
    public void update(PrivateHologram hologram, List<String> renderedLines) {
        if (!hologram.displaySettings().enabled()) {
            remove(hologram);
            return;
        }
        FancyHologramsPlugin fancyPlugin = FancyHologramsPlugin.get();
        if (fancyPlugin == null) {
            return;
        }
        String backendId = backendId(hologram);
        var manager = fancyPlugin.getHologramManager();
        Optional<Hologram> optional = manager.getHologram(backendId);
        if (optional.isEmpty()) {
            spawn(hologram, renderedLines);
            return;
        }
        Hologram existing = optional.get();
        if (!(existing.getData() instanceof TextHologramData textData)) {
            spawn(hologram, renderedLines);
            return;
        }
        textData.setText(renderedLines == null ? List.of() : new ArrayList<>(renderedLines));
        FancyDisplayApplier.apply(textData, hologram);
        existing.queueUpdate();
    }

    @Override
    public void remove(PrivateHologram hologram) {
        FancyHologramsPlugin fancyPlugin = FancyHologramsPlugin.get();
        if (fancyPlugin == null) {
            return;
        }
        String backendId = backendId(hologram);
        var manager = fancyPlugin.getHologramManager();
        manager.getHologram(backendId).ifPresent(manager::removeHologram);
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
