package bm.b0b0b0.soulHolo.gui;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.HologramDisplaySettings;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import bm.b0b0b0.soulHolo.service.DisplaySettingAccess;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramDisplayService;
import bm.b0b0b0.soulHolo.service.HologramPositionGuiService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HologramPositionMenu implements SoulHoloGuiHolder {

    private final Player viewer;
    private final PrivateHologram hologram;
    private final MessageService messages;
    private final GuiItemFactory items;
    private final GuiLayoutConfig.ScreenLayout layout;
    private final GuiNavigationService navigation;
    private final HologramPositionGuiService positionService;
    private final HologramDisplayService displayService;
    private final DisplaySettingAccess access;
    private final float referenceYaw;
    private final Inventory inventory;

    public HologramPositionMenu(Player viewer,
                                PrivateHologram hologram,
                                MessageService messages,
                                GuiItemFactory items,
                                GuiLayoutConfig.ScreenLayout layout,
                                GuiNavigationService navigation,
                                HologramPositionGuiService positionService,
                                HologramDisplayService displayService,
                                DisplaySettingAccess access) {
        this.viewer = viewer;
        this.hologram = hologram;
        this.messages = messages;
        this.items = items;
        this.layout = layout;
        this.navigation = navigation;
        this.positionService = positionService;
        this.displayService = displayService;
        this.access = access;
        this.referenceYaw = viewer.getLocation().getYaw();
        this.inventory = Bukkit.createInventory(this, layout.size(), messages.component(
                layout.titleKey(),
                titlePlaceholders()
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
        handleClick(slot);
    }

    public void handleClick(int slot) {
        Integer backSlot = layout.slots().get("back");
        if (backSlot != null && slot == backSlot) {
            navigation.openSettings(viewer, hologram);
            return;
        }
        if (matchesSlot("billboard", slot)) {
            if (displayService.cycleBillboard(viewer, hologram)) {
                redraw();
            }
            return;
        }
        RelativeMoveDirection direction = directionAtSlot(slot);
        if (direction == null) {
            return;
        }
        if (positionService.move(viewer, hologram, referenceYaw, direction)) {
            redraw();
        }
    }

    private RelativeMoveDirection directionAtSlot(int slot) {
        if (matchesSlot("up", slot)) {
            return RelativeMoveDirection.UP;
        }
        if (matchesSlot("down", slot)) {
            return RelativeMoveDirection.DOWN;
        }
        if (matchesSlot("left", slot)) {
            return RelativeMoveDirection.LEFT;
        }
        if (matchesSlot("right", slot)) {
            return RelativeMoveDirection.RIGHT;
        }
        return null;
    }

    private boolean matchesSlot(String key, int slot) {
        Integer configured = layout.slots().get(key);
        return configured != null && configured == slot;
    }

    private void redraw() {
        inventory.clear();
        fillBackground();
        Location location = hologram.location();
        String x = location == null ? "?" : positionService.formatCoordinate(location.getX());
        String y = location == null ? "?" : positionService.formatCoordinate(location.getY());
        String z = location == null ? "?" : positionService.formatCoordinate(location.getZ());
        Map<String, String> coords = Map.of(
                "x", x,
                "y", y,
                "z", z,
                "region", hologram.regionId(),
                "step", positionService.formatCoordinate(positionService.step())
        );
        placeBillboardButton();
        if (layout.slots().containsKey("center")) {
            inventory.setItem(layout.slots().get("center"), items.button(
                    layout.material("center"),
                    "gui.position.center.name",
                    "gui.position.center.lore",
                    coords
            ));
        }
        placeDirection("up", "gui.position.up.name", "gui.position.up.lore", coords);
        placeDirection("down", "gui.position.down.name", "gui.position.down.lore", coords);
        placeDirection("left", "gui.position.left.name", "gui.position.left.lore", coords);
        placeDirection("right", "gui.position.right.name", "gui.position.right.lore", coords);
        if (layout.slots().containsKey("back")) {
            inventory.setItem(layout.slots().get("back"), items.button(
                    layout.material("back"),
                    "gui.position.back.name",
                    null,
                    Map.of()
            ));
        }
    }

    private void placeBillboardButton() {
        Integer slot = layout.slots().get("billboard");
        if (slot == null) {
            return;
        }
        HologramDisplaySettings settings = hologram.displaySettings();
        Map<String, String> placeholders = Map.of(
                "value", enumLabel("billboard", settings.billboard().name())
        );
        if (!access.canChange(viewer, DisplaySettingKey.BILLBOARD) || !access.isSupported(DisplaySettingKey.BILLBOARD)) {
            inventory.setItem(slot, lockedBillboardItem(placeholders));
            return;
        }
        inventory.setItem(slot, items.button(
                layout.material("billboard"),
                "gui.position.billboard.name",
                "gui.position.billboard.lore",
                placeholders
        ));
    }

    private ItemStack lockedBillboardItem(Map<String, String> placeholders) {
        Material material = layout.material("locked");
        List<String> lore = new ArrayList<>(messages.lore("gui.settings.locked.lore", Map.of()));
        if (!access.isSupported(DisplaySettingKey.BILLBOARD)) {
            lore.addAll(messages.lore("gui.settings.unsupported.lore", Map.of()));
        }
        ItemStack stack = items.button(
                material,
                "gui.position.billboard.name",
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

    private void placeDirection(String slotKey, String nameKey, String loreKey, Map<String, String> placeholders) {
        Integer slot = layout.slots().get(slotKey);
        if (slot == null) {
            return;
        }
        if (!positionService.canMove(viewer)) {
            inventory.setItem(slot, lockedDirectionItem(nameKey, loreKey, placeholders));
            return;
        }
        inventory.setItem(slot, items.button(
                layout.material(slotKey),
                nameKey,
                loreKey,
                placeholders
        ));
    }

    private ItemStack lockedDirectionItem(String nameKey, String loreKey, Map<String, String> placeholders) {
        Material material = layout.material("locked");
        List<String> lore = new ArrayList<>(messages.lore("gui.settings.locked.lore", Map.of()));
        ItemStack stack = items.button(
                material,
                nameKey,
                loreKey,
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

    private Map<String, String> titlePlaceholders() {
        return Map.of(
                "name", hologram.name(),
                "region", hologram.regionId()
        );
    }
}
