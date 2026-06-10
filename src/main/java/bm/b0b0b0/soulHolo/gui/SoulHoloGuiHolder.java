package bm.b0b0b0.soulHolo.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;

public interface SoulHoloGuiHolder extends InventoryHolder {

    void handleInventoryClick(Player player, int slot, ClickType clickType);
}
