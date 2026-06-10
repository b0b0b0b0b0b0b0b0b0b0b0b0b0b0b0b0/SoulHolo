package bm.b0b0b0.soulHolo.hologram;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class HologramBackendFactory {

    private static final PaperTextDisplayBackend PAPER = new PaperTextDisplayBackend();
    private static final UnavailableHologramBackend UNAVAILABLE = new UnavailableHologramBackend();

    private HologramBackendFactory() {
    }

    public static HologramBackend resolve(PluginConfig config) {
        PluginConfig.HologramBackendType type = config.backendType();
        if (type == PluginConfig.HologramBackendType.PAPER) {
            return PAPER;
        }
        if (type == PluginConfig.HologramBackendType.DECENT) {
            return createDecent();
        }
        if (type == PluginConfig.HologramBackendType.FANCY) {
            return createFancy();
        }
        return PAPER;
    }

    private static HologramBackend createDecent() {
        if (!isPluginEnabled("DecentHolograms")) {
            return UNAVAILABLE;
        }
        return new DecentHologramsBackend();
    }

    private static HologramBackend createFancy() {
        if (!isPluginEnabled("FancyHolograms")) {
            return UNAVAILABLE;
        }
        return new FancyHologramsBackend();
    }

    private static boolean isPluginEnabled(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    public static boolean anyAvailable() {
        return true;
    }
}
