package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.hologram.HologramBackend;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import org.bukkit.entity.Player;

import java.util.Map;

public final class DisplaySettingAccess {

    private PluginConfig config;
    private HologramBackend backend;
    private final MessageService messages;

    public DisplaySettingAccess(PluginConfig config, HologramBackend backend, MessageService messages) {
        this.config = config;
        this.backend = backend;
        this.messages = messages;
    }

    public void reload(PluginConfig config, HologramBackend backend) {
        this.config = config;
        this.backend = backend;
    }

    public boolean isAdmin(Player player) {
        return player.isOp() || player.hasPermission(config.adminPermission());
    }

    public boolean canOpenGui(Player player) {
        return isAdmin(player) || player.hasPermission(config.guiOpenPermission());
    }

    public boolean canEditLines(Player player) {
        return isAdmin(player) || player.hasPermission(config.guiLinesPermission());
    }

    public boolean canMovePosition(Player player) {
        return isAdmin(player) || player.hasPermission(config.guiPositionPermission());
    }

    public boolean canChange(Player player, DisplaySettingKey key) {
        if (isAdmin(player)) {
            return true;
        }
        String permission = config.guiSettingPermissions().get(key);
        if (permission == null || permission.isBlank()) {
            return false;
        }
        return player.hasPermission(permission);
    }

    public boolean canUseBackgroundPreset(Player player, String presetPermission) {
        if (isAdmin(player)) {
            return true;
        }
        return player.hasPermission(presetPermission);
    }

    public boolean isSupported(DisplaySettingKey key) {
        return backend.supports(key);
    }

    public String settingMessageKey(DisplaySettingKey key) {
        return switch (key) {
            case ENABLED -> "enabled";
            case SEE_THROUGH -> "see-through";
            case TEXT_SHADOW -> "text-shadow";
            case BILLBOARD -> "billboard";
            case BACKGROUND -> "background";
            case SCALE -> "scale";
            case TEXT_ALIGNMENT -> "alignment";
            case SHADOW -> "shadow";
        };
    }

    public String settingLabel(DisplaySettingKey key) {
        return messages.plain("gui.setting-label." + settingMessageKey(key), Map.of());
    }
}
