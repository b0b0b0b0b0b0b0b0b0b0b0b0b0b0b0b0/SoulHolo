package bm.b0b0b0.soulHolo.config;

import bm.b0b0b0.soulHolo.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulHolo.config.settings.SoulHoloSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PluginConfig {

    private final HologramBackendType backendType;
    private final String ownerLine;
    private final String defaultCreateLine;
    private final double nearestRadius;
    private final float clickWidth;
    private final float clickHeight;
    private final float clickHeightPerLine;
    private final boolean loggingEnabled;
    private final boolean loggingFile;
    private final int maxLines;
    private final int maxLineLength;
    private final LimitCountScope limitCountScope;
    private final List<String> blacklistLiterals;
    private final List<Pattern> blacklistRegex;
    private final int nameMinLength;
    private final int nameMaxLength;
    private final Pattern namePattern;
    private final int restoreBatchSize;
    private final int ioThreads;
    private final boolean regionGuardPurgeEnabled;
    private final int regionGuardIntervalSeconds;
    private final int regionGuardBatchSize;
    private final double positionStep;
    private final String adminFallbackRegionId;
    private final float shadowLowRadius;
    private final float shadowLowStrength;
    private final float shadowHighRadius;
    private final float shadowHighStrength;
    private final float scaleMin;
    private final float scaleMax;
    private final float scaleStep;
    private final List<BackgroundPreset> backgroundPresets;
    private final GuiLayoutConfig guiLayout;

    public PluginConfig(SoulHoloSettings main, GuiGeneralSettings gui) {
        this.backendType = HologramBackendType.fromConfig(main.hologramBackend);
        this.ownerLine = main.ownerLine;
        this.defaultCreateLine = main.defaultCreateLine;
        this.nearestRadius = main.selection.nearestRadius;
        this.clickWidth = main.selection.clickWidth;
        this.clickHeight = main.selection.clickHeight;
        this.clickHeightPerLine = main.selection.clickHeightPerLine;
        this.loggingEnabled = main.logging.enabled;
        this.loggingFile = main.logging.file;
        this.maxLines = Math.max(1, main.limits.maxLines);
        this.maxLineLength = Math.max(1, main.limits.maxLineLength);
        this.limitCountScope = LimitCountScope.fromConfig(main.limits.countScope);
        this.blacklistLiterals = List.copyOf(main.blacklist.literals);
        this.blacklistRegex = compileRegex(main.blacklist.regex);
        this.nameMinLength = main.name.minLength;
        this.nameMaxLength = main.name.maxLength;
        this.namePattern = Pattern.compile(main.name.pattern);
        this.restoreBatchSize = Math.max(1, main.performance.restoreBatchSize);
        this.ioThreads = Math.max(1, main.performance.ioThreads);
        this.regionGuardPurgeEnabled = main.performance.regionGuardPurgeEnabled;
        this.regionGuardIntervalSeconds = Math.max(1, main.performance.regionGuardIntervalSeconds);
        this.regionGuardBatchSize = Math.max(1, main.performance.regionGuardBatchSize);
        this.positionStep = main.gui.position.step;
        this.adminFallbackRegionId = main.admin.fallbackRegionId;
        this.shadowLowRadius = main.gui.shadowCycle.lowRadius;
        this.shadowLowStrength = main.gui.shadowCycle.lowStrength;
        this.shadowHighRadius = main.gui.shadowCycle.highRadius;
        this.shadowHighStrength = main.gui.shadowCycle.highStrength;
        this.scaleMin = main.gui.scale.min;
        this.scaleMax = main.gui.scale.max;
        this.scaleStep = main.gui.scale.step;
        this.backgroundPresets = readBackgroundPresets(main.gui.backgroundPresets);
        this.guiLayout = GuiLayoutConfig.from(gui);
    }

    private static List<BackgroundPreset> readBackgroundPresets(List<SoulHoloSettings.BackgroundPresetSettings> presets) {
        List<BackgroundPreset> result = new ArrayList<>();
        for (SoulHoloSettings.BackgroundPresetSettings preset : presets) {
            result.add(new BackgroundPreset(
                    preset.id,
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

    public String defaultCreateLine() {
        return defaultCreateLine;
    }

    public double nearestRadius() {
        return nearestRadius;
    }

    public float clickWidth() {
        return clickWidth;
    }

    public float clickHeight() {
        return clickHeight;
    }

    public float clickHeightPerLine() {
        return clickHeightPerLine;
    }

    public boolean loggingEnabled() {
        return loggingEnabled;
    }

    public boolean loggingFile() {
        return loggingFile;
    }

    public int maxLines() {
        return maxLines;
    }

    public int maxLineLength() {
        return maxLineLength;
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

    public boolean regionGuardPurgeEnabled() {
        return regionGuardPurgeEnabled;
    }

    public int regionGuardIntervalSeconds() {
        return regionGuardIntervalSeconds;
    }

    public int regionGuardBatchSize() {
        return regionGuardBatchSize;
    }

    public double positionStep() {
        return positionStep;
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

    public record PlayerLimits(int maxHologramsPerRegion, int maxLines, int maxLineLength) {
    }

    public record BackgroundPreset(String id, int red, int green, int blue, int alpha) {
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
