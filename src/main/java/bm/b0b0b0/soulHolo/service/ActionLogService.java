package bm.b0b0b0.soulHolo.service;

import bm.b0b0b0.soulHolo.core.PluginExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class ActionLogService {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaPlugin plugin;
    private final PluginExecutor executor;
    private boolean enabled;
    private boolean fileEnabled;
    private final File logFile;

    public ActionLogService(JavaPlugin plugin, PluginExecutor executor, boolean enabled, boolean fileEnabled) {
        this.plugin = plugin;
        this.executor = executor;
        this.enabled = enabled;
        this.fileEnabled = fileEnabled;
        this.logFile = new File(plugin.getDataFolder(), "actions.log");
    }

    public void reload(boolean enabled, boolean fileEnabled) {
        this.enabled = enabled;
        this.fileEnabled = fileEnabled;
    }

    public void log(String action, String details) {
        if (!enabled) {
            return;
        }
        String line = "[" + FORMAT.format(LocalDateTime.now()) + "] " + action + " | " + details;
        plugin.getLogger().info(line);
        if (!fileEnabled) {
            return;
        }
        executor.io().execute(() -> appendLine(line));
    }

    private void appendLine(String line) {
        File parent = logFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(line);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not write actions.log", exception);
        }
    }
}
