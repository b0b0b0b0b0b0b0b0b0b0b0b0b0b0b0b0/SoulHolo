package bm.b0b0b0.soulHolo.command;

import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.service.HologramFailure;
import bm.b0b0b0.soulHolo.service.HologramService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public final class HologramFailureMessages {

    private final MessageService messages;
    private final HologramService hologramService;

    public HologramFailureMessages(MessageService messages, HologramService hologramService) {
        this.messages = messages;
        this.hologramService = hologramService;
    }

    public void send(CommandSender sender, HologramFailure failure, String name, int line) {
        switch (failure) {
            case BACKEND_MISSING -> messages.send(sender, "backend-missing");
            case WORLDGUARD_MISSING -> messages.send(sender, "worldguard-missing");
            case NOT_OWNER_REGION -> messages.send(sender, "not-owner-region");
            case NO_APPLICABLE_REGION -> messages.send(sender, "no-applicable-region");
            case REGION_LIMIT -> {
                if (sender instanceof Player player) {
                    var tier = hologramService.tier(player);
                    messages.send(sender, "region-limit-reached", Map.of(
                            "region", hologramService.regionForFailure(player),
                            "limit", String.valueOf(tier.maxHologramsPerRegion())
                    ));
                }
            }
            case LINE_LIMIT -> {
                if (sender instanceof Player player) {
                    messages.send(sender, "line-limit-reached", Map.of(
                            "limit", String.valueOf(hologramService.tier(player).maxLines())
                    ));
                }
            }
            case LINE_TOO_LONG -> {
                if (sender instanceof Player player) {
                    messages.send(sender, "line-too-long", Map.of(
                            "limit", String.valueOf(hologramService.tier(player).maxLineLength())
                    ));
                }
            }
            case BLACKLISTED -> messages.send(sender, "blacklisted-text");
            case INVALID_NAME -> messages.send(sender, "invalid-name", Map.of(
                    "min", String.valueOf(hologramService.nameMinLength()),
                    "max", String.valueOf(hologramService.nameMaxLength()),
                    "pattern", hologramService.namePatternLabel()
            ));
            case NAME_TAKEN -> messages.send(sender, "name-taken", Map.of("name", name == null ? "?" : name));
            case NOT_FOUND -> messages.send(sender, "hologram-not-found", Map.of("name", name == null ? "?" : name));
            case NOT_OWNED -> messages.send(sender, "hologram-not-owned");
            case NO_ACTIVE -> messages.send(sender, "no-active-hologram");
            case INVALID_LINE -> {
                int max = sender instanceof Player player ? hologramService.maxUserLines(player) : 0;
                messages.send(sender, "invalid-line-number", Map.of(
                        "line", String.valueOf(line),
                        "max", String.valueOf(max)
                ));
            }
            case CANNOT_EDIT_OWNER_LINE -> messages.send(sender, "cannot-edit-owner-line");
            case PLAYER_NOT_FOUND -> messages.send(sender, "player-not-found", Map.of("player", name == null ? "?" : name));
            case OUTSIDE_REGION -> messages.send(sender, "position-outside-region", Map.of(
                    "region", resolveRegion(name, sender)
            ));
            case NO_PERMISSION -> messages.send(sender, "no-permission");
            case LINE_EDIT_DENIED -> messages.send(sender, "gui-lines-no-permission");
            case POSITION_DENIED -> messages.send(sender, "gui-position-no-permission");
            case INVALID_SETTING -> messages.send(sender, "invalid-setting");
            default -> messages.send(sender, "unknown-subcommand");
        }
    }

    private String resolveRegion(String name, CommandSender sender) {
        if (name != null) {
            return hologramService.findByName(name.toLowerCase(Locale.ROOT))
                    .map(h -> h.regionId())
                    .orElse("?");
        }
        if (sender instanceof Player player) {
            return hologramService.resolveForPlayer(player, null, false)
                    .map(h -> h.regionId())
                    .orElse("?");
        }
        return "?";
    }
}
