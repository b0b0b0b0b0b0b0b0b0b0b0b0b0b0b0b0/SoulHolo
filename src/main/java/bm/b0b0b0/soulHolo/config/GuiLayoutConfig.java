package bm.b0b0b0.soulHolo.config;

import bm.b0b0b0.soulHolo.config.settings.GuiGeneralSettings;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuiLayoutConfig {

    private final ScreenLayout list;
    private final ScreenLayout settings;
    private final ScreenLayout lines;
    private final ScreenLayout position;

    public GuiLayoutConfig(ScreenLayout list,
                           ScreenLayout settings,
                           ScreenLayout lines,
                           ScreenLayout position) {
        this.list = list;
        this.settings = settings;
        this.lines = lines;
        this.position = position;
    }

    public static GuiLayoutConfig from(GuiGeneralSettings gui) {
        Map<String, Material> fallbacks = parseFallbackMaterials(gui.materialFallbacks);
        return new GuiLayoutConfig(
                buildListScreen(gui.list, fallbacks),
                buildSettingsScreen(gui.settings, fallbacks),
                buildLinesScreen(gui.lines, fallbacks),
                buildPositionScreen(gui.position, fallbacks)
        );
    }

    public ScreenLayout list() {
        return list;
    }

    public ScreenLayout settings() {
        return settings;
    }

    public ScreenLayout lines() {
        return lines;
    }

    public ScreenLayout position() {
        return position;
    }

    public record ScreenLayout(
            int size,
            String titleKey,
            List<Integer> contentSlots,
            Map<String, Integer> slots,
            Map<String, Material> materials,
            Map<String, Material> fallbacks
    ) {
        public Material material(String key) {
            Material configured = materials.get(key);
            if (configured != null) {
                return configured;
            }
            Material fallback = fallbacks.get(key);
            if (fallback != null) {
                return fallback;
            }
            Material button = fallbacks.get("button");
            return button == null ? Material.PAPER : button;
        }
    }

    private static ScreenLayout buildListScreen(GuiGeneralSettings.ListScreenSettings screen,
                                                Map<String, Material> fallbacks) {
        Map<String, Integer> slots = new HashMap<>();
        slots.put("previous", screen.slots.previous);
        slots.put("next", screen.slots.next);
        slots.put("close", screen.slots.close);
        slots.put("filler", screen.slots.filler);
        return new ScreenLayout(
                screen.size,
                screen.title,
                List.copyOf(screen.slots.content),
                Map.copyOf(slots),
                parseMaterials(screen.materials),
                fallbacks
        );
    }

    private static ScreenLayout buildSettingsScreen(GuiGeneralSettings.SettingsScreenSettings screen,
                                                    Map<String, Material> fallbacks) {
        Map<String, Integer> slots = new HashMap<>();
        GuiGeneralSettings.SettingsScreenSlotsSettings raw = screen.slots;
        slots.put("lines", raw.lines);
        slots.put("position", raw.position);
        slots.put("enabled", raw.enabled);
        slots.put("see-through", raw.seeThrough);
        slots.put("text-shadow", raw.textShadow);
        slots.put("billboard", raw.billboard);
        slots.put("background", raw.background);
        slots.put("scale-down", raw.scaleDown);
        slots.put("scale-up", raw.scaleUp);
        slots.put("alignment", raw.alignment);
        slots.put("shadow", raw.shadow);
        slots.put("back", raw.back);
        slots.put("close", raw.close);
        slots.put("filler", raw.filler);
        return new ScreenLayout(
                screen.size,
                screen.title,
                List.of(),
                Map.copyOf(slots),
                parseMaterials(screen.materials),
                fallbacks
        );
    }

    private static ScreenLayout buildLinesScreen(GuiGeneralSettings.LinesScreenSettings screen,
                                                 Map<String, Material> fallbacks) {
        Map<String, Integer> slots = new HashMap<>();
        GuiGeneralSettings.LinesScreenSlotsSettings raw = screen.slots;
        slots.put("add", raw.add);
        slots.put("previous", raw.previous);
        slots.put("next", raw.next);
        slots.put("back", raw.back);
        slots.put("close", raw.close);
        return new ScreenLayout(
                screen.size,
                screen.title,
                List.copyOf(raw.content),
                Map.copyOf(slots),
                parseMaterials(screen.materials),
                fallbacks
        );
    }

    private static ScreenLayout buildPositionScreen(GuiGeneralSettings.PositionScreenSettings screen,
                                                    Map<String, Material> fallbacks) {
        Map<String, Integer> slots = new HashMap<>();
        GuiGeneralSettings.PositionScreenSlotsSettings raw = screen.slots;
        slots.put("up", raw.up);
        slots.put("down", raw.down);
        slots.put("left", raw.left);
        slots.put("right", raw.right);
        slots.put("center", raw.center);
        slots.put("back", raw.back);
        slots.put("close", raw.close);
        return new ScreenLayout(
                screen.size,
                screen.title,
                List.of(),
                Map.copyOf(slots),
                parseMaterials(screen.materials),
                fallbacks
        );
    }

    private static Map<String, Material> parseFallbackMaterials(GuiGeneralSettings.MaterialFallbackSettings raw) {
        Map<String, Material> materials = new HashMap<>();
        putMaterial(materials, "entry", raw.entry);
        putMaterial(materials, "filler", raw.filler);
        putMaterial(materials, "button", raw.button);
        putMaterial(materials, "locked", raw.locked);
        return Map.copyOf(materials);
    }

    private static Map<String, Material> parseMaterials(Map<String, String> raw) {
        Map<String, Material> materials = new HashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            putMaterial(materials, entry.getKey(), entry.getValue());
        }
        return Map.copyOf(materials);
    }

    private static void putMaterial(Map<String, Material> materials, String key, String raw) {
        Material material = Material.matchMaterial(raw);
        if (material != null) {
            materials.put(key, material);
        }
    }
}
