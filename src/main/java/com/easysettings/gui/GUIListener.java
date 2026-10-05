package com.easysettings.gui;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import com.easysettings.api.event.PlayerSettingClickEvent;
import com.easysettings.api.event.PlayerSettingToggleEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles inventory clicks and interactions within EasySettings menus.
 */
public class GUIListener implements Listener {

    private final EasySettings plugin;

    public GUIListener(EasySettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SettingsHolder)) {
            return;
        }

        // Always prevent taking or moving items in EasySettings GUI
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        SettingsHolder holder = (SettingsHolder) event.getInventory().getHolder();

        // Only process clicks within the top inventory
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) {
            return;
        }

        // Close button check
        if (rawSlot == holder.getCloseButtonSlot()) {
            player.closeInventory();
            return;
        }

        Setting setting = holder.getSettingAt(rawSlot);
        if (setting == null) {
            return;
        }

        // Permission check
        if (setting.getPermission() != null && !setting.getPermission().trim().isEmpty()) {
            if (!player.hasPermission(setting.getPermission())) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return;
            }
        }

        // Call Custom Click Event
        PlayerSettingClickEvent clickEvent = new PlayerSettingClickEvent(player, setting, event.getClick());
        Bukkit.getPluginManager().callEvent(clickEvent);
        if (clickEvent.isCancelled()) {
            return;
        }

        // Custom API click handler
        if (setting.getClickHandler() != null) {
            try {
                setting.getClickHandler().onClick(player, setting, event.getClick());
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing custom click handler for setting " + setting.getId() + ": " + e.getMessage());
            }
        }

        if (setting.getType() == SettingType.TOGGLE) {
            handleToggle(player, setting, event);
        } else if (setting.getType() == SettingType.ACTION) {
            handleAction(player, setting);
        }
    }

    private void handleToggle(Player player, Setting setting, InventoryClickEvent event) {
        boolean oldState = plugin.getPlayerDataManager().getState(
                player.getUniqueId(),
                setting.getId(),
                setting.isDefaultState()
        );
        boolean newState = !oldState;

        PlayerSettingToggleEvent toggleEvent = new PlayerSettingToggleEvent(player, setting, newState);
        Bukkit.getPluginManager().callEvent(toggleEvent);
        if (toggleEvent.isCancelled()) {
            return;
        }

        newState = toggleEvent.getNewState();
        plugin.getPlayerDataManager().setState(player.getUniqueId(), setting.getId(), newState);
        plugin.getPlayerDataManager().saveDataAsync();

        // Execute toggle actions
        if (newState) {
            plugin.getActionManager().executeActions(player, setting.getActionsOn());
        } else {
            plugin.getActionManager().executeActions(player, setting.getActionsOff());
        }

        if (!setting.getActions().isEmpty()) {
            plugin.getActionManager().executeActions(player, setting.getActions());
        }

        // Custom API toggle handler
        if (setting.getToggleHandler() != null) {
            try {
                setting.getToggleHandler().onToggle(player, setting, newState);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing custom toggle handler for setting " + setting.getId() + ": " + e.getMessage());
            }
        }

        // Refresh the single slot in the open inventory directly
        ItemStack updatedItem = plugin.getGuiManager().createSettingItem(player, setting);
        event.getInventory().setItem(event.getRawSlot(), updatedItem);
    }

    private void handleAction(Player player, Setting setting) {
        plugin.getActionManager().executeActions(player, setting.getActions());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SettingsHolder) {
            event.setCancelled(true);
        }
    }
}
