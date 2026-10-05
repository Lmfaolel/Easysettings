package com.easysettings.api;

import com.easysettings.EasySettings;
import com.easysettings.api.action.ActionHandler;
import com.easysettings.api.action.SettingClickHandler;
import com.easysettings.api.action.SettingToggleHandler;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * Public developer API for EasySettings.
 * Allows other plugins to easily add custom settings, action handlers, and click callbacks.
 */
public final class EasySettingsAPI {

    private static EasySettings plugin;

    private EasySettingsAPI() {
    }

    /**
     * Internal method to initialize the API instance.
     *
     * @param instance Plugin instance
     */
    public static void setPlugin(EasySettings instance) {
        plugin = instance;
    }

    /**
     * Gets the EasySettings plugin instance.
     *
     * @return Plugin instance
     */
    public static EasySettings getPlugin() {
        return plugin;
    }

    /**
     * Registers a custom setting created by another plugin.
     *
     * @param setting The Setting object to register
     */
    public static void registerSetting(Setting setting) {
        checkInitialized();
        plugin.getSettingManager().registerApiSetting(setting);
    }

    /**
     * Unregisters a setting by its ID.
     *
     * @param settingId The ID of the setting to unregister
     */
    public static void unregisterSetting(String settingId) {
        checkInitialized();
        plugin.getSettingManager().unregisterApiSetting(settingId);
    }

    /**
     * Gets a setting by ID.
     *
     * @param settingId Setting identifier
     * @return Setting or null if not found
     */
    public static Setting getSetting(String settingId) {
        checkInitialized();
        return plugin.getSettingManager().getSetting(settingId);
    }

    /**
     * Returns an unmodifiable collection of all registered settings.
     *
     * @return Collection of settings
     */
    public static Collection<Setting> getAllSettings() {
        checkInitialized();
        return plugin.getSettingManager().getAllSettings();
    }

    /**
     * Registers a custom action handler tag.
     * For example, registering "eco" will execute the handler for actions formatted as:
     * "[eco] give 100"
     *
     * @param tag     Action prefix without brackets
     * @param handler Action handler implementation
     */
    public static void registerActionHandler(String tag, ActionHandler handler) {
        checkInitialized();
        plugin.getActionManager().registerHandler(tag, handler);
    }

    /**
     * Unregisters a custom action handler.
     *
     * @param tag Action prefix
     */
    public static void unregisterActionHandler(String tag) {
        checkInitialized();
        plugin.getActionManager().unregisterHandler(tag);
    }

    /**
     * Attaches a click callback to an existing setting.
     *
     * @param settingId Setting ID
     * @param handler   Click handler callback
     */
    public static void registerClickHandler(String settingId, SettingClickHandler handler) {
        checkInitialized();
        Setting setting = plugin.getSettingManager().getSetting(settingId);
        if (setting != null) {
            setting.setClickHandler(handler);
        }
    }

    /**
     * Attaches a toggle callback to an existing toggle setting.
     *
     * @param settingId Setting ID
     * @param handler   Toggle handler callback
     */
    public static void registerToggleHandler(String settingId, SettingToggleHandler handler) {
        checkInitialized();
        Setting setting = plugin.getSettingManager().getSetting(settingId);
        if (setting != null) {
            setting.setToggleHandler(handler);
        }
    }

    /**
     * Checks if a toggle setting is enabled for a specific player.
     *
     * @param playerId  Player UUID
     * @param settingId Setting ID
     * @return true if enabled, false otherwise
     */
    public static boolean isSettingEnabled(UUID playerId, String settingId) {
        checkInitialized();
        Setting setting = plugin.getSettingManager().getSetting(settingId);
        boolean defaultState = setting != null && setting.isDefaultState();
        return plugin.getPlayerDataManager().getState(playerId, settingId, defaultState);
    }

    /**
     * Updates a player's setting state directly.
     *
     * @param playerId  Player UUID
     * @param settingId Setting ID
     * @param state     New state (true/false)
     */
    public static void setSettingEnabled(UUID playerId, String settingId, boolean state) {
        checkInitialized();
        plugin.getPlayerDataManager().setState(playerId, settingId, state);
        plugin.getPlayerDataManager().saveDataAsync();
    }

    /**
     * Programmatically toggles a setting for a player, triggering actions and events.
     *
     * @param player    Player
     * @param settingId Setting ID
     * @return The new state
     */
    public static boolean toggleSetting(Player player, String settingId) {
        checkInitialized();
        Setting setting = plugin.getSettingManager().getSetting(settingId);
        if (setting == null) return false;

        boolean oldState = plugin.getPlayerDataManager().getState(
                player.getUniqueId(),
                setting.getId(),
                setting.isDefaultState()
        );
        boolean newState = !oldState;
        plugin.getPlayerDataManager().setState(player.getUniqueId(), setting.getId(), newState);
        plugin.getPlayerDataManager().saveDataAsync();

        if (newState) {
            plugin.getActionManager().executeActions(player, setting.getActionsOn());
        } else {
            plugin.getActionManager().executeActions(player, setting.getActionsOff());
        }

        if (setting.getToggleHandler() != null) {
            setting.getToggleHandler().onToggle(player, setting, newState);
        }

        return newState;
    }

    /**
     * Opens the EasySettings GUI for a player.
     *
     * @param player Player
     */
    public static void openGUI(Player player) {
        checkInitialized();
        plugin.getGuiManager().openGUI(player);
    }

    private static void checkInitialized() {
        if (plugin == null) {
            throw new IllegalStateException("EasySettings plugin is not initialized yet!");
        }
    }
}
