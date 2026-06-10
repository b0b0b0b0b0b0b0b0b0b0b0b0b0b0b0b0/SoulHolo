package bm.b0b0b0.soulHolo.gui;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HologramListMenu implements SoulHoloGuiHolder {

    private final Player viewer;
    private final MessageService messages;
    private final GuiItemFactory items;
    private final GuiLayoutConfig.ScreenLayout layout;
    private final GuiNavigationService navigation;
    private final List<PrivateHologram> holograms;
    private final Inventory inventory;
    private int page;

    public HologramListMenu(Player viewer,
                            MessageService messages,
                            GuiItemFactory items,
                            GuiLayoutConfig.ScreenLayout layout,
                            GuiNavigationService navigation,
                            List<PrivateHologram> holograms,
                            int page) {
        this.viewer = viewer;
        this.messages = messages;
        this.items = items;
        this.layout = layout;
        this.navigation = navigation;
        this.holograms = holograms;
        this.page = Math.max(0, page);
        this.inventory = Bukkit.createInventory(this, layout.size(),
                messages.component(layout.titleKey(), titlePlaceholders()));
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
        Integer previousSlot = layout.slots().get("previous");
        Integer nextSlot = layout.slots().get("next");
        Integer closeSlot = layout.slots().get("close");
        if (closeSlot != null && slot == closeSlot) {
            viewer.closeInventory();
            return;
        }
        if (previousSlot != null && slot == previousSlot && page > 0) {
            page--;
            redraw();
            return;
        }
        if (nextSlot != null && slot == nextSlot && page < totalPages() - 1) {
            page++;
            redraw();
            return;
        }
        PrivateHologram selected = hologramAtSlot(slot);
        if (selected != null) {
            navigation.openSettings(viewer, selected);
        }
    }

    private PrivateHologram hologramAtSlot(int slot) {
        List<Integer> contentSlots = layout.contentSlots();
        int pageSize = contentSlots.size();
        int start = page * pageSize;
        for (int index = 0; index < contentSlots.size(); index++) {
            if (contentSlots.get(index) != slot) {
                continue;
            }
            int hologramIndex = start + index;
            if (hologramIndex >= holograms.size()) {
                return null;
            }
            return holograms.get(hologramIndex);
        }
        return null;
    }

    private void redraw() {
        inventory.clear();
        fillBackground();
        List<Integer> contentSlots = layout.contentSlots();
        int pageSize = contentSlots.size();
        int start = page * pageSize;
        Material entryMaterial = layout.material("entry");
        for (int index = 0; index < contentSlots.size(); index++) {
            int hologramIndex = start + index;
            if (hologramIndex >= holograms.size()) {
                break;
            }
            PrivateHologram hologram = holograms.get(hologramIndex);
            inventory.setItem(contentSlots.get(index), entryItem(entryMaterial, hologram));
        }
        if (layout.slots().containsKey("previous") && page > 0) {
            inventory.setItem(layout.slots().get("previous"), items.button(
                    layout.material("previous"),
                    "gui.list.previous.name",
                    null,
                    Map.of()
            ));
        }
        if (layout.slots().containsKey("next") && page < totalPages() - 1) {
            inventory.setItem(layout.slots().get("next"), items.button(
                    layout.material("next"),
                    "gui.list.next.name",
                    null,
                    Map.of()
            ));
        }
        if (layout.slots().containsKey("close")) {
            inventory.setItem(layout.slots().get("close"), items.button(
                    layout.material("close"),
                    "gui.list.close.name",
                    null,
                    Map.of()
            ));
        }
    }

    private ItemStack entryItem(Material material, PrivateHologram hologram) {
        String statusKey = hologram.displaySettings().enabled() ? "gui.status.enabled" : "gui.status.disabled";
        return items.button(
                material,
                "gui.list.entry.name",
                "gui.list.entry.lore",
                Map.of(
                        "name", hologram.name(),
                        "region", hologram.regionId(),
                        "lines", String.valueOf(hologram.lines().size()),
                        "status", messages.plain(statusKey, Map.of())
                )
        );
    }

    private void fillBackground() {
        Material filler = layout.material("filler");
        ItemStack pane = items.filler(filler);
        for (int slot = 0; slot < layout.size(); slot++) {
            inventory.setItem(slot, pane);
        }
    }

    private int totalPages() {
        int pageSize = Math.max(1, layout.contentSlots().size());
        return Math.max(1, (int) Math.ceil(holograms.size() / (double) pageSize));
    }

    private Map<String, String> titlePlaceholders() {
        return Map.of(
                "page", String.valueOf(page + 1),
                "pages", String.valueOf(totalPages())
        );
    }
}
