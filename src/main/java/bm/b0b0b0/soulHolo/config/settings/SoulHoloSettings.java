package bm.b0b0b0.soulHolo.config.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

@Comment(@CommentValue("SoulHolo — лимиты, blacklist, GUI-права. Тексты — messages.yml"))
public final class SoulHoloSettings extends YamlSerializable {

    public SoulHoloSettings() {
        super(SoulHoloSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("auto | paper | decent | fancy — auto и paper: встроенные TextDisplay, без сторонних плагинов"))
    public String hologramBackend = "auto";

    @Comment(@CommentValue("Последняя строка (%player% = владелец)"))
    public String ownerLine = "&fГолограмма игрока: &a%player%";

    @NewLine
    @Comment(@CommentValue("Выбор активной голограммы"))
    public SelectionSettings selection = new SelectionSettings();

    @Comment(@CommentValue("Лог действий"))
    public LoggingSettings logging = new LoggingSettings();

    @NewLine
    @Comment(@CommentValue("Лимиты по permission-группам"))
    public LimitsSettings limits = new LimitsSettings();

    @NewLine
    @Comment(@CommentValue("Чёрный список строк"))
    public BlacklistSettings blacklist = new BlacklistSettings();

    @Comment(@CommentValue("Имя голограммы (/dholo create)"))
    public NameSettings name = new NameSettings();

    @NewLine
    @Comment(@CommentValue("Производительность I/O и старта"))
    public PerformanceSettings performance = new PerformanceSettings();

    @NewLine
    @Comment(@CommentValue("Permission nodes команд"))
    public PermissionSettings permissions = new PermissionSettings();

    @Comment(@CommentValue("Админ-команды"))
    public AdminSettings admin = new AdminSettings();

    @NewLine
    @Comment(@CommentValue("GUI — права на настройки display"))
    public GuiPermissionSettings gui = new GuiPermissionSettings();

    public static final class PermissionSettings {
        public String use = "soulholo.use";
        public String admin = "soulholo.admin";
    }

    public static final class AdminSettings {
        @Comment(@CommentValue("region-id, если WorldGuard не нашёл регион"))
        public String fallbackRegionId = "global";
    }

    public static final class SelectionSettings {
        @Comment(@CommentValue("Радиус поиска своей голограммы для add/edit"))
        public double nearestRadius = 5.0;
    }

    public static final class LoggingSettings {
        public boolean enabled = true;
        public boolean file = true;
    }

    public static final class LimitsSettings {
        @Comment(@CommentValue("owner-region | region"))
        public String countScope = "owner-region";

        public LimitTierSettings defaultTier = defaultLimit();

        @Comment(@CommentValue("Донат-tier'ы (ключ = id в YAML)"))
        public Map<String, LimitTierSettings> tiers = defaultTiers();

        private static LimitTierSettings defaultLimit() {
            LimitTierSettings tier = new LimitTierSettings();
            tier.permission = "soulholo.limit.default";
            tier.maxHologramsPerRegion = 3;
            tier.maxLines = 10;
            tier.maxLineLength = 50;
            return tier;
        }

        private static Map<String, LimitTierSettings> defaultTiers() {
            Map<String, LimitTierSettings> map = new LinkedHashMap<>();
            LimitTierSettings vip = new LimitTierSettings();
            vip.permission = "soulholo.limit.vip";
            vip.maxHologramsPerRegion = 8;
            vip.maxLines = 15;
            vip.maxLineLength = 80;
            map.put("vip", vip);
            LimitTierSettings premium = new LimitTierSettings();
            premium.permission = "soulholo.limit.premium";
            premium.maxHologramsPerRegion = 15;
            premium.maxLines = 25;
            premium.maxLineLength = 120;
            map.put("premium", premium);
            return map;
        }
    }

    public static final class LimitTierSettings {
        public String permission = "soulholo.limit.default";
        public int maxHologramsPerRegion = 3;
        public int maxLines = 10;
        public int maxLineLength = 50;
    }

    public static final class BlacklistSettings {
        public List<String> literals = defaultLiterals();
        public List<String> regex = defaultRegex();

        private static List<String> defaultLiterals() {
            List<String> list = new ArrayList<>();
            list.add("badword");
            return list;
        }

        private static List<String> defaultRegex() {
            List<String> list = new ArrayList<>();
            list.add("(?i).*advert.*");
            return list;
        }
    }

    public static final class NameSettings {
        public int minLength = 3;
        public int maxLength = 24;
        public String pattern = "[a-zA-Z0-9_-]+";
    }

    public static final class PerformanceSettings {
        public int restoreBatchSize = 20;
        public int ioThreads = 1;
    }

    public static final class GuiPermissionSettings {
        public String openPermission = "soulholo.gui";

        @Comment(@CommentValue("Редактирование строк голограммы в GUI"))
        public String linesPermission = "soulholo.gui.lines";

        @Comment(@CommentValue("Перемещение голограммы в GUI"))
        public String positionPermission = "soulholo.gui.position";

        public PositionSettings position = new PositionSettings();

        public GuiScaleSettings scale = new GuiScaleSettings();

        public ShadowCycleSettings shadowCycle = new ShadowCycleSettings();

        public GuiSettingPermissions settings = new GuiSettingPermissions();

        @Comment(@CommentValue("Пресеты фона (FancyHolograms)"))
        public List<BackgroundPresetSettings> backgroundPresets = defaultBackgroundPresets();

        private static List<BackgroundPresetSettings> defaultBackgroundPresets() {
            List<BackgroundPresetSettings> list = new ArrayList<>();
            list.add(preset("transparent", "soulholo.gui.bg.transparent", 0, 0, 0, 0));
            list.add(preset("dark", "soulholo.gui.bg.dark", 0, 0, 0, 120));
            list.add(preset("blue", "soulholo.gui.bg.blue", 0, 0, 80, 100));
            list.add(preset("gold", "soulholo.gui.bg.gold", 120, 90, 0, 110));
            return list;
        }

        private static BackgroundPresetSettings preset(String id, String permission, int r, int g, int b, int a) {
            BackgroundPresetSettings preset = new BackgroundPresetSettings();
            preset.id = id;
            preset.permission = permission;
            preset.red = r;
            preset.green = g;
            preset.blue = b;
            preset.alpha = a;
            return preset;
        }
    }

    public static final class PositionSettings {
        @Comment(@CommentValue("Шаг смещения за один клик (блоки)"))
        public double step = 0.25;
    }

    public static final class GuiScaleSettings {
        public float min = 0.5f;
        public float max = 2.5f;
        public float step = 0.1f;
    }

    public static final class ShadowCycleSettings {
        public float lowRadius = 0.5f;
        public float lowStrength = 0.5f;
        public float highRadius = 1.0f;
        public float highStrength = 1.0f;
    }

    public static final class GuiSettingPermissions {
        public String enabled = "soulholo.gui.setting.enabled";
        public String seeThrough = "soulholo.gui.setting.see-through";
        public String textShadow = "soulholo.gui.setting.text-shadow";
        public String billboard = "soulholo.gui.setting.billboard";
        public String background = "soulholo.gui.setting.background";
        public String scale = "soulholo.gui.setting.scale";
        public String textAlignment = "soulholo.gui.setting.text-alignment";
        public String shadow = "soulholo.gui.setting.shadow";
    }

    public static final class BackgroundPresetSettings {
        public String id = "transparent";
        public String permission = "soulholo.gui.bg.transparent";
        public int red = 0;
        public int green = 0;
        public int blue = 0;
        public int alpha = 0;
    }
}
