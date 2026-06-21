package bm.b0b0b0.soulHolo.model;

import java.util.Objects;

public record RegionWorldKey(String worldName, String regionId) {

    public RegionWorldKey {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(regionId, "regionId");
        worldName = worldName.trim();
        regionId = regionId.trim();
    }

    public static RegionWorldKey from(PrivateHologram hologram) {
        return new RegionWorldKey(hologram.worldName(), hologram.regionId());
    }
}
