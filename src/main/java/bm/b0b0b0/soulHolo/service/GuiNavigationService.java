package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.gui.HologramLineDeleteConfirmMenu;
import bm.b0b0b0.soulHolo.gui.HologramLinesMenu;
import bm.b0b0b0.soulHolo.gui.HologramListMenu;
import bm.b0b0b0.soulHolo.gui.HologramPositionMenu;
import bm.b0b0b0.soulHolo.gui.HologramSettingsMenu;
import bm.b0b0b0.soulHolo.gui.item.GuiItemFactory;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

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

    public GuiOpenResult openList(Player player, int page) {
        if (!access.canOpenGui(player)) {
            return GuiOpenResult.NO_PERMISSION;
        }
        List<PrivateHologram> owned = hologramService.ownedHolograms(player);
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
        return GuiOpenResult.OPENED;
    }

    public GuiOpenResult openSettings(Player player, PrivateHologram hologram) {
        if (!access.canOpenGui(player)) {
            return GuiOpenResult.NO_PERMISSION;
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
        return GuiOpenResult.OPENED;
    }

    public GuiOpenResult openLines(Player player, PrivateHologram hologram, int page) {
        if (!lineGuiService.canEdit(player)) {
            return GuiOpenResult.NO_PERMISSION;
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
        return GuiOpenResult.OPENED;
    }

    public GuiOpenResult openLineDeleteConfirm(Player player, PrivateHologram hologram, int lineNumber, int returnPage) {
        if (!lineGuiService.canEdit(player)) {
            return GuiOpenResult.NO_PERMISSION;
        }
        if (!lineGuiService.hasLineContent(hologram, lineNumber)) {
            return openLines(player, hologram, returnPage);
        }
        HologramLineDeleteConfirmMenu menu = new HologramLineDeleteConfirmMenu(
                player,
                hologram,
                lineNumber,
                returnPage,
                messages,
                items,
                layoutConfig.linesDeleteConfirm(),
                this,
                lineGuiService
        );
        menu.open();
        return GuiOpenResult.OPENED;
    }

    public int maxLinesFor(Player player) {
        return lineGuiService.maxLines(player);
    }

    public GuiOpenResult openPosition(Player player, PrivateHologram hologram) {
        boolean canMove = positionGuiService.canMove(player);
        boolean canRotate = access.canChange(player, DisplaySettingKey.BILLBOARD)
                && access.isSupported(DisplaySettingKey.BILLBOARD);
        if (!canMove && !canRotate) {
            return GuiOpenResult.NO_PERMISSION;
        }
        HologramPositionMenu menu = new HologramPositionMenu(
                player,
                hologram,
                messages,
                items,
                layoutConfig.position(),
                this,
                positionGuiService,
                displayService,
                access
        );
        menu.open();
        return GuiOpenResult.OPENED;
    }

    public GuiOpenResult deleteHologram(Player player, PrivateHologram hologram) {
        if (!hologramService.canManage(player, hologram)) {
            return GuiOpenResult.NOT_OWNED;
        }
        HologramFailure failure = hologramService.deleteOwnedHologram(player, hologram, access.isAdmin(player));
        if (failure != HologramFailure.NONE) {
            return GuiOpenResult.NOT_OWNED;
        }
        messages.send(player, "hologram-deleted", Map.of("name", hologram.name()));
        return openList(player, 0);
    }

    public static HologramFailure toFailure(GuiOpenResult result) {
        return switch (result) {
            case OPENED -> HologramFailure.NONE;
            case NO_PERMISSION -> HologramFailure.GUI_NO_PERMISSION;
            case NOT_FOUND -> HologramFailure.NOT_FOUND;
            case NOT_OWNED -> HologramFailure.NOT_OWNED;
        };
    }
}
