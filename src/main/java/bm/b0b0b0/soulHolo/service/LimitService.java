package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.permission.SoulHoloPermissions;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public final class LimitService {

    private PluginConfig config;

    public LimitService(PluginConfig config) {
        this.config = config;
    }

    public void reload(PluginConfig config) {
        this.config = config;
    }

    public PluginConfig.PlayerLimits resolve(Player player) {
        return new PluginConfig.PlayerLimits(
                resolveHologramLimit(player),
                config.maxLines(),
                config.maxLineLength()
        );
    }

    public boolean hasHologramSlot(Player player) {
        return resolve(player).maxHologramsPerRegion() > 0;
    }

    public int nameMinLength() {
        return config.nameMinLength();
    }

    public int nameMaxLength() {
        return config.nameMaxLength();
    }

    public String namePatternLabel() {
        return config.namePattern().pattern();
    }

    public PluginConfig.LimitCountScope countScope() {
        return config.limitCountScope();
    }

    public boolean isNameValid(String name) {
        if (name == null) {
            return false;
        }
        int length = name.length();
        if (length < config.nameMinLength() || length > config.nameMaxLength()) {
            return false;
        }
        return config.namePattern().matcher(name).matches();
    }

    private int resolveHologramLimit(Player player) {
        String prefix = SoulHoloPermissions.LIMIT_PREFIX;
        int best = -1;
        for (PermissionAttachmentInfo attachment : player.getEffectivePermissions()) {
            if (!attachment.getValue()) {
                continue;
            }
            String permission = attachment.getPermission();
            if (!permission.startsWith(prefix)) {
                continue;
            }
            String suffix = permission.substring(prefix.length());
            if (suffix.isEmpty()) {
                continue;
            }
            try {
                int value = Integer.parseInt(suffix);
                if (value > best) {
                    best = value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (best >= 0) {
            return best;
        }
        return 0;
    }
}
