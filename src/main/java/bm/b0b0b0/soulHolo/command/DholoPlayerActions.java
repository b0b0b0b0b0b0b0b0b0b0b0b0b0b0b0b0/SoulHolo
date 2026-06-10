package bm.b0b0b0.soulHolo.command;

import bm.b0b0b0.soulHolo.model.DisplaySettingKey;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.model.RelativeMoveDirection;
import bm.b0b0b0.soulHolo.service.DisplaySettingAccess;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramDisplayService;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import bm.b0b0b0.soulHolo.service.HologramLineGuiService;
import bm.b0b0b0.soulHolo.service.HologramPositionGuiService;
import bm.b0b0b0.soulHolo.service.HologramService;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public final class DholoPlayerActions {

    private final HologramService hologramService;
    private final HologramDisplayService displayService;
    private final HologramLineGuiService lineGuiService;
    private final HologramPositionGuiService positionGuiService;
    private final DisplaySettingAccess access;
    private final GuiNavigationService navigation;

    public DholoPlayerActions(HologramService hologramService,
                              HologramDisplayService displayService,
                              HologramLineGuiService lineGuiService,
                              HologramPositionGuiService positionGuiService,
                              DisplaySettingAccess access,
                              GuiNavigationService navigation) {
        this.hologramService = hologramService;
        this.displayService = displayService;
        this.lineGuiService = lineGuiService;
        this.positionGuiService = positionGuiService;
        this.access = access;
        this.navigation = navigation;
    }

    public HologramFailure openGui(Player player, String hologramName) {
        if (hologramName == null || hologramName.isBlank()) {
            navigation.openList(player, 0);
            return HologramFailure.NONE;
        }
        Optional<PrivateHologram> optional = hologramService.findByName(hologramName.toLowerCase(Locale.ROOT));
        if (optional.isEmpty()) {
            return HologramFailure.NOT_FOUND;
        }
        PrivateHologram hologram = optional.get();
        if (!hologram.ownerId().equals(player.getUniqueId()) && !access.isAdmin(player)) {
            return HologramFailure.NOT_OWNED;
        }
        navigation.openSettings(player, hologram);
        return HologramFailure.NONE;
    }

    public HologramFailure movePosition(Player player, RelativeMoveDirection direction, String hologramName, boolean admin) {
        if (!positionGuiService.canMove(player)) {
            return HologramFailure.POSITION_DENIED;
        }
        Optional<PrivateHologram> optional = hologramService.resolveForPlayer(player, hologramName, admin);
        if (optional.isEmpty()) {
            return admin ? HologramFailure.NOT_FOUND : HologramFailure.NO_ACTIVE;
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(player.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        return hologramService.shiftPosition(
                player,
                hologram,
                player.getLocation().getYaw(),
                direction,
                admin || access.isAdmin(player)
        );
    }

    public HologramFailure moveLine(Player player, int lineNumber, int direction, String hologramName, boolean admin) {
        if (!lineGuiService.canEdit(player)) {
            return HologramFailure.LINE_EDIT_DENIED;
        }
        Optional<PrivateHologram> optional = hologramService.resolveForPlayer(player, hologramName, admin);
        if (optional.isEmpty()) {
            return admin ? HologramFailure.NOT_FOUND : HologramFailure.NO_ACTIVE;
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(player.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        return hologramService.moveLine(player, hologram, lineNumber, direction, admin || access.isAdmin(player));
    }

    public HologramFailure addLine(Player player, String text, boolean admin, String hologramName) {
        if (!lineGuiService.canEdit(player)) {
            return HologramFailure.LINE_EDIT_DENIED;
        }
        return hologramService.addLine(player, text, admin, hologramName);
    }

    public HologramFailure removeLine(Player player, int line, boolean admin, String hologramName) {
        if (!lineGuiService.canEdit(player)) {
            return HologramFailure.LINE_EDIT_DENIED;
        }
        return hologramService.removeLine(player, line, admin, hologramName);
    }

    public HologramFailure editLine(Player player, int line, String text, boolean admin, String hologramName) {
        if (!lineGuiService.canEdit(player)) {
            return HologramFailure.LINE_EDIT_DENIED;
        }
        return hologramService.editLine(player, line, text, admin, hologramName);
    }

    public boolean applySetting(Player player, String rawKey, String rawScaleDirection, String hologramName, boolean admin) {
        Optional<PrivateHologram> optional = hologramService.resolveForPlayer(player, hologramName, admin);
        if (optional.isEmpty()) {
            return false;
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(player.getUniqueId())) {
            return false;
        }
        DisplaySettingKey key = parseSettingKey(rawKey);
        if (key == null) {
            return false;
        }
        return switch (key) {
            case ENABLED, SEE_THROUGH, TEXT_SHADOW -> displayService.toggleBoolean(player, hologram, key);
            case BILLBOARD -> displayService.cycleBillboard(player, hologram);
            case BACKGROUND -> displayService.cycleBackground(player, hologram);
            case SCALE -> displayService.adjustScale(player, hologram, parseScaleIncrease(rawScaleDirection));
            case TEXT_ALIGNMENT -> displayService.cycleAlignment(player, hologram);
            case SHADOW -> displayService.cycleShadow(player, hologram);
        };
    }

    public HologramFailure applySettingFailure(Player player,
                                               String rawKey,
                                               String rawScaleDirection,
                                               String hologramName,
                                               boolean admin) {
        Optional<PrivateHologram> optional = hologramService.resolveForPlayer(player, hologramName, admin);
        if (optional.isEmpty()) {
            return admin ? HologramFailure.NOT_FOUND : HologramFailure.NO_ACTIVE;
        }
        PrivateHologram hologram = optional.get();
        if (!admin && !hologram.ownerId().equals(player.getUniqueId())) {
            return HologramFailure.NOT_OWNED;
        }
        if (parseSettingKey(rawKey) == null) {
            return HologramFailure.INVALID_SETTING;
        }
        if (!applySetting(player, rawKey, rawScaleDirection, hologramName, admin)) {
            return HologramFailure.SETTING_DENIED;
        }
        return HologramFailure.NONE;
    }

    public Optional<PrivateHologram> resolve(Player player, String hologramName, boolean admin) {
        return hologramService.resolveForPlayer(player, hologramName, admin);
    }

    public static RelativeMoveDirection parseMoveDirection(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "up", "u", "выше", "вверх" -> RelativeMoveDirection.UP;
            case "down", "d", "ниже", "вниз" -> RelativeMoveDirection.DOWN;
            case "left", "l", "влево" -> RelativeMoveDirection.LEFT;
            case "right", "r", "вправо" -> RelativeMoveDirection.RIGHT;
            default -> null;
        };
    }

    public static int parseLineShiftDirection(String raw) {
        if (raw == null) {
            return 0;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "up", "u", "выше", "вверх" -> -1;
            case "down", "d", "ниже", "вниз" -> 1;
            default -> 0;
        };
    }

    private static DisplaySettingKey parseSettingKey(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "enabled", "visible", "visibility", "видимость" -> DisplaySettingKey.ENABLED;
            case "see-through", "seethrough", "see", "сквозь" -> DisplaySettingKey.SEE_THROUGH;
            case "text-shadow", "textshadow", "shadow-text", "тень-текста" -> DisplaySettingKey.TEXT_SHADOW;
            case "billboard", "поворот" -> DisplaySettingKey.BILLBOARD;
            case "background", "bg", "фон" -> DisplaySettingKey.BACKGROUND;
            case "scale", "масштаб" -> DisplaySettingKey.SCALE;
            case "alignment", "align", "выравнивание" -> DisplaySettingKey.TEXT_ALIGNMENT;
            case "shadow", "block-shadow", "тень" -> DisplaySettingKey.SHADOW;
            default -> null;
        };
    }

    private static boolean parseScaleIncrease(String raw) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "+", "up", "plus", "больше", "увеличить" -> true;
            case "-", "down", "minus", "меньше", "уменьшить" -> false;
            default -> true;
        };
    }
}
