package bm.b0b0b0.soulHolo.config.settings;

import java.util.ArrayList;
import java.util.List;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

@Comment(@CommentValue("SoulHolo — лимиты, blacklist, GUI. Тексты — messages.yml. Права — plugin.yml и README."))
public final class SoulHoloSettings extends YamlSerializable {

    public SoulHoloSettings() {
        super(SoulHoloSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("auto | paper | decent | fancy — auto и paper: встроенные TextDisplay, без сторонних плагинов"))
    public String hologramBackend = "auto";

    @Comment(@CommentValue("Подпись владельца (%player%) — только пока голограмма на дефолтной заглушке; после своего текста скрывается"))
    public String ownerLine = "&fГолограмма игрока: &a%player%";

    @Comment(@CommentValue("Заглушка при /dholo create; убирается, когда игрок пишет свой текст"))
    public String defaultCreateLine = "&eСтукни меня";

    @NewLine
    @Comment(@CommentValue("Выбор голограммы — клик по невидимому хитбоксу (Interaction)"))
    public SelectionSettings selection = new SelectionSettings();

    @Comment(@CommentValue("Лог действий"))
    public LoggingSettings logging = new LoggingSettings();

    @NewLine
    @Comment({
            @CommentValue("Лимиты голограмм выдаются через число. Группа получает ровно такой лимит."),
            @CommentValue("Пример:"),
            @CommentValue("soulholo.limit.1 = 1 голограмма в его регионе"),
            @CommentValue("soulholo.limit.3 = 3 голограммы в его регионе"),
            @CommentValue("Без soulholo.limit.<N> — создать голограмму нельзя.")
    })
    public LimitsSettings limits = new LimitsSettings();

    @NewLine
    @Comment(@CommentValue("Чёрный список строк"))
    public BlacklistSettings blacklist = new BlacklistSettings();

    @Comment(@CommentValue("Имя голограммы (/dholo create)"))
    public NameSettings name = new NameSettings();

    @NewLine
    @Comment(@CommentValue("Производительность I/O и старта"))
    public PerformanceSettings performance = new PerformanceSettings();

    @Comment(@CommentValue("Админ-команды"))
    public AdminSettings admin = new AdminSettings();

    @NewLine
    @Comment(@CommentValue("GUI — шаг смещения, масштаб, тень, пресеты фона"))
    public GuiSettings gui = new GuiSettings();

    public static final class AdminSettings {
        @Comment(@CommentValue("region-id, если WorldGuard не нашёл регион"))
        public String fallbackRegionId = "global";
    }

    public static final class SelectionSettings {
        @Comment(@CommentValue("Ширина невидимого хитбокса для ЛКМ/ПКМ"))
        public float clickWidth = 2.5f;

        @Comment(@CommentValue("Базовая высота хитбокса"))
        public float clickHeight = 1.0f;

        @Comment(@CommentValue("Доп. высота хитбокса за каждую строку текста"))
        public float clickHeightPerLine = 0.35f;

        @Comment(@CommentValue("Радиус поиска своей голограммы для add/edit (legacy)"))
        public double nearestRadius = 5.0;
    }

    public static final class LoggingSettings {
        public boolean enabled = true;
        public boolean file = true;
    }

    public static final class LimitsSettings {
        @Comment(@CommentValue("owner-region — лимит по голограммам этого игрока в регионе | region — по всем голограммам в регионе"))
        public String countScope = "owner-region";

        @Comment(@CommentValue("Максимум своих строк (без «Стукни меня» и подписи владельца)"))
        public int maxLines = 10;

        @Comment(@CommentValue("Максимальная длина одной строки (символов)"))
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

        @Comment({
                @CommentValue("Проверять, существует ли регион WG. Удалён — голограммы в нём удаляются."),
                @CommentValue("WG поел говна, и не отдаёт event при удалении региона, так что могут быть траблы с оптимизацией.")
        })
        public boolean regionGuardPurgeEnabled = true;

        @Comment(@CommentValue("Интервал батча проверки (сек), если regionGuardPurgeEnabled: true"))
        public int regionGuardIntervalSeconds = 5;

        @Comment(@CommentValue("Уникальных регионов за один батч (не голограмм)"))
        public int regionGuardBatchSize = 100;
    }

    public static final class GuiSettings {
        public PositionSettings position = new PositionSettings();

        public GuiScaleSettings scale = new GuiScaleSettings();

        public ShadowCycleSettings shadowCycle = new ShadowCycleSettings();

        @Comment(@CommentValue("Пресеты цвета фона голограммы: id, red, green, blue, alpha"))
        public List<BackgroundPresetSettings> backgroundPresets = defaultBackgroundPresets();

        private static List<BackgroundPresetSettings> defaultBackgroundPresets() {
            List<BackgroundPresetSettings> list = new ArrayList<>();
            list.add(preset("transparent", 0, 0, 0, 0));
            list.add(preset("dark", 0, 0, 0, 120));
            list.add(preset("white", 255, 255, 255, 100));
            list.add(preset("gray", 70, 70, 70, 115));
            list.add(preset("red", 140, 20, 20, 110));
            list.add(preset("orange", 210, 100, 0, 110));
            list.add(preset("gold", 120, 90, 0, 110));
            list.add(preset("green", 20, 100, 30, 110));
            list.add(preset("lime", 90, 180, 30, 110));
            list.add(preset("cyan", 0, 130, 150, 110));
            list.add(preset("blue", 20, 40, 140, 110));
            list.add(preset("purple", 90, 20, 140, 110));
            list.add(preset("pink", 200, 60, 130, 110));
            list.add(preset("magenta", 170, 0, 170, 110));
            return list;
        }

        private static BackgroundPresetSettings preset(String id, int r, int g, int b, int a) {
            BackgroundPresetSettings preset = new BackgroundPresetSettings();
            preset.id = id;
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

    public static final class BackgroundPresetSettings {
        public String id = "transparent";
        public int red = 0;
        public int green = 0;
        public int blue = 0;
        public int alpha = 0;
    }
}
