package bm.b0b0b0.soulHolo.listener;

import bm.b0b0b0.soulHolo.hologram.HologramEntityKeys;
import bm.b0b0b0.soulHolo.message.MessageService;
import bm.b0b0b0.soulHolo.model.PrivateHologram;
import bm.b0b0b0.soulHolo.service.GuiNavigationService;
import bm.b0b0b0.soulHolo.service.GuiOpenResult;
import bm.b0b0b0.soulHolo.service.HologramService;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.Optional;

public final class HologramInteractListener implements Listener {

    private final HologramService hologramService;
    private final GuiNavigationService navigation;
    private final MessageService messages;

    public HologramInteractListener(HologramService hologramService,
                                    GuiNavigationService navigation,
                                    MessageService messages) {
        this.hologramService = hologramService;
        this.navigation = navigation;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPreAttack(PrePlayerAttackEntityEvent event) {
        if (!HologramEntityKeys.read(event.getAttacked()).isPresent()) {
            return;
        }
        event.setCancelled(true);
        handle(event.getPlayer(), event.getAttacked(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeftClickDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (HologramEntityKeys.read(event.getEntity()).isEmpty()) {
            return;
        }
        event.setCancelled(true);
        handle(player, event.getEntity(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (HologramEntityKeys.read(event.getRightClicked()).isEmpty()) {
            return;
        }
        event.setCancelled(true);
        handle(event.getPlayer(), event.getRightClicked(), false);
    }

    private void handle(Player player, Entity entity, boolean openGui) {
        Optional<PrivateHologram> optional = hologramService.findByEntity(entity);
        if (optional.isEmpty()) {
            return;
        }
        PrivateHologram hologram = optional.get();
        if (!hologramService.canManage(player, hologram)) {
            messages.send(player, "hologram-not-owned");
            return;
        }
        hologramService.select(player, hologram);
        if (openGui) {
            if (navigation.openSettings(player, hologram) != GuiOpenResult.OPENED) {
                messages.send(player, "hologram-limit-denied");
            }
            return;
        }
        messages.send(player, "hologram-id-shown", Map.of("id", hologram.name()));
    }
}
