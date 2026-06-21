package bm.b0b0b0.soulHolo.permission;

import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import org.bukkit.entity.Player;

public final class SoulHoloPermissions {

    public static final String ADMIN = "soulholo.admin";
    public static final String GUI_LINES = "soulholo.gui.lines";
    public static final String GUI_POSITION = "soulholo.gui.position";
    public static final String LIMIT_PREFIX = "soulholo.limit.";

    private static final String GUI_SETTING_PREFIX = "soulholo.gui.setting.";

    private SoulHoloPermissions() {
    }

    public static boolean hasAdmin(org.bukkit.command.CommandSender sender) {
        return sender.isOp() || sender.hasPermission(ADMIN);
    }

    public static boolean bypassesLimits(Player player) {
        return hasAdmin(player);
    }

    public static String guiSetting(DisplaySettingKey key) {
        return switch (key) {
            case ENABLED -> GUI_SETTING_PREFIX + "enabled";
            case SEE_THROUGH -> GUI_SETTING_PREFIX + "see-through";
            case TEXT_SHADOW -> GUI_SETTING_PREFIX + "text-shadow";
            case BILLBOARD -> GUI_SETTING_PREFIX + "billboard";
            case BACKGROUND -> GUI_SETTING_PREFIX + "background";
            case SCALE -> GUI_SETTING_PREFIX + "scale";
            case TEXT_ALIGNMENT -> GUI_SETTING_PREFIX + "text-alignment";
            case SHADOW -> GUI_SETTING_PREFIX + "shadow";
        };
    }
}
