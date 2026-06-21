package bm.b0b0b0.soulHolo.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSessionService {

    private final Map<UUID, UUID> activeHologramByPlayer = new ConcurrentHashMap<>();

    public void setActive(UUID playerId, UUID hologramId) {
        activeHologramByPlayer.put(playerId, hologramId);
    }

    public UUID activeHologram(UUID playerId) {
        return activeHologramByPlayer.get(playerId);
    }

    public void clear(UUID playerId) {
        activeHologramByPlayer.remove(playerId);
    }

    public void clearActiveHologram(UUID hologramId) {
        activeHologramByPlayer.entrySet().removeIf(entry -> hologramId.equals(entry.getValue()));
    }
}
