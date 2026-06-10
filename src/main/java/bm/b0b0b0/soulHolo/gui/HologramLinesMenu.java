package bm.b0b0b0.soulHolo.gui;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramLineGuiService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class HologramLinesMenu implements SoulHoloGuiHolder {

    private final Player viewer;
    private final PrivateHologram hologram;
    private final MessageService messages;
    private final GuiItemFactory items;
    private final GuiLayoutConfig.ScreenLayout layout;
    private final GuiNavigationService navigation;
    private final HologramLineGuiService lineGuiService;
    private final Inventory inventory;
    private int page;

    public HologramLinesMenu(Player viewer,
                             PrivateHologram hologram,
                             MessageService messages,
                             GuiItemFactory items,
                             GuiLayoutConfig.ScreenLayout layout,
                             GuiNavigationService navigation,
                             HologramLineGuiService lineGuiService,
                             int page) {
        this.viewer = viewer;
        this.hologram = hologram;
        this.messages = messages;
        this.items = items;
        this.layout = layout;
        this.navigation = navigation;
        this.lineGuiService = lineGuiService;
        this.page = Math.max(0, page);
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
        handleClick(slot, clickType);
    }

    public void handleClick(int slot, ClickType clickType) {
        Integer previousSlot = layout.slots().get("previous");
        Integer nextSlot = layout.slots().get("next");
        Integer backSlot = layout.slots().get("back");
        Integer closeSlot = layout.slots().get("close");
        Integer addSlot = layout.slots().get("add");
        if (closeSlot != null && slot == closeSlot) {
            viewer.closeInventory();
            return;
        }
        if (backSlot != null && slot == backSlot) {
            navigation.openSettings(viewer, hologram);
            return;
        }
        if (addSlot != null && slot == addSlot) {
            lineGuiService.beginAdd(viewer, hologram);
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
        Integer lineNumber = lineNumberAtSlot(slot);
        if (lineNumber == null) {
            return;
        }
        if (clickType == ClickType.SHIFT_LEFT) {
            if (lineGuiService.moveLine(viewer, hologram, lineNumber, -1)) {
                redraw();
            }
            return;
        }
        if (clickType == ClickType.SHIFT_RIGHT) {
            if (lineGuiService.moveLine(viewer, hologram, lineNumber, 1)) {
                redraw();
            }
            return;
        }
        if (clickType == ClickType.RIGHT || clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP) {
            if (lineGuiService.deleteLine(viewer, hologram, lineNumber)) {
                if (page >= totalPages()) {
                    page = Math.max(0, totalPages() - 1);
                }
                redraw();
            }
            return;
        }
        if (clickType == ClickType.LEFT) {
            lineGuiService.beginEdit(viewer, hologram, lineNumber);
        }
    }

    private Integer lineNumberAtSlot(int slot) {
        List<Integer> contentSlots = layout.contentSlots();
        int pageSize = contentSlots.size();
        int start = page * pageSize;
        for (int index = 0; index < contentSlots.size(); index++) {
            if (contentSlots.get(index) != slot) {
                continue;
            }
            int lineIndex = start + index;
            if (lineIndex >= hologram.lines().size()) {
                return null;
            }
            return lineIndex + 1;
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
            int lineIndex = start + index;
            if (lineIndex >= hologram.lines().size()) {
                break;
            }
            int lineNumber = lineIndex + 1;
            String preview = preview(hologram.lines().get(lineIndex));
            inventory.setItem(contentSlots.get(index), items.button(
                    entryMaterial,
                    "gui.lines.entry.name",
                    "gui.lines.entry.lore",
                    Map.of(
                            "line", String.valueOf(lineNumber),
                            "text", preview,
                            "max", String.valueOf(lineGuiService.maxLines(viewer))
                    )
            ));
        }
        if (layout.slots().containsKey("add")) {
            inventory.setItem(layout.slots().get("add"), items.button(
                    layout.material("add"),
                    "gui.lines.add.name",
                    "gui.lines.add.lore",
                    Map.of(
                            "count", String.valueOf(hologram.lines().size()),
                            "max", String.valueOf(lineGuiService.maxLines(viewer))
                    )
            ));
        }
        if (layout.slots().containsKey("previous") && page > 0) {
            inventory.setItem(layout.slots().get("previous"), items.button(
                    layout.material("previous"),
                    "gui.lines.previous.name",
                    null,
                    Map.of()
            ));
        }
        if (layout.slots().containsKey("next") && page < totalPages() - 1) {
            inventory.setItem(layout.slots().get("next"), items.button(
                    layout.material("next"),
                    "gui.lines.next.name",
                    null,
                    Map.of()
            ));
        }
        if (layout.slots().containsKey("back")) {
            inventory.setItem(layout.slots().get("back"), items.button(
                    layout.material("back"),
                    "gui.lines.back.name",
                    null,
                    Map.of()
            ));
        }
        if (layout.slots().containsKey("close")) {
            inventory.setItem(layout.slots().get("close"), items.button(
                    layout.material("close"),
                    "gui.lines.close.name",
                    null,
                    Map.of()
            ));
        }
    }

    private String preview(String raw) {
        if (raw == null || raw.isBlank()) {
            return messages.plain("gui.lines.empty-preview", Map.of());
        }
        String trimmed = raw.replace('\n', ' ');
        if (trimmed.length() <= 48) {
            return trimmed;
        }
        return trimmed.substring(0, 45) + "...";
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
        return Math.max(1, (int) Math.ceil(hologram.lines().size() / (double) pageSize));
    }

    private Map<String, String> titlePlaceholders() {
        return Map.of(
                "name", hologram.name(),
                "count", String.valueOf(hologram.lines().size()),
                "max", String.valueOf(lineGuiService.maxLines(viewer)),
                "page", String.valueOf(page + 1),
                "pages", String.valueOf(totalPages())
        );
    }
}
