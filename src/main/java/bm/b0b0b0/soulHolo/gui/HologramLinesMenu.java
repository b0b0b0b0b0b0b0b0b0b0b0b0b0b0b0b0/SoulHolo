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
        if (backSlot != null && slot == backSlot) {
            navigation.openSettings(viewer, hologram);
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
        Integer hintSlot = layout.slots().get("hint-toggle");
        if (hintSlot != null && slot == hintSlot) {
            if (lineGuiService.toggleHintLine(viewer, hologram)) {
                redraw();
            }
            return;
        }
        Integer ownerSlot = layout.slots().get("owner-toggle");
        if (ownerSlot != null && slot == ownerSlot) {
            if (lineGuiService.toggleOwnerLine(viewer, hologram)) {
                redraw();
            }
            return;
        }
        Integer contentIndex = contentIndexAtSlot(slot);
        if (contentIndex == null) {
            return;
        }
        int lineNumber = lineNumberForContentIndex(contentIndex);
        if (lineNumber > lineGuiService.maxLines(viewer)) {
            return;
        }
        boolean filled = lineGuiService.hasLineContent(hologram, lineNumber);
        if (clickType == ClickType.LEFT) {
            if (filled) {
                lineGuiService.beginEdit(viewer, hologram, lineNumber);
            } else {
                lineGuiService.beginAddAt(viewer, hologram, lineNumber);
            }
            return;
        }
        if (clickType == ClickType.RIGHT && filled) {
            navigation.openLineDeleteConfirm(viewer, hologram, lineNumber, page);
            return;
        }
        if (clickType == ClickType.MIDDLE && filled) {
            if (lineGuiService.toggleLineHidden(viewer, hologram, lineNumber)) {
                redraw();
            }
        }
    }

    private Integer contentIndexAtSlot(int slot) {
        List<Integer> contentSlots = layout.contentSlots();
        for (int index = 0; index < contentSlots.size(); index++) {
            if (contentSlots.get(index) == slot) {
                return index;
            }
        }
        return null;
    }

    private int lineNumberForContentIndex(int contentIndex) {
        int pageSize = layout.contentSlots().size();
        return page * pageSize + contentIndex + 1;
    }

    private void redraw() {
        inventory.clear();
        fillBackground();
        drawReservedToggles();
        List<Integer> contentSlots = layout.contentSlots();
        int pageSize = contentSlots.size();
        int maxLines = lineGuiService.maxLines(viewer);
        for (int index = 0; index < contentSlots.size(); index++) {
            int lineNumber = page * pageSize + index + 1;
            if (lineNumber > maxLines) {
                break;
            }
            if (lineGuiService.hasLineContent(hologram, lineNumber)) {
                String preview = preview(lineAt(hologram, lineNumber));
                boolean hidden = lineGuiService.isLineHidden(hologram, lineNumber);
                inventory.setItem(contentSlots.get(index), items.button(
                        layout.material(hidden ? "entry-hidden" : "entry"),
                        hidden ? "gui.lines.entry-hidden.name" : "gui.lines.entry.name",
                        hidden ? "gui.lines.entry-hidden.lore" : "gui.lines.entry.lore",
                        Map.of(
                                "line", String.valueOf(lineNumber),
                                "text", preview,
                                "max", String.valueOf(maxLines)
                        )
                ));
            } else {
                inventory.setItem(contentSlots.get(index), items.button(
                        layout.material("empty"),
                        "gui.lines.empty.name",
                        "gui.lines.empty.lore",
                        Map.of(
                                "line", String.valueOf(lineNumber),
                                "max", String.valueOf(maxLines)
                        )
                ));
            }
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
    }

    private void drawReservedToggles() {
        Integer hintSlot = layout.slots().get("hint-toggle");
        if (hintSlot != null) {
            boolean visible = lineGuiService.hintLineVisible(hologram);
            inventory.setItem(hintSlot, items.toggle(
                    layout.material(visible ? "reserved-shown" : "reserved-hidden"),
                    "gui.lines.reserved.hint.name",
                    "gui.lines.reserved.hint.lore-on",
                    "gui.lines.reserved.hint.lore-off",
                    visible,
                    Map.of("text", lineGuiService.hintLinePreview(viewer, hologram))
            ));
        }
        Integer ownerSlot = layout.slots().get("owner-toggle");
        if (ownerSlot != null) {
            boolean visible = lineGuiService.ownerLineVisible(hologram);
            inventory.setItem(ownerSlot, items.toggle(
                    layout.material(visible ? "reserved-shown" : "reserved-hidden"),
                    "gui.lines.reserved.owner.name",
                    "gui.lines.reserved.owner.lore-on",
                    "gui.lines.reserved.owner.lore-off",
                    visible,
                    Map.of("text", lineGuiService.ownerLinePreview(viewer, hologram))
            ));
        }
    }

    private String lineAt(PrivateHologram hologram, int lineNumber) {
        if (lineNumber < 1 || lineNumber > hologram.lines().size()) {
            return "";
        }
        String line = hologram.lines().get(lineNumber - 1);
        return line == null ? "" : line;
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
        int maxLines = lineGuiService.maxLines(viewer);
        return Math.max(1, (int) Math.ceil(maxLines / (double) pageSize));
    }

    private Map<String, String> titlePlaceholders() {
        return Map.of(
                "name", hologram.name(),
                "count", String.valueOf(lineGuiService.countFilledLines(hologram)),
                "max", String.valueOf(lineGuiService.maxLines(viewer)),
                "page", String.valueOf(page + 1),
                "pages", String.valueOf(totalPages())
        );
    }
}
