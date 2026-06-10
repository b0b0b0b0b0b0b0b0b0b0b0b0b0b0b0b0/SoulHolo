package bm.b0b0b0.soulHolo.config;

import bm.b0b0b0.soulHolo.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulHolo.config.settings.SoulHoloSettings;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigurationLoader {

    private final JavaPlugin plugin;
    private final SoulHoloSettings mainSettings = new SoulHoloSettings();
    private final GuiGeneralSettings guiSettings = new GuiGeneralSettings();
    private PluginConfig pluginConfig;

    public ConfigurationLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PluginConfig load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        SerializedConfigReloader.reload(plugin, mainSettings, Path.of("config.yml"));
        SerializedConfigReloader.reload(plugin, guiSettings, Path.of("gui", "general.yml"));
        pluginConfig = new PluginConfig(mainSettings, guiSettings);
        return pluginConfig;
    }

    public PluginConfig config() {
        return pluginConfig;
    }

    public SoulHoloSettings mainSettings() {
        return mainSettings;
    }

    public GuiGeneralSettings guiSettings() {
        return guiSettings;
    }

    public void reload() {
        load();
    }
}
