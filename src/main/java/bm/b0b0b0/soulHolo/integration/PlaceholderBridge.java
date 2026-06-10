package bm.b0b0b0.soulHolo.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlaceholderBridge {

    private final boolean available;

    public PlaceholderBridge() {
        this.available = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public boolean available() {
        return available;
    }

    public String apply(Player player, String text) {
        if (text == null) {
            return "";
        }
        if (!available || player == null) {
            return text;
        }
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

    public String applyOffline(String ownerName, String text) {
        if (text == null) {
            return "";
        }
        String resolved = text.replace("%player%", ownerName == null ? "?" : ownerName);
        if (!available) {
            return resolved;
        }
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, resolved);
    }
}
