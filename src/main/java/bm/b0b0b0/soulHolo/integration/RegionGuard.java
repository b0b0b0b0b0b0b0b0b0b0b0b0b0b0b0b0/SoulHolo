package bm.b0b0b0.soulHolo.integration;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface RegionGuard {

    boolean available();

    Optional<String> ownerRegionAt(Player player, Location location);

    Optional<String> anyRegionAt(Location location);

    boolean regionExists(String regionId, Location location);

    boolean regionExistsInWorld(String regionId, String worldName);

    boolean containsInRegion(String regionId, Location location);
}
