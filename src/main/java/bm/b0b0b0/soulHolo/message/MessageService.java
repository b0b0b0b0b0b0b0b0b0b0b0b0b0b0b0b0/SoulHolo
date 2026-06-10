package bm.b0b0b0.soulHolo.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class MessageService {

    private static final int MESSAGES_VERSION = 3;

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private final JavaPlugin plugin;
    private File messagesFile;
    private FileConfiguration messages;

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create data folder");
        }
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        mergeDefaults(messages, "messages.yml");
        upgradeMessagesIfNeeded();
        saveQuietly();
    }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        mergeDefaults(messages, "messages.yml");
        upgradeMessagesIfNeeded();
        saveQuietly();
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(component(key, placeholders));
    }

    public String plain(String key, Map<String, String> placeholders) {
        return applyPlaceholders(messages.getString(key, key), placeholders);
    }

    public Component component(String key, Map<String, String> placeholders) {
        return LEGACY.deserialize(applyPlaceholders(messages.getString(key, key), placeholders));
    }

    public Component legacyComponent(String raw) {
        return LEGACY.deserialize(raw == null ? "" : raw);
    }

    public List<String> lore(String key, Map<String, String> placeholders) {
        List<String> source = messages.getStringList(key);
        if (source.isEmpty()) {
            String single = messages.getString(key);
            if (single == null) {
                return List.of();
            }
            source = List.of(single);
        }
        List<String> result = new ArrayList<>();
        for (String line : source) {
            result.add(applyPlaceholders(line, placeholders));
        }
        return result;
    }

    private String applyPlaceholders(String raw, Map<String, String> placeholders) {
        if (raw == null) {
            return "";
        }
        String prefix = messages.getString("prefix", "");
        Map<String, String> merged = new HashMap<>(placeholders);
        merged.putIfAbsent("prefix", prefix);
        for (Map.Entry<String, String> entry : merged.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return raw;
    }

    private void mergeDefaults(FileConfiguration target, String resourceName) {
        InputStream stream = plugin.getResource(resourceName);
        if (stream == null) {
            return;
        }
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        target.setDefaults(defaults);
        target.options().copyDefaults(true);
    }

    private void upgradeMessagesIfNeeded() {
        int version = messages.getInt("_meta.version", 0);
        if (version >= MESSAGES_VERSION) {
            return;
        }
        InputStream stream = plugin.getResource("messages.yml");
        if (stream == null) {
            return;
        }
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        for (String key : defaults.getKeys(true)) {
            if (key.startsWith("_meta")) {
                continue;
            }
            if (defaults.isConfigurationSection(key)) {
                continue;
            }
            messages.set(key, defaults.get(key));
        }
        messages.set("_meta.version", MESSAGES_VERSION);
    }

    private void saveQuietly() {
        try {
            messages.save(messagesFile);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save messages.yml", exception);
        }
    }
}
