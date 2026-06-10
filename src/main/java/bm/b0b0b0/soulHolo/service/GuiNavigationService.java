package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.HologramLinesMenu;
import bm.b0b0b0.soulHolo.gui.HologramListMenu;
import bm.b0b0b0.soulHolo.gui.HologramPositionMenu;
import bm.b0b0b0.soulHolo.gui.HologramSettingsMenu;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.entity.Player;

import java.util.List;

public final class GuiNavigationService {

    private final MessageService messages;
    private final GuiItemFactory items;
    private GuiLayoutConfig layoutConfig;
    private final DisplaySettingAccess access;
    private final HologramService hologramService;
    private final HologramDisplayService displayService;
    private final HologramLineGuiService lineGuiService;
    private final HologramPositionGuiService positionGuiService;

    public GuiNavigationService(MessageService messages,
                                GuiItemFactory items,
                                GuiLayoutConfig layoutConfig,
                                DisplaySettingAccess access,
                                HologramService hologramService,
                                HologramDisplayService displayService,
                                HologramLineGuiService lineGuiService,
                                HologramPositionGuiService positionGuiService) {
        this.messages = messages;
        this.items = items;
        this.layoutConfig = layoutConfig;
        this.access = access;
        this.hologramService = hologramService;
        this.displayService = displayService;
        this.lineGuiService = lineGuiService;
        this.positionGuiService = positionGuiService;
    }

    public void reload(GuiLayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }

    public void openList(Player player, int page) {
        if (!access.canOpenGui(player)) {
            messages.send(player, "gui-no-permission");
            return;
        }
        List<PrivateHologram> owned = hologramService.ownedHolograms(player);
        if (owned.isEmpty()) {
            messages.send(player, "gui-no-holograms");
            return;
        }
        HologramListMenu menu = new HologramListMenu(
                player,
                messages,
                items,
                layoutConfig.list(),
                this,
                owned,
                page
        );
        menu.open();
        messages.send(player, "gui-opened");
    }

    public void openSettings(Player player, PrivateHologram hologram) {
        if (!access.canOpenGui(player)) {
            messages.send(player, "gui-no-permission");
            return;
        }
        HologramSettingsMenu menu = new HologramSettingsMenu(
                player,
                hologram,
                messages,
                items,
                layoutConfig.settings(),
                access,
                displayService,
                this
        );
        menu.open();
    }

    public void openLines(Player player, PrivateHologram hologram, int page) {
        if (!lineGuiService.canEdit(player)) {
            messages.send(player, "gui-lines-no-permission");
            return;
        }
        HologramLinesMenu menu = new HologramLinesMenu(
                player,
                hologram,
                messages,
                items,
                layoutConfig.lines(),
                this,
                lineGuiService,
                page
        );
        menu.open();
    }

    public int maxLinesFor(Player player) {
        return lineGuiService.maxLines(player);
    }

    public void openPosition(Player player, PrivateHologram hologram) {
        if (!positionGuiService.canMove(player)) {
            messages.send(player, "gui-position-no-permission");
            return;
        }
        HologramPositionMenu menu = new HologramPositionMenu(
                player,
                hologram,
                messages,
                items,
                layoutConfig.position(),
                this,
                positionGuiService
        );
        menu.open();
    }
}
