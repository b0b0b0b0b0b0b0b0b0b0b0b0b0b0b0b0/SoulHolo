package bm.b0b0b0.soulHolo.listener;

import bm.b0b0b0.soulHolo.SoulHolo;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.HologramLineGuiService;
import bm.b0b0b0.soulHolo.session.LineEditSessionService;
import bm.b0b0b0.soulHolo.session.PlayerSessionService;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.UUID;

public final class LineEditChatListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final SoulHolo plugin;
    private final HologramLineGuiService lineGuiService;
    private final GuiNavigationService navigation;
    private final LineEditSessionService sessions;
    private final PlayerSessionService playerSessions;

    public LineEditChatListener(SoulHolo plugin,
                                HologramLineGuiService lineGuiService,
                                GuiNavigationService navigation,
                                LineEditSessionService sessions,
                                PlayerSessionService playerSessions) {
        this.plugin = plugin;
        this.lineGuiService = lineGuiService;
        this.navigation = navigation;
        this.sessions = sessions;
        this.playerSessions = playerSessions;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (sessions.pending(event.getPlayer().getUniqueId()) == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = PLAIN.serialize(event.message());
        Bukkit.getScheduler().runTask(plugin, () -> {
            Optional<PrivateHologram> hologram = lineGuiService.applyChatInput(player, message);
            hologram.ifPresent(value -> navigation.openLines(player, value, 0));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        sessions.clear(playerId);
        playerSessions.clear(playerId);
    }
}
