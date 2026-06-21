package bm.b0b0b0.soulHolo.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LineEditSessionService {

    public enum Mode {
        EDIT,
        ADD
    }

    public record PendingLineEdit(UUID hologramId, int lineNumber, Mode mode) {
    }

    private final Map<UUID, PendingLineEdit> pendingByPlayer = new ConcurrentHashMap<>();

    public void beginEdit(UUID playerId, UUID hologramId, int lineNumber) {
        pendingByPlayer.put(playerId, new PendingLineEdit(hologramId, lineNumber, Mode.EDIT));
    }

    public void beginInsert(UUID playerId, UUID hologramId, int lineNumber) {
        pendingByPlayer.put(playerId, new PendingLineEdit(hologramId, lineNumber, Mode.ADD));
    }

    public void beginAdd(UUID playerId, UUID hologramId) {
        beginInsert(playerId, hologramId, 0);
    }

    public PendingLineEdit pending(UUID playerId) {
        return pendingByPlayer.get(playerId);
    }

    public void clear(UUID playerId) {
        pendingByPlayer.remove(playerId);
    }
}
