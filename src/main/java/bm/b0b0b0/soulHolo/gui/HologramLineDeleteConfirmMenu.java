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

import java.util.Map;

public final class HologramLineDeleteConfirmMenu implements SoulHoloGuiHolder {

    private final Player viewer;
    private final PrivateHologram hologram;
    private final int lineNumber;
    private final int returnPage;
    private final MessageService messages;
    private final GuiItemFactory items;
    private final GuiLayoutConfig.ScreenLayout layout;
    private final GuiNavigationService navigation;
    private final HologramLineGuiService lineGuiService;
    private final Inventory inventory;

    public HologramLineDeleteConfirmMenu(Player viewer,
                                         PrivateHologram hologram,
                                         int lineNumber,
                                         int returnPage,
                                         MessageService messages,
                                         GuiItemFactory items,
                                         GuiLayoutConfig.ScreenLayout layout,
                                         GuiNavigationService navigation,
                                         HologramLineGuiService lineGuiService) {
        this.viewer = viewer;
        this.hologram = hologram;
        this.lineNumber = lineNumber;
        this.returnPage = returnPage;
        this.messages = messages;
        this.items = items;
        this.layout = layout;
        this.navigation = navigation;
        this.lineGuiService = lineGuiService;
        this.inventory = Bukkit.createInventory(this, layout.size(), messages.component(
                layout.titleKey(),
                Map.of(
                        "line", String.valueOf(lineNumber),
                        "name", hologram.name()
                )
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
        Integer yesSlot = layout.slots().get("confirm-yes");
        Integer noSlot = layout.slots().get("confirm-no");
        if (yesSlot != null && slot == yesSlot) {
            if (lineGuiService.deleteLine(viewer, hologram, lineNumber)) {
                navigation.openLines(viewer, hologram, returnPage);
            } else {
                navigation.openLines(viewer, hologram, returnPage);
            }
            return;
        }
        if (noSlot != null && slot == noSlot) {
            navigation.openLines(viewer, hologram, returnPage);
        }
    }

    private void redraw() {
        inventory.clear();
        fillBackground();
        String preview = previewText();
        Integer infoSlot = layout.slots().get("confirm-info");
        if (infoSlot != null) {
            inventory.setItem(infoSlot, items.button(
                    layout.material("entry"),
                    "gui.lines.delete-confirm.info.name",
                    "gui.lines.delete-confirm.info.lore",
                    Map.of(
                            "line", String.valueOf(lineNumber),
                            "text", preview
                    )
            ));
        }
        Integer yesSlot = layout.slots().get("confirm-yes");
        if (yesSlot != null) {
            inventory.setItem(yesSlot, items.button(
                    layout.material("confirm-yes"),
                    "gui.lines.delete-confirm.yes.name",
                    "gui.lines.delete-confirm.yes.lore",
                    Map.of("line", String.valueOf(lineNumber))
            ));
        }
        Integer noSlot = layout.slots().get("confirm-no");
        if (noSlot != null) {
            inventory.setItem(noSlot, items.button(
                    layout.material("confirm-no"),
                    "gui.lines.delete-confirm.no.name",
                    "gui.lines.delete-confirm.no.lore",
                    Map.of()
            ));
        }
    }

    private String previewText() {
        if (!lineGuiService.hasLineContent(hologram, lineNumber)) {
            return messages.plain("gui.lines.empty-preview", Map.of());
        }
        String raw = hologram.lines().get(lineNumber - 1);
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
}
