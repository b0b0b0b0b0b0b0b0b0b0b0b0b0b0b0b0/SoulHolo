package bm.b0b0b0.soulHolo.gui.item;

import bm.b0b0b0.soulHolo.config.GuiLayoutConfig;
import bm.b0b0b0.soulHolo.message.MessageService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GuiItemFactory {

    private final MessageService messages;
    private GuiLayoutConfig layoutConfig;

    public GuiItemFactory(MessageService messages, GuiLayoutConfig layoutConfig) {
        this.messages = messages;
        this.layoutConfig = layoutConfig;
    }

    public void reload(GuiLayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }

    public ItemStack button(Material material, String nameKey, String loreKey, Map<String, String> placeholders) {
        return item(resolveMaterial(material), nameKey, loreKey, placeholders);
    }

    public ItemStack toggle(Material material, String nameKey, String loreOnKey, String loreOffKey, boolean enabled,
                            Map<String, String> placeholders) {
        ItemStack stack = new ItemStack(resolveMaterial(material));
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(messages.component(nameKey, placeholders));
        List<Component> lore = new ArrayList<>();
        for (String line : messages.lore(enabled ? loreOnKey : loreOffKey, placeholders)) {
            lore.add(messages.legacyComponent(line));
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack filler(Material material) {
        ItemStack stack = new ItemStack(resolveMaterial(material));
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.empty());
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack item(Material material, String nameKey, String loreKey, Map<String, String> placeholders) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(messages.component(nameKey, placeholders));
        if (loreKey != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : messages.lore(loreKey, placeholders)) {
                lore.add(messages.legacyComponent(line));
            }
            if (!lore.isEmpty()) {
                meta.lore(lore);
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    private Material resolveMaterial(Material material) {
        if (material != null) {
            return material;
        }
        return layoutConfig.list().material("button");
    }
}
