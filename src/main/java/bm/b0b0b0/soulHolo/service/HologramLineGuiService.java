package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.session.LineEditSessionService;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class HologramLineGuiService {

    private final HologramService hologramService;
    private final MessageService messages;
    private final DisplaySettingAccess access;
    private final LineEditSessionService sessions;

    public HologramLineGuiService(HologramService hologramService,
                                  MessageService messages,
                                  DisplaySettingAccess access,
                                  LineEditSessionService sessions) {
        this.hologramService = hologramService;
        this.messages = messages;
        this.access = access;
        this.sessions = sessions;
    }

    public boolean canEdit(Player player) {
        return access.canEditLines(player);
    }

    public boolean beginEdit(Player player, PrivateHologram hologram, int lineNumber) {
        if (!canEdit(player)) {
            messages.send(player, "gui-lines-no-permission");
            return false;
        }
        sessions.beginEdit(player.getUniqueId(), hologram.id(), lineNumber);
        player.closeInventory();
        messages.send(player, "gui.lines.prompt-edit", Map.of(
                "line", String.valueOf(lineNumber),
                "cancel", messages.plain("gui.lines.cancel-word", Map.of())
        ));
        return true;
    }

    public boolean beginAdd(Player player, PrivateHologram hologram) {
        if (!canEdit(player)) {
            messages.send(player, "gui-lines-no-permission");
            return false;
        }
        sessions.beginAdd(player.getUniqueId(), hologram.id());
        player.closeInventory();
        messages.send(player, "gui.lines.prompt-add", Map.of(
                "cancel", messages.plain("gui.lines.cancel-word", Map.of())
        ));
        return true;
    }

    public boolean deleteLine(Player player, PrivateHologram hologram, int lineNumber) {
        if (!canEdit(player)) {
            messages.send(player, "gui-lines-no-permission");
            return false;
        }
        HologramFailure failure = hologramService.removeLine(player, hologram, lineNumber, access.isAdmin(player));
        if (failure != HologramFailure.NONE) {
            sendFailure(player, failure, lineNumber);
            return false;
        }
        messages.send(player, "gui.lines.deleted", Map.of("line", String.valueOf(lineNumber)));
        return true;
    }

    public boolean moveLine(Player player, PrivateHologram hologram, int lineNumber, int direction) {
        if (!canEdit(player)) {
            messages.send(player, "gui-lines-no-permission");
            return false;
        }
        HologramFailure failure = hologramService.moveLine(player, hologram, lineNumber, direction, access.isAdmin(player));
        if (failure != HologramFailure.NONE) {
            sendFailure(player, failure, lineNumber);
            return false;
        }
        return true;
    }

    public Optional<PrivateHologram> applyChatInput(Player player, String rawText) {
        LineEditSessionService.PendingLineEdit pending = sessions.pending(player.getUniqueId());
        if (pending == null) {
            return Optional.empty();
        }
        String text = rawText == null ? "" : rawText.trim();
        String cancelWord = messages.plain("gui.lines.cancel-word", Map.of()).toLowerCase(Locale.ROOT);
        if (text.equalsIgnoreCase(cancelWord)) {
            sessions.clear(player.getUniqueId());
            messages.send(player, "gui.lines.cancelled");
            return hologramService.findById(pending.hologramId());
        }
        Optional<PrivateHologram> optional = hologramService.findById(pending.hologramId());
        if (optional.isEmpty()) {
            sessions.clear(player.getUniqueId());
            messages.send(player, "hologram-not-found", Map.of("name", "?"));
            return optional;
        }
        PrivateHologram hologram = optional.get();
        boolean admin = access.isAdmin(player);
        HologramFailure failure = switch (pending.mode()) {
            case EDIT -> hologramService.editLine(player, hologram, pending.lineNumber(), text, admin);
            case ADD -> hologramService.addLine(player, hologram, text, admin);
        };
        sessions.clear(player.getUniqueId());
        if (failure != HologramFailure.NONE) {
            sendFailure(player, failure, pending.lineNumber());
            return Optional.of(hologram);
        }
        if (pending.mode() == LineEditSessionService.Mode.ADD) {
            messages.send(player, "gui.lines.added", Map.of("line", String.valueOf(hologram.lines().size())));
        } else {
            messages.send(player, "gui.lines.edited", Map.of("line", String.valueOf(pending.lineNumber())));
        }
        return Optional.of(hologram);
    }

    public int maxLines(Player player) {
        return hologramService.maxUserLines(player);
    }

    private void sendFailure(Player player, HologramFailure failure, int line) {
        switch (failure) {
            case LINE_LIMIT -> messages.send(player, "line-limit-reached", Map.of(
                    "limit", String.valueOf(hologramService.tier(player).maxLines())
            ));
            case LINE_TOO_LONG -> messages.send(player, "line-too-long", Map.of(
                    "limit", String.valueOf(hologramService.tier(player).maxLineLength())
            ));
            case BLACKLISTED -> messages.send(player, "blacklisted-text");
            case INVALID_LINE -> messages.send(player, "invalid-line-number", Map.of(
                    "line", String.valueOf(line),
                    "max", String.valueOf(hologramService.maxUserLines(player))
            ));
            case NOT_OWNED -> messages.send(player, "hologram-not-owned");
            default -> messages.send(player, "unknown-subcommand");
        }
    }
}
