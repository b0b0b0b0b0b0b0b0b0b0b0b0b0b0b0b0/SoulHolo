package bm.b0b0b0.soulHolo.config;

import bm.b0b0b0.soulHolo.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulHolo.config.settings.SoulHoloSettings;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class PluginConfig {

    private final HologramBackendType backendType;
    private final String ownerLine;
    private final double nearestRadius;
    private final boolean loggingEnabled;
    private final boolean loggingFile;
    private final LimitTier defaultLimit;
    private final List<LimitTier> limitTiers;
    private final LimitCountScope limitCountScope;
    private final List<String> blacklistLiterals;
    private final List<Pattern> blacklistRegex;
    private final int nameMinLength;
    private final int nameMaxLength;
    private final Pattern namePattern;
    private final int restoreBatchSize;
    private final int ioThreads;
    private final String guiOpenPermission;
    private final String guiLinesPermission;
    private final String guiPositionPermission;
    private final double positionStep;
    private final String usePermission;
    private final String adminPermission;
    private final String adminFallbackRegionId;
    private final float shadowLowRadius;
    private final float shadowLowStrength;
    private final float shadowHighRadius;
    private final float shadowHighStrength;
    private final float scaleMin;
    private final float scaleMax;
    private final float scaleStep;
    private final Map<DisplaySettingKey, String> guiSettingPermissions;
    private final List<BackgroundPreset> backgroundPresets;
    private final GuiLayoutConfig guiLayout;

    public PluginConfig(SoulHoloSettings main, GuiGeneralSettings gui) {
        this.backendType = HologramBackendType.fromConfig(main.hologramBackend);
        this.ownerLine = main.ownerLine;
        this.nearestRadius = main.selection.nearestRadius;
        this.loggingEnabled = main.logging.enabled;
        this.loggingFile = main.logging.file;
        this.defaultLimit = toTier(main.limits.defaultTier);
        this.limitTiers = new ArrayList<>();
        for (SoulHoloSettings.LimitTierSettings tier : main.limits.tiers.values()) {
            limitTiers.add(toTier(tier));
        }
        this.limitCountScope = LimitCountScope.fromConfig(main.limits.countScope);
        this.blacklistLiterals = List.copyOf(main.blacklist.literals);
        this.blacklistRegex = compileRegex(main.blacklist.regex);
        this.nameMinLength = main.name.minLength;
        this.nameMaxLength = main.name.maxLength;
        this.namePattern = Pattern.compile(main.name.pattern);
        this.restoreBatchSize = Math.max(1, main.performance.restoreBatchSize);
        this.ioThreads = Math.max(1, main.performance.ioThreads);
        this.guiOpenPermission = main.gui.openPermission;
        this.guiLinesPermission = main.gui.linesPermission;
        this.guiPositionPermission = main.gui.positionPermission;
        this.positionStep = main.gui.position.step;
        this.usePermission = main.permissions.use;
        this.adminPermission = main.permissions.admin;
        this.adminFallbackRegionId = main.admin.fallbackRegionId;
        this.shadowLowRadius = main.gui.shadowCycle.lowRadius;
        this.shadowLowStrength = main.gui.shadowCycle.lowStrength;
        this.shadowHighRadius = main.gui.shadowCycle.highRadius;
        this.shadowHighStrength = main.gui.shadowCycle.highStrength;
        this.scaleMin = main.gui.scale.min;
        this.scaleMax = main.gui.scale.max;
        this.scaleStep = main.gui.scale.step;
        this.guiSettingPermissions = readGuiPermissions(main.gui.settings);
        this.backgroundPresets = readBackgroundPresets(main.gui.backgroundPresets);
        this.guiLayout = GuiLayoutConfig.from(gui);
    }

    private static LimitTier toTier(SoulHoloSettings.LimitTierSettings tier) {
        return new LimitTier(
                tier.permission,
                tier.maxHologramsPerRegion,
                tier.maxLines,
                tier.maxLineLength
        );
    }

    private static Map<DisplaySettingKey, String> readGuiPermissions(SoulHoloSettings.GuiSettingPermissions settings) {
        Map<DisplaySettingKey, String> map = new EnumMap<>(DisplaySettingKey.class);
        map.put(DisplaySettingKey.ENABLED, settings.enabled);
        map.put(DisplaySettingKey.SEE_THROUGH, settings.seeThrough);
        map.put(DisplaySettingKey.TEXT_SHADOW, settings.textShadow);
        map.put(DisplaySettingKey.BILLBOARD, settings.billboard);
        map.put(DisplaySettingKey.BACKGROUND, settings.background);
        map.put(DisplaySettingKey.SCALE, settings.scale);
        map.put(DisplaySettingKey.TEXT_ALIGNMENT, settings.textAlignment);
        map.put(DisplaySettingKey.SHADOW, settings.shadow);
        return map;
    }

    private static List<BackgroundPreset> readBackgroundPresets(List<SoulHoloSettings.BackgroundPresetSettings> presets) {
        List<BackgroundPreset> result = new ArrayList<>();
        for (SoulHoloSettings.BackgroundPresetSettings preset : presets) {
            result.add(new BackgroundPreset(
                    preset.id,
                    preset.permission,
                    preset.red,
                    preset.green,
                    preset.blue,
                    preset.alpha
            ));
        }
        return List.copyOf(result);
    }

    private static List<Pattern> compileRegex(List<String> patterns) {
        List<Pattern> compiled = new ArrayList<>();
        for (String raw : patterns) {
            try {
                compiled.add(Pattern.compile(raw));
            } catch (Exception ignored) {
            }
        }
        return compiled;
    }

    public HologramBackendType backendType() {
        return backendType;
    }

    public String ownerLine() {
        return ownerLine;
    }

    public double nearestRadius() {
        return nearestRadius;
    }

    public boolean loggingEnabled() {
        return loggingEnabled;
    }

    public boolean loggingFile() {
        return loggingFile;
    }

    public LimitTier defaultLimit() {
        return defaultLimit;
    }

    public List<LimitTier> limitTiers() {
        return limitTiers;
    }

    public LimitCountScope limitCountScope() {
        return limitCountScope;
    }

    public int restoreBatchSize() {
        return restoreBatchSize;
    }

    public int ioThreads() {
        return ioThreads;
    }

    public String guiOpenPermission() {
        return guiOpenPermission;
    }

    public String guiLinesPermission() {
        return guiLinesPermission;
    }

    public String guiPositionPermission() {
        return guiPositionPermission;
    }

    public double positionStep() {
        return positionStep;
    }

    public String usePermission() {
        return usePermission;
    }

    public String adminPermission() {
        return adminPermission;
    }

    public String adminFallbackRegionId() {
        return adminFallbackRegionId;
    }

    public float shadowLowRadius() {
        return shadowLowRadius;
    }

    public float shadowLowStrength() {
        return shadowLowStrength;
    }

    public float shadowHighRadius() {
        return shadowHighRadius;
    }

    public float shadowHighStrength() {
        return shadowHighStrength;
    }

    public float scaleMin() {
        return scaleMin;
    }

    public float scaleMax() {
        return scaleMax;
    }

    public float scaleStep() {
        return scaleStep;
    }

    public Map<DisplaySettingKey, String> guiSettingPermissions() {
        return guiSettingPermissions;
    }

    public List<BackgroundPreset> backgroundPresets() {
        return backgroundPresets;
    }

    public List<String> blacklistLiterals() {
        return blacklistLiterals;
    }

    public List<Pattern> blacklistRegex() {
        return blacklistRegex;
    }

    public int nameMinLength() {
        return nameMinLength;
    }

    public int nameMaxLength() {
        return nameMaxLength;
    }

    public Pattern namePattern() {
        return namePattern;
    }

    public GuiLayoutConfig guiLayout() {
        return guiLayout;
    }

    public record LimitTier(String permission, int maxHologramsPerRegion, int maxLines, int maxLineLength) {
    }

    public record BackgroundPreset(String id, String permission, int red, int green, int blue, int alpha) {
    }

    public enum LimitCountScope {
        OWNER_REGION,
        REGION;

        public static LimitCountScope fromConfig(String raw) {
            if (raw == null) {
                return OWNER_REGION;
            }
            return switch (raw.trim().toLowerCase(Locale.ROOT)) {
                case "region", "total", "global" -> REGION;
                default -> OWNER_REGION;
            };
        }
    }

    public enum HologramBackendType {
        AUTO,
        PAPER,
        DECENT,
        FANCY;

        public static HologramBackendType fromConfig(String raw) {
            if (raw == null) {
                return AUTO;
            }
            return switch (raw.trim().toLowerCase(Locale.ROOT)) {
                case "paper", "native", "display", "textdisplay" -> PAPER;
                case "decent", "decentholograms" -> DECENT;
                case "fancy", "fancyholograms" -> FANCY;
                default -> AUTO;
            };
        }
    }
}
