package bm.b0b0b0.soulHolo.gui;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramPositionGuiService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class HologramPositionMenu implements SoulHoloGuiHolder {

    private final Player viewer;
    private final PrivateHologram hologram;
    private final MessageService messages;
    private final GuiItemFactory items;
    private final GuiLayoutConfig.ScreenLayout layout;
    private final GuiNavigationService navigation;
    private final HologramPositionGuiService positionService;
    private final float referenceYaw;
    private final Inventory inventory;

    public HologramPositionMenu(Player viewer,
                                PrivateHologram hologram,
                                MessageService messages,
                                GuiItemFactory items,
                                GuiLayoutConfig.ScreenLayout layout,
                                GuiNavigationService navigation,
                                HologramPositionGuiService positionService) {
        this.viewer = viewer;
        this.hologram = hologram;
        this.messages = messages;
        this.items = items;
        this.layout = layout;
        this.navigation = navigation;
        this.positionService = positionService;
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
        Integer closeSlot = layout.slots().get("close");
        if (backSlot != null && slot == backSlot) {
            navigation.openSettings(viewer, hologram);
            return;
        }
        if (closeSlot != null && slot == closeSlot) {
            viewer.closeInventory();
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
        if (layout.slots().containsKey("close")) {
            inventory.setItem(layout.slots().get("close"), items.button(
                    layout.material("close"),
                    "gui.position.close.name",
                    null,
                    Map.of()
            ));
        }
    }

    private void placeDirection(String slotKey, String nameKey, String loreKey, Map<String, String> placeholders) {
        Integer slot = layout.slots().get(slotKey);
        if (slot == null) {
            return;
        }
        inventory.setItem(slot, items.button(
                layout.material(slotKey),
                nameKey,
                loreKey,
                placeholders
        ));
    }

    private void fillBackground() {
        Material filler = layout.material("filler");
        ItemStack pane = items.filler(filler);
        for (int slot = 0; slot < layout.size(); slot++) {
            inventory.setItem(slot, pane);
        }
    }

    private Map<String, String> titlePlaceholders() {
        return Map.of(
                "name", hologram.name(),
                "region", hologram.regionId()
        );
    }
}
