package com.easysettings.gui;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import com.easysettings.config.ConfigManager;
import com.easysettings.util.ColorUtil;
import com.easysettings.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages construction and opening of the EasySettings GUI for players.
 */
public class GUIManager {

    private final EasySettings plugin;

    public GUIManager(EasySettings plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the settings GUI for the given player.
     *
     * @param player Player to open GUI for
     */
    public void openGUI(Player player) {
        if (player == null) return;

        ConfigManager config = plugin.getConfigManager();
        int size = config.getGuiSize();
        String title = ColorUtil.colorize(plugin.getPlaceholderHook().parse(player, config.getGuiTitle()));

        SettingsHolder holder = new SettingsHolder(player);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        // Fill background if enabled
        if (config.isFillerEnabled()) {
            ItemStack filler = new ItemBuilder(config.getFillerMaterial())
                    .name(config.getFillerName())
                    .hideAllAttributes()
                    .build();

            for (int i = 0; i < size; i++) {
                inv.setItem(i, filler);
            }
        }

        // Close button
        if (config.isCloseButtonEnabled()) {
            int closeSlot = config.getCloseButtonSlot();
            if (closeSlot >= 0 && closeSlot < size) {
                holder.setCloseButtonSlot(closeSlot);
                ItemStack closeItem = new ItemBuilder(config.getCloseButtonMaterial())
                        .name(config.getCloseButtonName())
                        .lore(config.getCloseButtonLore())
                        .hideAllAttributes()
                        .build();
                inv.setItem(closeSlot, closeItem);
            }
        }

        // Populate settings
        for (Setting setting : plugin.getSettingManager().getAllSettings()) {
            // Check permission if specified
            if (setting.getPermission() != null && !setting.getPermission().trim().isEmpty()) {
                if (!player.hasPermission(setting.getPermission())) {
                    continue; // Skip settings the player cannot access
                }
            }

            int slot = setting.getSlot();
            if (slot < 0 || slot >= size) {
                continue; // Skip out-of-bounds slot
            }

            ItemStack icon = createSettingItem(player, setting);
            inv.setItem(slot, icon);
            holder.registerSetting(slot, setting);
        }

        player.openInventory(inv);
    }

    /**
     * Builds the ItemStack representation of a setting for a player.
     *
     * @param player  Player viewing the setting
     * @param setting Setting configuration
     * @return Formatted ItemStack
     */
    public ItemStack createSettingItem(Player player, Setting setting) {
        ConfigManager config = plugin.getConfigManager();
        Material mat = setting.getItem();
        boolean isToggle = (setting.getType() == SettingType.TOGGLE);
        boolean state = false;
        String statusText = "";

        if (isToggle) {
            state = plugin.getPlayerDataManager().getState(player.getUniqueId(), setting.getId(), setting.isDefaultState());
            statusText = state ? config.getStatusEnabled() : config.getStatusDisabled();

            if (state && setting.getItemOn() != null) {
                mat = setting.getItemOn();
            } else if (!state && setting.getItemOff() != null) {
                mat = setting.getItemOff();
            }
        }

        // Display Name
        String rawName = setting.getDisplayName();
        String parsedName = plugin.getPlaceholderHook().parse(player, rawName);
        parsedName = parsedName.replace("%status%", statusText);

        // Lore
        List<String> parsedLore = new ArrayList<>();
        for (String line : setting.getDescription()) {
            String parsedLine = line.replace("%status%", statusText);
            parsedLine = plugin.getPlaceholderHook().parse(player, parsedLine);
            parsedLore.add(parsedLine);
        }

        boolean glowing = setting.isGlowing() || (isToggle && state);

        return new ItemBuilder(mat)
                .name(parsedName)
                .lore(parsedLore)
                .customModelData(setting.getCustomModelData())
                .glowing(glowing)
                .hideAllAttributes()
                .build();
    }
}
