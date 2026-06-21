package bm.b0b0b0.soulHolo.repository;

import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RegionWorldKey;

import bm.b0b0b0.soulHolo.config.PluginConfig;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HologramRepository {

    CompletableFuture<Void> loadAll();

    void save(PrivateHologram hologram);

    void awaitPendingSaves();

    void delete(UUID id);

    Optional<PrivateHologram> findById(UUID id);

    Optional<PrivateHologram> findByName(String name);

    Collection<PrivateHologram> all();

    List<PrivateHologram> findByOwner(UUID ownerId);

    int countForLimit(String regionId, UUID ownerId, PluginConfig.LimitCountScope scope);

    Set<RegionWorldKey> regionKeys();

    List<PrivateHologram> hologramsInRegion(RegionWorldKey key);

    Optional<PrivateHologram> findNearestOwned(PlayerContext context);
}
