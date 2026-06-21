package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.permission.SoulHoloPermissions;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class HologramDisplayService {

    private PluginConfig config;
    private final HologramService hologramService;
    private final DisplaySettingAccess access;
    private final MessageService messages;
    private ActionLogService actionLogService;

    public HologramDisplayService(PluginConfig config,
                                    HologramService hologramService,
                                    DisplaySettingAccess access,
                                    MessageService messages,
                                    ActionLogService actionLogService) {
        this.config = config;
        this.hologramService = hologramService;
        this.access = access;
        this.messages = messages;
        this.actionLogService = actionLogService;
    }

    public void reload(PluginConfig config, ActionLogService actionLogService) {
        this.config = config;
        this.actionLogService = actionLogService;
    }

    public Optional<PrivateHologram> ownedHologram(Player player, java.util.UUID hologramId) {
        Optional<PrivateHologram> optional = hologramService.findById(hologramId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        PrivateHologram hologram = optional.get();
        if (!hologram.ownerId().equals(player.getUniqueId())
                && !SoulHoloPermissions.hasAdmin(player)) {
            return Optional.empty();
        }
        return optional;
    }

    public boolean toggleBoolean(Player player, PrivateHologram hologram, DisplaySettingKey key) {
        if (!ensureAllowed(player, hologram, key)) {
            return false;
        }
        HologramDisplaySettings settings = hologram.displaySettings();
        switch (key) {
            case ENABLED -> settings.setEnabled(!settings.enabled());
            case SEE_THROUGH -> settings.setSeeThrough(!settings.seeThrough());
            case TEXT_SHADOW -> settings.setTextShadow(!settings.textShadow());
            default -> {
                return false;
            }
        }
        commit(player, hologram, key);
        return true;
    }

    public boolean cycleBillboard(Player player, PrivateHologram hologram) {
        DisplaySettingKey key = DisplaySettingKey.BILLBOARD;
        if (!ensureAllowed(player, hologram, key)) {
            return false;
        }
        hologram.displaySettings().setBillboard(hologram.displaySettings().billboard().next());
        commit(player, hologram, key);
        return true;
    }

    public boolean cycleBackground(Player player, PrivateHologram hologram) {
        DisplaySettingKey key = DisplaySettingKey.BACKGROUND;
        if (!ensureAllowed(player, hologram, key)) {
            return false;
        }
        List<PluginConfig.BackgroundPreset> allowed = config.backgroundPresets();
        if (allowed.isEmpty()) {
            messages.send(player, "gui-setting-locked", Map.of("setting", access.settingLabel(key)));
            return false;
        }
        String current = hologram.displaySettings().backgroundPreset();
        int index = 0;
        for (int i = 0; i < allowed.size(); i++) {
            if (allowed.get(i).id().equalsIgnoreCase(current)) {
                index = i;
                break;
            }
        }
        PluginConfig.BackgroundPreset next = allowed.get((index + 1) % allowed.size());
        hologram.displaySettings().setBackgroundPreset(next.id());
        hologram.displaySettings().setBackgroundColor(next.red(), next.green(), next.blue(), next.alpha());
        commit(player, hologram, key);
        return true;
    }

    public boolean adjustScale(Player player, PrivateHologram hologram, boolean increase) {
        DisplaySettingKey key = DisplaySettingKey.SCALE;
        if (!ensureAllowed(player, hologram, key)) {
            return false;
        }
        float value = hologram.displaySettings().scale();
        value = increase ? value + config.scaleStep() : value - config.scaleStep();
        value = Math.max(config.scaleMin(), Math.min(config.scaleMax(), value));
        hologram.displaySettings().setScale(value);
        commit(player, hologram, key);
        return true;
    }

    public boolean cycleAlignment(Player player, PrivateHologram hologram) {
        DisplaySettingKey key = DisplaySettingKey.TEXT_ALIGNMENT;
        if (!ensureAllowed(player, hologram, key)) {
            return false;
        }
        TextDisplay.TextAlignment current = hologram.displaySettings().textAlignment();
        TextDisplay.TextAlignment next = switch (current) {
            case LEFT -> TextDisplay.TextAlignment.CENTER;
            case CENTER -> TextDisplay.TextAlignment.RIGHT;
            default -> TextDisplay.TextAlignment.LEFT;
        };
        hologram.displaySettings().setTextAlignment(next);
        commit(player, hologram, key);
        return true;
    }

    public boolean cycleShadow(Player player, PrivateHologram hologram) {
        DisplaySettingKey key = DisplaySettingKey.SHADOW;
        if (!ensureAllowed(player, hologram, key)) {
            return false;
        }
        HologramDisplaySettings settings = hologram.displaySettings();
        if (settings.shadowRadius() <= 0.0f && settings.shadowStrength() <= 0.0f) {
            settings.setShadowRadius(config.shadowLowRadius());
            settings.setShadowStrength(config.shadowLowStrength());
        } else if (settings.shadowRadius() <= config.shadowLowRadius()) {
            settings.setShadowRadius(config.shadowHighRadius());
            settings.setShadowStrength(config.shadowHighStrength());
        } else {
            settings.setShadowRadius(0.0f);
            settings.setShadowStrength(0.0f);
        }
        commit(player, hologram, key);
        return true;
    }

    private boolean ensureAllowed(Player player, PrivateHologram hologram, DisplaySettingKey key) {
        if (!hologram.ownerId().equals(player.getUniqueId()) && !access.isAdmin(player)) {
            messages.send(player, "hologram-not-owned");
            return false;
        }
        if (!access.canChange(player, key)) {
            messages.send(player, "gui-setting-locked", Map.of("setting", access.settingLabel(key)));
            return false;
        }
        if (!access.isSupported(key)) {
            messages.send(player, "gui-setting-unsupported", Map.of(
                    "setting", access.settingLabel(key),
                    "backend", hologramService.backendId()
            ));
            return false;
        }
        return true;
    }

    private void commit(Player player, PrivateHologram hologram, DisplaySettingKey key) {
        hologramService.resync(hologram, player);
        actionLogService.log("GUI_SETTING", player.getName() + " holo=" + hologram.name()
                + " setting=" + key.name().toLowerCase(Locale.ROOT));
        messages.send(player, "gui-setting-changed", Map.of("setting", access.settingLabel(key)));
    }
}
