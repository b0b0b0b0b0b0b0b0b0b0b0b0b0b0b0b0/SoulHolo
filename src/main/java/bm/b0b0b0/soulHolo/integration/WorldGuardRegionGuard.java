package bm.b0b0b0.soulHolo.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public final class WorldGuardRegionGuard implements RegionGuard {

    private final boolean available;

    public WorldGuardRegionGuard() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        this.available = plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public Optional<String> ownerRegionAt(Player player, Location location) {
        if (!available || location.getWorld() == null) {
            return Optional.empty();
        }
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));
        ProtectedRegion best = null;
        for (ProtectedRegion region : regions) {
            if (!region.getOwners().contains(localPlayer)) {
                continue;
            }
            if (best == null || region.getPriority() >= best.getPriority()) {
                best = region;
            }
        }
        return best == null ? Optional.empty() : Optional.of(best.getId());
    }

    @Override
    public Optional<String> anyRegionAt(Location location) {
        if (!available || location.getWorld() == null) {
            return Optional.empty();
        }
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));
        ProtectedRegion best = null;
        for (ProtectedRegion region : regions) {
            if (best == null || region.getPriority() >= best.getPriority()) {
                best = region;
            }
        }
        return best == null ? Optional.empty() : Optional.of(best.getId());
    }

    @Override
    public boolean regionExists(String regionId, Location location) {
        if (!available || location.getWorld() == null || regionId == null || regionId.isBlank()) {
            return false;
        }
        var manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
        return manager != null && manager.getRegion(regionId) != null;
    }

    @Override
    public boolean containsInRegion(String regionId, Location location) {
        if (!available || location.getWorld() == null || regionId == null || regionId.isBlank()) {
            return false;
        }
        var manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
        if (manager == null) {
            return false;
        }
        ProtectedRegion region = manager.getRegion(regionId);
        if (region == null) {
            return false;
        }
        return region.contains(BukkitAdapter.asBlockVector(location));
    }
}
