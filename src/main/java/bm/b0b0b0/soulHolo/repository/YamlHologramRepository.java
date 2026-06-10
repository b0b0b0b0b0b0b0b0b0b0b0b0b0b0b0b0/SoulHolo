package bm.b0b0b0.soulHolo.repository;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.core.PluginExecutor;
import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class YamlHologramRepository implements HologramRepository {

    private final JavaPlugin plugin;
    private final PluginExecutor executor;
    private final File storageDir;
    private final Map<UUID, PrivateHologram> byId = new ConcurrentHashMap<>();
    private final Map<String, UUID> byName = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Void>> saveChains = new ConcurrentHashMap<>();

    public YamlHologramRepository(JavaPlugin plugin, PluginExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
        this.storageDir = new File(plugin.getDataFolder(), "holograms");
    }

    @Override
    public CompletableFuture<Void> loadAll() {
        return CompletableFuture.runAsync(() -> {
            byId.clear();
            byName.clear();
            saveChains.clear();
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                plugin.getLogger().warning("Could not create holograms folder");
                return;
            }
            File[] files = storageDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) {
                return;
            }
            for (File file : files) {
                PrivateHologram hologram = readFile(file);
                if (hologram != null) {
                    index(hologram);
                }
            }
        }, executor.io());
    }

    @Override
    public void save(PrivateHologram hologram) {
        index(hologram);
        UUID id = hologram.id();
        saveChains.compute(id, (ignored, chain) -> {
            CompletableFuture<Void> previous = chain == null ? CompletableFuture.completedFuture(null) : chain;
            return previous.thenRunAsync(() -> writeFile(hologram), executor.io());
        });
    }

    @Override
    public void awaitPendingSaves() {
        CompletableFuture<?>[] pending = saveChains.values().toArray(new CompletableFuture[0]);
        if (pending.length == 0) {
            return;
        }
        try {
            CompletableFuture.allOf(pending).get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Timed out waiting for hologram saves", exception);
        }
    }

    @Override
    public void delete(UUID id) {
        PrivateHologram removed = byId.remove(id);
        if (removed == null) {
            return;
        }
        byName.remove(removed.name());
        saveChains.compute(id, (ignored, chain) -> {
            CompletableFuture<Void> previous = chain == null ? CompletableFuture.completedFuture(null) : chain;
            return previous.thenRunAsync(() -> deleteFile(id), executor.io());
        });
    }

    @Override
    public Optional<PrivateHologram> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<PrivateHologram> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        UUID id = byName.get(name.toLowerCase(Locale.ROOT));
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public Collection<PrivateHologram> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    @Override
    public List<PrivateHologram> findByOwner(UUID ownerId) {
        List<PrivateHologram> owned = new ArrayList<>();
        for (PrivateHologram hologram : byId.values()) {
            if (hologram.ownerId().equals(ownerId)) {
                owned.add(hologram);
            }
        }
        owned.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));
        return owned;
    }

    @Override
    public int countForLimit(String regionId, UUID ownerId, PluginConfig.LimitCountScope scope) {
        int count = 0;
        for (PrivateHologram hologram : byId.values()) {
            if (!hologram.regionId().equalsIgnoreCase(regionId)) {
                continue;
            }
            if (scope == PluginConfig.LimitCountScope.REGION) {
                count++;
            } else if (hologram.ownerId().equals(ownerId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Optional<PrivateHologram> findNearestOwned(PlayerContext context) {
        if (context.location().getWorld() == null) {
            return Optional.empty();
        }
        double radiusSquared = context.radius() * context.radius();
        PrivateHologram nearest = null;
        double nearestDistance = radiusSquared;
        for (PrivateHologram hologram : byId.values()) {
            if (!hologram.ownerId().equals(context.playerId())) {
                continue;
            }
            Location location = hologram.location();
            if (location == null || location.getWorld() == null) {
                continue;
            }
            if (!location.getWorld().equals(context.location().getWorld())) {
                continue;
            }
            double distance = location.distanceSquared(context.location());
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = hologram;
            }
        }
        return Optional.ofNullable(nearest);
    }

    private void index(PrivateHologram hologram) {
        byId.put(hologram.id(), hologram);
        byName.put(hologram.name(), hologram.id());
    }

    private File fileFor(UUID id) {
        return new File(storageDir, id + ".yml");
    }

    private void deleteFile(UUID id) {
        File file = fileFor(id);
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Could not delete hologram file " + file.getName());
        }
        saveChains.remove(id);
    }

    private PrivateHologram readFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        UUID id = UUID.fromString(yaml.getString("id"));
        String name = yaml.getString("name");
        UUID ownerId = UUID.fromString(yaml.getString("owner-id"));
        String ownerName = yaml.getString("owner-name", "Unknown");
        String regionId = yaml.getString("region-id");
        String worldName = yaml.getString("world");
        double x = yaml.getDouble("x");
        double y = yaml.getDouble("y");
        double z = yaml.getDouble("z");
        List<String> lines = yaml.getStringList("lines");
        String backendId = yaml.getString("backend-id");
        HologramDisplaySettings displaySettings = readDisplay(yaml.getConfigurationSection("display"));
        if (worldName == null || worldName.isBlank()) {
            plugin.getLogger().warning("Skipping hologram " + name + ": world name missing");
            return null;
        }
        return new PrivateHologram(
                id,
                name,
                ownerId,
                ownerName,
                regionId,
                worldName,
                x,
                y,
                z,
                lines,
                backendId,
                displaySettings
        );
    }

    private HologramDisplaySettings readDisplay(org.bukkit.configuration.ConfigurationSection section) {
        HologramDisplaySettings settings = new HologramDisplaySettings();
        if (section == null) {
            return settings;
        }
        settings.setEnabled(section.getBoolean("enabled", true));
        settings.setSeeThrough(section.getBoolean("see-through", false));
        settings.setTextShadow(section.getBoolean("text-shadow", false));
        settings.setBillboard(HologramDisplaySettings.DisplayBillboard.fromConfig(section.getString("billboard")));
        settings.setBackgroundPreset(section.getString("background-preset", "transparent"));
        settings.setBackgroundColor(
                section.getInt("background-red", 0),
                section.getInt("background-green", 0),
                section.getInt("background-blue", 0),
                section.getInt("background-alpha", 0)
        );
        settings.setScale((float) section.getDouble("scale", 1.0));
        settings.setTextAlignment(parseAlignment(section.getString("text-alignment", "CENTER")));
        settings.setShadowRadius((float) section.getDouble("shadow-radius", 0.0));
        settings.setShadowStrength((float) section.getDouble("shadow-strength", 0.0));
        return settings;
    }

    private TextDisplay.TextAlignment parseAlignment(String raw) {
        if (raw == null) {
            return TextDisplay.TextAlignment.CENTER;
        }
        try {
            return TextDisplay.TextAlignment.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return TextDisplay.TextAlignment.CENTER;
        }
    }

    private void writeFile(PrivateHologram hologram) {
        File file = fileFor(hologram.id());
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", hologram.id().toString());
        yaml.set("name", hologram.name());
        yaml.set("owner-id", hologram.ownerId().toString());
        yaml.set("owner-name", hologram.ownerName());
        yaml.set("region-id", hologram.regionId());
        yaml.set("world", hologram.worldName());
        yaml.set("x", hologram.x());
        yaml.set("y", hologram.y());
        yaml.set("z", hologram.z());
        yaml.set("lines", new ArrayList<>(hologram.lines()));
        yaml.set("backend-id", hologram.backendId());
        writeDisplay(yaml, hologram.displaySettings());
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save hologram " + hologram.name(), exception);
        }
    }

    private void writeDisplay(YamlConfiguration yaml, HologramDisplaySettings settings) {
        yaml.set("display.enabled", settings.enabled());
        yaml.set("display.see-through", settings.seeThrough());
        yaml.set("display.text-shadow", settings.textShadow());
        yaml.set("display.billboard", settings.billboard().name());
        yaml.set("display.background-preset", settings.backgroundPreset());
        yaml.set("display.background-red", settings.backgroundRed());
        yaml.set("display.background-green", settings.backgroundGreen());
        yaml.set("display.background-blue", settings.backgroundBlue());
        yaml.set("display.background-alpha", settings.backgroundAlpha());
        yaml.set("display.scale", settings.scale());
        yaml.set("display.text-alignment", settings.textAlignment().name());
        yaml.set("display.shadow-radius", settings.shadowRadius());
        yaml.set("display.shadow-strength", settings.shadowStrength());
    }
}
