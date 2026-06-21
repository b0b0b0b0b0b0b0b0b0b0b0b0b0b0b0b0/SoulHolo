package bm.b0b0b0.soulHolo.listener;

import bm.b0b0b0.soulHolo.config.PluginConfig;
import bm.b0b0b0.soulHolo.config.PluginConfigHolder;
import bm.b0b0b0.soulHolo.service.HologramService;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class RegionGuardCleanupListener {

    private final JavaPlugin plugin;
    private final PluginConfigHolder configHolder;
    private final HologramService hologramService;
    private BukkitTask task;

    public RegionGuardCleanupListener(JavaPlugin plugin,
                                      PluginConfigHolder configHolder,
                                      HologramService hologramService) {
        this.plugin = plugin;
        this.configHolder = configHolder;
        this.hologramService = hologramService;
    }

    public void start() {
        stop();
        PluginConfig config = configHolder.pluginConfig();
        if (!config.regionGuardPurgeEnabled()) {
            return;
        }
        long intervalTicks = Math.max(20L, config.regionGuardIntervalSeconds() * 20L);
        int batchSize = config.regionGuardBatchSize();
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> hologramService.purgeMissingRegionsBatch(batchSize),
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
