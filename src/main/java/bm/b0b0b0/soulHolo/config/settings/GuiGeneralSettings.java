package bm.b0b0b0.soulHolo.config.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

@Comment(@CommentValue("SoulHolo — слоты и материалы GUI. Тексты — messages.yml"))
public final class GuiGeneralSettings extends YamlSerializable {

    public GuiGeneralSettings() {
        super(SoulHoloSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Список голограмм игрока"))
    public ListScreenSettings list = ListScreenSettings.defaults();

    @NewLine
    @Comment(@CommentValue("Настройки display одной голограммы"))
    public SettingsScreenSettings settings = SettingsScreenSettings.defaults();

    @NewLine
    @Comment(@CommentValue("Редактирование строк голограммы"))
    public LinesScreenSettings lines = LinesScreenSettings.defaults();

    @NewLine
    @Comment(@CommentValue("Перемещение голограммы в пределах региона"))
    public PositionScreenSettings position = PositionScreenSettings.defaults();

    @Comment(@CommentValue("Материалы по умолчанию, если ключ не задан в materials"))
    public MaterialFallbackSettings materialFallbacks = new MaterialFallbackSettings();

    public static final class MaterialFallbackSettings {
        public String entry = "PAINTING";
        public String filler = "GRAY_STAINED_GLASS_PANE";
        public String button = "LIGHT_GRAY_DYE";
        public String locked = "BARRIER";
    }

    public static final class ListScreenSettings {
        public int size = 54;
        @Comment(@CommentValue("Ключ title в messages.yml"))
        public String title = "gui.list.title";
        public ListScreenSlotsSettings slots = new ListScreenSlotsSettings();
        public Map<String, String> materials = defaultMaterials();

        public static ListScreenSettings defaults() {
            return new ListScreenSettings();
        }

        private static Map<String, String> defaultMaterials() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("entry", "PAINTING");
            map.put("previous", "ARROW");
            map.put("next", "ARROW");
            map.put("filler", "GRAY_STAINED_GLASS_PANE");
            map.put("empty", "STRUCTURE_VOID");
            return map;
        }
    }

    public static final class ListScreenSlotsSettings {
        public List<Integer> content = defaultContent();
        public int previous = 45;
        public int next = 53;
        public int empty = 22;
        public int filler = 40;

        private static List<Integer> defaultContent() {
            List<Integer> slots = new ArrayList<>();
            slots.add(10);
            slots.add(11);
            slots.add(12);
            slots.add(13);
            slots.add(14);
            slots.add(15);
            slots.add(16);
            slots.add(19);
            slots.add(20);
            slots.add(21);
            slots.add(22);
            slots.add(23);
            slots.add(24);
            slots.add(25);
            slots.add(28);
            slots.add(29);
            slots.add(30);
            slots.add(31);
            slots.add(32);
            slots.add(33);
            slots.add(34);
            return slots;
        }
    }

    public static final class SettingsScreenSettings {
        public int size = 45;
        public String title = "gui.settings.title";
        public SettingsScreenSlotsSettings slots = new SettingsScreenSlotsSettings();
        public Map<String, String> materials = defaultMaterials();

        public static SettingsScreenSettings defaults() {
            return new SettingsScreenSettings();
        }

        private static Map<String, String> defaultMaterials() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("enabled-on", "LIME_DYE");
            map.put("lines", "LIME_DYE");
            map.put("position", "ENDER_EYE");
            map.put("enabled-off", "RED_DYE");
            map.put("see-through-on", "CYAN_DYE");
            map.put("see-through-off", "GRAY_DYE");
            map.put("text-shadow-on", "BLACK_DYE");
            map.put("text-shadow-off", "LIGHT_GRAY_DYE");
            map.put("background", "PAINTING");
            map.put("scale-down", "IRON_NUGGET");
            map.put("scale-up", "GOLD_NUGGET");
            map.put("alignment", "OAK_SIGN");
            map.put("shadow", "SOUL_LANTERN");
            map.put("delete", "RED_DYE");
            map.put("back", "LIGHT_GRAY_DYE");
            map.put("locked", "BARRIER");
            map.put("filler", "GRAY_STAINED_GLASS_PANE");
            return map;
        }
    }

