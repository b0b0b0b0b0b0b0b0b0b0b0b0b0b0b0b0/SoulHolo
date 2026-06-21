package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.hologram.HologramBackend;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.permission.SoulHoloPermissions;
import org.bukkit.entity.Player;

import java.util.Map;

public final class DisplaySettingAccess {

    private PluginConfig config;
    private HologramBackend backend;
    private final MessageService messages;
    private final LimitService limitService;

    public DisplaySettingAccess(PluginConfig config,
                                HologramBackend backend,
                                MessageService messages,
                                LimitService limitService) {
        this.config = config;
        this.backend = backend;
        this.messages = messages;
        this.limitService = limitService;
    }

    public void reload(PluginConfig config, HologramBackend backend) {
        this.config = config;
        this.backend = backend;
    }

    public boolean isAdmin(Player player) {
        return SoulHoloPermissions.hasAdmin(player);
    }

    public boolean canOpenGui(Player player) {
        return hasBaseAccess(player);
    }

    public boolean canEditLines(Player player) {
        if (!hasBaseAccess(player)) {
            return false;
        }
        return isAdmin(player) || player.hasPermission(SoulHoloPermissions.GUI_LINES);
    }

    public boolean canMovePosition(Player player) {
        if (!hasBaseAccess(player)) {
            return false;
        }
        return isAdmin(player) || player.hasPermission(SoulHoloPermissions.GUI_POSITION);
    }

    public boolean canChange(Player player, DisplaySettingKey key) {
        if (!hasBaseAccess(player)) {
            return false;
        }
        if (isAdmin(player)) {
            return true;
        }
        return player.hasPermission(SoulHoloPermissions.guiSetting(key));
    }

    public boolean canUseBackgroundPreset(Player player, String presetId) {
        return canChange(player, DisplaySettingKey.BACKGROUND);
    }

    private boolean hasBaseAccess(Player player) {
        return isAdmin(player) || limitService.hasHologramSlot(player);
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
