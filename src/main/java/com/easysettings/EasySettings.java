package com.easysettings;

import com.easysettings.action.ActionManager;
import com.easysettings.api.EasySettingsAPI;
import com.easysettings.command.SettingCommand;
import com.easysettings.command.SettingTabCompleter;
import com.easysettings.config.ConfigManager;
import com.easysettings.config.SettingManager;
import com.easysettings.gui.GUIListener;
import com.easysettings.gui.GUIManager;
import com.easysettings.hook.PlaceholderAPIHook;
import com.easysettings.storage.PlayerDataManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for EasySettings.
 */
public class EasySettings extends JavaPlugin {

    private static EasySettings instance;

    private ConfigManager configManager;
    private SettingManager settingManager;
    private PlayerDataManager playerDataManager;
    private ActionManager actionManager;
    private GUIManager guiManager;
    private PlaceholderAPIHook placeholderHook;
    private com.easysettings.editor.EditorManager editorManager;

    @Override
    public void onEnable() {
        instance = this;
        EasySettingsAPI.setPlugin(this);

        // Initialize components
        this.placeholderHook = new PlaceholderAPIHook();
        this.playerDataManager = new PlayerDataManager(this);
        this.actionManager = new ActionManager(this);
        this.configManager = new ConfigManager(this);
        this.settingManager = new SettingManager(this);
        this.guiManager = new GUIManager(this);
        this.editorManager = new com.easysettings.editor.EditorManager(this);

        // Register GUI listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(editorManager.getChatInputManager(), this);
        getServer().getPluginManager().registerEvents(new com.easysettings.editor.EditorListener(this), this);

        // Register commands & completer
        PluginCommand command = getCommand("setting");
        if (command != null) {
            command.setExecutor(new SettingCommand(this));
            command.setTabCompleter(new SettingTabCompleter(this));
        }

        getLogger().info("========================================");
        getLogger().info("EasySettings v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info("Settings Loaded: " + settingManager.getAllSettings().size());
        getLogger().info("PlaceholderAPI: " + (placeholderHook.isEnabled() ? "Hooked" : "Not Found (Optional)"));
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // 1. Close any open EasySettings inventories to prevent ghost menus or dupe glitches on reload
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null) {
                org.bukkit.inventory.InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
                if (holder instanceof com.easysettings.gui.SettingsHolder
                        || holder instanceof com.easysettings.editor.EditorHolder
                        || holder instanceof com.easysettings.editor.InspectorHolder
                        || holder instanceof com.easysettings.editor.ActionEditorHolder
                        || holder instanceof com.easysettings.editor.LoreEditorHolder) {
                    player.closeInventory();
                }
            }
        }

        // 2. Synchronously save all player states to disk
        if (playerDataManager != null) {
            playerDataManager.saveData();
        }

        // 3. Cancel all running and queued tasks
        getServer().getScheduler().cancelTasks(this);

        // 4. Cleanly unregister all listeners to avoid memory leaks with Plugman
        org.bukkit.event.HandlerList.unregisterAll(this);

        // 5. Clean static references
        EasySettingsAPI.setPlugin(null);
        instance = null;

        getLogger().info("EasySettings has been safely disabled and unloaded!");
    }

    public static EasySettings getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public PlaceholderAPIHook getPlaceholderHook() {
        return placeholderHook;
    }

    public com.easysettings.editor.EditorManager getEditorManager() {
        return editorManager;
    }
}
