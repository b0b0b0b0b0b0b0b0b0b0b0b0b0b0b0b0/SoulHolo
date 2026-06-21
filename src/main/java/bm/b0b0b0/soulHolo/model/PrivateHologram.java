package bm.b0b0b0.soulHolo.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PrivateHologram {

    private final UUID id;
    private final String name;
    private final UUID ownerId;
    private String ownerName;
    private final String regionId;
    private final String worldName;
    private double x;
    private double y;
    private double z;
    private final List<String> lines;
    private String backendId;
    private final HologramDisplaySettings displaySettings;
    private final Set<Integer> hiddenLines;

    public PrivateHologram(UUID id, String name, UUID ownerId, String ownerName, String regionId,
                           String worldName, double x, double y, double z, List<String> lines, String backendId,
                           HologramDisplaySettings displaySettings) {
        this(id, name, ownerId, ownerName, regionId, worldName, x, y, z, lines, backendId, displaySettings, List.of());
    }

    public PrivateHologram(UUID id, String name, UUID ownerId, String ownerName, String regionId,
                           String worldName, double x, double y, double z, List<String> lines, String backendId,
                           HologramDisplaySettings displaySettings, Collection<Integer> hiddenLines) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT);
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
        this.ownerName = ownerName == null ? "Unknown" : ownerName;
        this.regionId = Objects.requireNonNull(regionId, "regionId");
        this.worldName = Objects.requireNonNull(worldName, "worldName");
        this.x = x;
        this.y = y;
        this.z = z;
        this.lines = new ArrayList<>(lines == null ? List.of() : lines);
        this.backendId = backendId;
        this.displaySettings = displaySettings == null ? new HologramDisplaySettings() : displaySettings;
        this.hiddenLines = new HashSet<>();
        if (hiddenLines != null) {
            for (Integer lineNumber : hiddenLines) {
                if (lineNumber != null && lineNumber > 0) {
                    this.hiddenLines.add(lineNumber);
                }
            }
        }
    }

    public PrivateHologram(UUID id, String name, UUID ownerId, String ownerName, String regionId,
                           Location location, List<String> lines, String backendId,
                           HologramDisplaySettings displaySettings) {
        this(
                id,
                name,
                ownerId,
                ownerName,
                regionId,
                requireWorld(location).getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                lines,
                backendId,
                displaySettings
        );
    }

    private static World requireWorld(Location location) {
        return Objects.requireNonNull(location.getWorld(), "world");
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String ownerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String regionId() {
        return regionId;
    }

    public String worldName() {
        return worldName;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public Location location() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    public void setCoordinates(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public List<String> lines() {
        return lines;
    }

    public void ensureLineCapacity(int lineNumber) {
        while (lines.size() < lineNumber) {
            lines.add("");
        }
    }

    public boolean hasLineContent(int lineNumber) {
        if (lineNumber < 1 || lineNumber > lines.size()) {
            return false;
        }
        String line = lines.get(lineNumber - 1);
        return line != null && !line.isBlank();
    }

    public int countFilledLines() {
        int count = 0;
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                count++;
            }
        }
        return count;
    }

    public Set<Integer> hiddenLines() {
        return Collections.unmodifiableSet(hiddenLines);
    }

    public boolean isLineHidden(int lineNumber) {
        return hiddenLines.contains(lineNumber);
    }

    public void setLineHidden(int lineNumber, boolean hidden) {
        if (lineNumber < 1) {
            return;
        }
        if (hidden) {
            hiddenLines.add(lineNumber);
        } else {
            hiddenLines.remove(lineNumber);
        }
    }

    public String backendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public HologramDisplaySettings displaySettings() {
        return displaySettings;
    }

    public String displayName() {
        return name;
    }
}
