package bm.b0b0b0.soulHolo.gui;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.service.DisplaySettingAccess;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramDisplayService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HologramSettingsMenu implements SoulHoloGuiHolder {

    private final Player viewer;
    private final PrivateHologram hologram;
    private final MessageService messages;
    private final GuiItemFactory items;
    private final GuiLayoutConfig.ScreenLayout layout;
    private final DisplaySettingAccess access;
    private final HologramDisplayService displayService;
    private final GuiNavigationService navigation;
    private final Inventory inventory;

    public HologramSettingsMenu(Player viewer,
                                PrivateHologram hologram,
                                MessageService messages,
                                GuiItemFactory items,
                                GuiLayoutConfig.ScreenLayout layout,
                                DisplaySettingAccess access,
                                HologramDisplayService displayService,
                                GuiNavigationService navigation) {
        this.viewer = viewer;
        this.hologram = hologram;
        this.messages = messages;
        this.items = items;
        this.layout = layout;
        this.access = access;
        this.displayService = displayService;
        this.navigation = navigation;
        this.inventory = Bukkit.createInventory(this, layout.size(), messages.component(
                layout.titleKey(),
                Map.of("name", hologram.name())
        ));
        redraw();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        viewer.openInventory(inventory);
    }

    @Override
    public void handleInventoryClick(Player player, int slot, ClickType clickType) {
        handleClick(slot, clickType);
    }

    public void handleClick(int slot, ClickType clickType) {
        Integer backSlot = layout.slots().get("back");
        Integer closeSlot = layout.slots().get("close");
        if (backSlot != null && slot == backSlot) {
            navigation.openList(viewer, 0);
            return;
        }
        if (closeSlot != null && slot == closeSlot) {
            viewer.closeInventory();
            return;
        }
        if (matchesSlot("lines", slot)) {
            if (access.canEditLines(viewer)) {
                navigation.openLines(viewer, hologram, 0);
            }
            return;
        }
        if (matchesSlot("position", slot)) {
            if (access.canMovePosition(viewer)) {
                navigation.openPosition(viewer, hologram);
            }
            return;
        }
        if (matchesSlot("enabled", slot)) {
            if (displayService.toggleBoolean(viewer, hologram, DisplaySettingKey.ENABLED)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("see-through", slot)) {
            if (displayService.toggleBoolean(viewer, hologram, DisplaySettingKey.SEE_THROUGH)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("text-shadow", slot)) {
            if (displayService.toggleBoolean(viewer, hologram, DisplaySettingKey.TEXT_SHADOW)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("billboard", slot)) {
            if (displayService.cycleBillboard(viewer, hologram)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("background", slot)) {
            if (displayService.cycleBackground(viewer, hologram)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("scale-down", slot) && (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT)) {
            if (displayService.adjustScale(viewer, hologram, false)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("scale-up", slot) && (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT)) {
            if (displayService.adjustScale(viewer, hologram, true)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("alignment", slot)) {
            if (displayService.cycleAlignment(viewer, hologram)) {
                redraw();
            }
            return;
        }
        if (matchesSlot("shadow", slot)) {
            if (displayService.cycleShadow(viewer, hologram)) {
                redraw();
            }
        }
    }

    private boolean matchesSlot(String key, int slot) {
        Integer configured = layout.slots().get(key);
        return configured != null && configured == slot;
    }

    private void redraw() {
        inventory.clear();
        fillBackground();
        placeLinesButton();
        placePositionButton();
        HologramDisplaySettings settings = hologram.displaySettings();
        placeToggle("enabled", DisplaySettingKey.ENABLED, "enabled-on", "enabled-off",
                settings.enabled(), settings.enabled());
        placeToggle("see-through", DisplaySettingKey.SEE_THROUGH, "see-through-on", "see-through-off",
                settings.seeThrough(), settings.seeThrough());
        placeToggle("text-shadow", DisplaySettingKey.TEXT_SHADOW, "text-shadow-on", "text-shadow-off",
                settings.textShadow(), settings.textShadow());
        placeValue("billboard", DisplaySettingKey.BILLBOARD, "billboard",
                Map.of("value", enumLabel("billboard", settings.billboard().name())));
        placeValue("background", DisplaySettingKey.BACKGROUND, "background",
                Map.of("value", backgroundLabel(settings.backgroundPreset())));
        placeValue("scale-down", DisplaySettingKey.SCALE, "scale",
                Map.of("value", String.format(Locale.US, "%.1f", settings.scale())));
        placeValue("scale-up", DisplaySettingKey.SCALE, "scale",
                Map.of("value", String.format(Locale.US, "%.1f", settings.scale())));
        placeValue("alignment", DisplaySettingKey.TEXT_ALIGNMENT, "alignment",
                Map.of("value", enumLabel("alignment", settings.textAlignment().name())));
        placeValue("shadow", DisplaySettingKey.SHADOW, "shadow", Map.of(
                "radius", String.format(Locale.US, "%.1f", settings.shadowRadius()),
                "strength", String.format(Locale.US, "%.1f", settings.shadowStrength())
        ));
        if (layout.slots().containsKey("back")) {
            inventory.setItem(layout.slots().get("back"), items.button(
                    layout.material("back"),
                    "gui.settings.back.name",
                    null,
                    Map.of()
            ));
        }
        if (layout.slots().containsKey("close")) {
            inventory.setItem(layout.slots().get("close"), items.button(
                    layout.material("close"),
                    "gui.settings.close.name",
                    null,
                    Map.of()
            ));
        }
    }

    private void placeToggle(String slotKey, DisplaySettingKey settingKey, String materialOn, String materialOff,
                             boolean enabledState, boolean displayOn) {
        Integer slot = layout.slots().get(slotKey);
        if (slot == null) {
            return;
        }
        if (!access.canChange(viewer, settingKey) || !access.isSupported(settingKey)) {
            inventory.setItem(slot, lockedItem(settingKey));
            return;
        }
        Material material = layout.material(displayOn ? materialOn : materialOff);
        inventory.setItem(slot, items.toggle(
                material,
                "gui.settings." + access.settingMessageKey(settingKey) + ".name",
                "gui.settings." + access.settingMessageKey(settingKey) + ".lore-on",
                "gui.settings." + access.settingMessageKey(settingKey) + ".lore-off",
                enabledState,
                Map.of()
        ));
    }

    private void placeValue(String slotKey, DisplaySettingKey settingKey, String messageKey, Map<String, String> values) {
        Integer slot = layout.slots().get(slotKey);
        if (slot == null) {
            return;
        }
        if (!access.canChange(viewer, settingKey) || !access.isSupported(settingKey)) {
            inventory.setItem(slot, lockedItem(settingKey));
            return;
        }
        Material material = layout.material(messageKey);
        inventory.setItem(slot, items.button(
                material,
                "gui.settings." + messageKey + ".name",
                "gui.settings." + messageKey + ".lore",
                values
        ));
    }

    private void placePositionButton() {
        Integer slot = layout.slots().get("position");
        if (slot == null) {
            return;
        }
        Map<String, String> placeholders = positionPlaceholders();
        if (!access.canMovePosition(viewer)) {
            inventory.setItem(slot, positionLockedItem(placeholders));
            return;
        }
        inventory.setItem(slot, items.button(
                layout.material("position"),
                "gui.settings.position.name",
                "gui.settings.position.lore",
                placeholders
        ));
    }

    private Map<String, String> positionPlaceholders() {
        org.bukkit.Location location = hologram.location();
        String x = location == null ? "?" : String.format(Locale.US, "%.1f", location.getX());
        String y = location == null ? "?" : String.format(Locale.US, "%.1f", location.getY());
        String z = location == null ? "?" : String.format(Locale.US, "%.1f", location.getZ());
        return Map.of(
                "region", hologram.regionId(),
                "x", x,
                "y", y,
                "z", z
        );
    }

    private ItemStack positionLockedItem(Map<String, String> placeholders) {
        Material material = layout.material("locked");
        List<String> lore = new ArrayList<>(messages.lore("gui.settings.locked.lore", Map.of()));
        ItemStack stack = items.button(
                material,
                "gui.settings.position.name",
                null,
                placeholders
        );
        var meta = stack.getItemMeta();
        List<net.kyori.adventure.text.Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(messages.legacyComponent(line));
        }
        meta.lore(components);
        stack.setItemMeta(meta);
        return stack;
    }

    private void placeLinesButton() {
        Integer slot = layout.slots().get("lines");
        if (slot == null) {
            return;
        }
        if (!access.canEditLines(viewer)) {
            inventory.setItem(slot, linesLockedItem());
            return;
        }
        inventory.setItem(slot, items.button(
                layout.material("lines"),
                "gui.settings.lines.name",
                "gui.settings.lines.lore",
                Map.of(
                        "count", String.valueOf(hologram.lines().size()),
                        "max", String.valueOf(hologramServiceMaxLines())
                )
        ));
    }

    private int hologramServiceMaxLines() {
        return navigation.maxLinesFor(viewer);
    }

    private ItemStack linesLockedItem() {
        Material material = layout.material("locked");
        List<String> lore = new ArrayList<>(messages.lore("gui.settings.locked.lore", Map.of()));
        ItemStack stack = items.button(
                material,
                "gui.settings.lines.name",
                null,
                Map.of(
                        "count", String.valueOf(hologram.lines().size()),
                        "max", String.valueOf(hologramServiceMaxLines())
                )
        );
        var meta = stack.getItemMeta();
        List<net.kyori.adventure.text.Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(messages.legacyComponent(line));
        }
        meta.lore(components);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack lockedItem(DisplaySettingKey settingKey) {
        Material material = layout.material("locked");
        List<String> lore = new ArrayList<>(messages.lore("gui.settings.locked.lore", Map.of()));
        if (!access.isSupported(settingKey)) {
            lore.addAll(messages.lore("gui.settings.unsupported.lore", Map.of()));
        }
        ItemStack stack = items.button(
                material,
                "gui.settings." + access.settingMessageKey(settingKey) + ".name",
                null,
                Map.of()
        );
        var meta = stack.getItemMeta();
        List<net.kyori.adventure.text.Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(messages.legacyComponent(line));
        }
        meta.lore(components);
        stack.setItemMeta(meta);
        return stack;
    }

    private void fillBackground() {
        Material filler = layout.material("filler");
        ItemStack pane = items.filler(filler);
        for (int slot = 0; slot < layout.size(); slot++) {
            inventory.setItem(slot, pane);
        }
    }

    private String enumLabel(String group, String value) {
        String key = "gui.enum." + group + "." + value;
        String resolved = messages.plain(key, Map.of());
        return resolved.equals(key) ? value : resolved;
    }

    private String backgroundLabel(String presetId) {
        String key = "gui.enum.background." + presetId;
        String resolved = messages.plain(key, Map.of());
        return resolved.equals(key) ? presetId : resolved;
    }
}
