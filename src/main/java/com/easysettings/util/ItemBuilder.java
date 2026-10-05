package com.easysettings.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for creating and customizing ItemStacks.
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material != null ? material : Material.STONE, amount);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String displayName) {
        if (meta != null && displayName != null) {
            meta.setDisplayName(ColorUtil.colorize(displayName));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (meta != null && lore != null) {
            meta.setLore(ColorUtil.colorize(lore));
        }
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        if (meta != null && lines != null) {
            List<String> existing = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            for (String line : lines) {
                existing.add(ColorUtil.colorize(line));
            }
            meta.setLore(existing);
        }
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        if (meta != null && customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        return this;
    }

    public ItemBuilder glowing(boolean glowing) {
        if (meta != null) {
            try {
                // Native 1.20.5+ and 1.21+ (Mounts of Mayhem / Tricky Trials)
                java.lang.reflect.Method glintMethod = meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class);
                glintMethod.invoke(meta, glowing);
            } catch (Throwable ignored) {
                // Fallback for 1.20 - 1.20.4
                if (glowing) {
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null && flags != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder hideAllAttributes() {
        if (meta != null) {
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS
            );
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