    public static final class SettingsScreenSlotsSettings {
        public int lines = 4;
        public int position = 6;
        public int enabled = 10;
        public int seeThrough = 12;
        public int textShadow = 14;
        public int background = 20;
        public int scaleDown = 22;
        public int scaleUp = 24;
        public int alignment = 30;
        public int shadow = 32;
        public int delete = 19;
        public int back = 36;
        public int filler = 40;
    }

    public static final class LinesScreenSettings {
        public int size = 54;
        public String title = "gui.lines.title";
        public int confirmSize = 27;
        public String confirmTitle = "gui.lines.delete-confirm.title";
        public LinesScreenSlotsSettings slots = new LinesScreenSlotsSettings();
        public LinesConfirmSlotsSettings confirm = new LinesConfirmSlotsSettings();
        public Map<String, String> materials = defaultMaterials();

        public static LinesScreenSettings defaults() {
            return new LinesScreenSettings();
        }

        private static Map<String, String> defaultMaterials() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("entry", "LIME_DYE");
            map.put("entry-hidden", "PURPLE_DYE");
            map.put("empty", "GRAY_DYE");
            map.put("confirm-yes", "LIME_DYE");
            map.put("confirm-no", "RED_DYE");
            map.put("previous", "LIGHT_GRAY_DYE");
            map.put("next", "LIGHT_GRAY_DYE");
            map.put("back", "LIGHT_GRAY_DYE");
            map.put("reserved-shown", "LIME_DYE");
            map.put("reserved-hidden", "GRAY_DYE");
            map.put("filler", "GRAY_STAINED_GLASS_PANE");
            return map;
        }
    }

    public static final class LinesConfirmSlotsSettings {
        public int info = 13;
        public int yes = 11;
        public int no = 15;
    }

    public static final class LinesScreenSlotsSettings {
        public int hintToggle = 3;
        public int ownerToggle = 4;
        public List<Integer> content = defaultContent();
        public int previous = 45;
        public int next = 53;
        public int back = 36;

        private static List<Integer> defaultContent() {
            List<Integer> slots = new ArrayList<>();
            slots.add(10);
            slots.add(11);
            slots.add(12);
            slots.add(13);
            slots.add(14);
            slots.add(15);
            slots.add(16);
            slots.add(19);
            slots.add(20);
            slots.add(21);
            slots.add(22);
            slots.add(23);
            slots.add(24);
            slots.add(25);
            slots.add(28);
            slots.add(29);
            slots.add(30);
            slots.add(31);
            slots.add(32);
            slots.add(33);
            slots.add(34);
            return slots;
        }
    }

    public static final class PositionScreenSettings {
        public int size = 45;
        public String title = "gui.position.title";
        public PositionScreenSlotsSettings slots = new PositionScreenSlotsSettings();
        public Map<String, String> materials = defaultMaterials();

        public static PositionScreenSettings defaults() {
            return new PositionScreenSettings();
        }

        private static Map<String, String> defaultMaterials() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("center", "RECOVERY_COMPASS");
            map.put("up", "FEATHER");
            map.put("down", "ANVIL");
            map.put("left", "ARROW");
            map.put("right", "SPECTRAL_ARROW");
            map.put("billboard", "END_CRYSTAL");
            map.put("back", "LIGHT_GRAY_DYE");
            map.put("filler", "GRAY_STAINED_GLASS_PANE");
            return map;
        }
    }

    public static final class PositionScreenSlotsSettings {
        public int up = 13;
        public int down = 31;
        public int left = 21;
        public int right = 23;
        public int center = 22;
        public int billboard = 4;
        public int back = 36;
    }
}
