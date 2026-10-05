package com.easysettings.config;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages loading, saving, creating, editing, and deleting settings.
 */
public class SettingManager {

    private final EasySettings plugin;
    private final File settingsFile;
    private FileConfiguration settingsConfig;

    // Map of settingId (lowercase) -> Setting
    private final Map<String, Setting> settings = new ConcurrentHashMap<>();
    // Keep track of settings registered dynamically by other plugins via API
    private final Map<String, Setting> apiSettings = new ConcurrentHashMap<>();

    public SettingManager(EasySettings plugin) {
        this.plugin = plugin;
        this.settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        loadSettings();
    }

    /**
     * Loads settings from settings.yml.
     */
    public synchronized void loadSettings() {
        if (!settingsFile.exists()) {
            plugin.saveResource("settings.yml", false);
        }

        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        settings.clear();

        ConfigurationSection section = settingsConfig.getConfigurationSection("settings");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection s = section.getConfigurationSection(key);
                if (s == null) continue;

                Setting setting = new Setting(key);
                setting.setDisplayName(s.getString("name", "&e" + key));
                setting.setSlot(s.getInt("slot", 0));

                String typeStr = s.getString("type", "TOGGLE");
                try {
                    setting.setType(SettingType.valueOf(typeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    setting.setType(SettingType.TOGGLE);
                }

                String itemStr = s.getString("item", "STONE");
                Material mat = Material.matchMaterial(itemStr);
                setting.setItem(mat != null ? mat : Material.STONE);

                if (s.contains("item-on")) {
                    Material matOn = Material.matchMaterial(s.getString("item-on", ""));
                    if (matOn != null) setting.setItemOn(matOn);
                }

                if (s.contains("item-off")) {
                    Material matOff = Material.matchMaterial(s.getString("item-off", ""));
                    if (matOff != null) setting.setItemOff(matOff);
                }

                setting.setCustomModelData(s.getInt("custom-model-data", 0));
                setting.setGlowing(s.getBoolean("glowing", false));
                setting.setDefaultState(s.getBoolean("default-state", false));
                setting.setPermission(s.getString("permission", ""));
                setting.setDescription(s.getStringList("description"));
                setting.setActions(s.getStringList("actions"));
                setting.setActionsOn(s.getStringList("actions-on"));
                setting.setActionsOff(s.getStringList("actions-off"));
                setting.setFromConfig(true);

                settings.put(key.toLowerCase(), setting);
            }
        }

        // Re-apply API-registered settings
        for (Map.Entry<String, Setting> entry : apiSettings.entrySet()) {
            settings.put(entry.getKey(), entry.getValue());
        }

        plugin.getLogger().info("Loaded " + settings.size() + " settings (" + apiSettings.size() + " from API).");
    }

    /**
     * Registers a setting from an external plugin API.
     *
     * @param setting Setting to register
     */
    public void registerApiSetting(Setting setting) {
        if (setting == null || setting.getId() == null) return;
        setting.setFromConfig(false);
        String idKey = setting.getId().toLowerCase();
        apiSettings.put(idKey, setting);
        settings.put(idKey, setting);
    }

    /**
     * Unregisters an API setting.
     *
     * @param id Setting ID
     */
    public void unregisterApiSetting(String id) {
        if (id == null) return;
        String idKey = id.toLowerCase();
        apiSettings.remove(idKey);
        settings.remove(idKey);
    }

    /**
     * Retrieves a setting by ID (case-insensitive).
     *
     * @param id Setting ID
     * @return Setting or null
     */
    public Setting getSetting(String id) {
        if (id == null) return null;
        return settings.get(id.toLowerCase());
    }

    /**
     * Checks if a setting exists.
     *
     * @param id Setting ID
     * @return true if exists
     */
    public boolean hasSetting(String id) {
        if (id == null) return false;
        return settings.containsKey(id.toLowerCase());
    }

    /**
     * Gets all registered settings.
     *
     * @return Collection of settings
     */
    public Collection<Setting> getAllSettings() {
        return Collections.unmodifiableCollection(settings.values());
    }

    /**
     * Creates and saves a new setting.
     *
     * @param setting Setting to add
     * @return true if added successfully, false if already exists
     */
    public synchronized boolean addSetting(Setting setting) {
        if (setting == null || setting.getId() == null) return false;
        String idKey = setting.getId().toLowerCase();
        if (settings.containsKey(idKey)) {
            return false;
        }

        setting.setFromConfig(true);
        settings.put(idKey, setting);
        saveSettingToConfig(setting);
        return true;
    }

    /**
     * Deletes a setting by ID.
     *
     * @param id Setting ID
     * @return true if deleted, false if not found
     */
    public synchronized boolean deleteSetting(String id) {
        if (id == null) return false;
        String idKey = id.toLowerCase();
        Setting removed = settings.remove(idKey);
        apiSettings.remove(idKey);

        if (removed != null) {
            if (settingsConfig != null) {
                settingsConfig.set("settings." + removed.getId(), null);
                saveConfigFile();
            }
            return true;
        }
        return false;
    }

    /**
     * Saves a setting to settings.yml.
     *
     * @param setting Setting to save
     */
    public synchronized void saveSettingToConfig(Setting setting) {
        if (setting == null || settingsConfig == null) return;

        String path = "settings." + setting.getId();
        settingsConfig.set(path + ".name", setting.getDisplayName());
        settingsConfig.set(path + ".slot", setting.getSlot());
        settingsConfig.set(path + ".type", setting.getType().name());
        settingsConfig.set(path + ".item", setting.getItem().name());

        if (setting.getItemOn() != null) {
            settingsConfig.set(path + ".item-on", setting.getItemOn().name());
        }
        if (setting.getItemOff() != null) {
            settingsConfig.set(path + ".item-off", setting.getItemOff().name());
        }

        if (setting.getCustomModelData() > 0) {
            settingsConfig.set(path + ".custom-model-data", setting.getCustomModelData());
        }
        settingsConfig.set(path + ".glowing", setting.isGlowing());
        settingsConfig.set(path + ".default-state", setting.isDefaultState());
        settingsConfig.set(path + ".permission", setting.getPermission());
        settingsConfig.set(path + ".description", setting.getDescription());

        if (setting.getType() == SettingType.ACTION) {
            settingsConfig.set(path + ".actions", setting.getActions());
            settingsConfig.set(path + ".actions-on", null);
            settingsConfig.set(path + ".actions-off", null);
        } else {
            settingsConfig.set(path + ".actions-on", setting.getActionsOn());
            settingsConfig.set(path + ".actions-off", setting.getActionsOff());
            if (!setting.getActions().isEmpty()) {
                settingsConfig.set(path + ".actions", setting.getActions());
            }
        }

        saveConfigFile();
    }

    private void saveConfigFile() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save settings.yml!", e);
        }
    }

    /**
     * Finds the next available slot that isn't occupied.
     *
     * @param maxSlots Total size of the GUI
     * @return Next free slot index, or -1 if full
     */
    public int getNextFreeSlot(int maxSlots) {
        Set<Integer> occupiedSlots = new HashSet<>();
        for (Setting s : settings.values()) {
            occupiedSlots.add(s.getSlot());
        }
        // Also reserve close button slot if enabled
        if (plugin.getConfigManager().isCloseButtonEnabled()) {
            occupiedSlots.add(plugin.getConfigManager().getCloseButtonSlot());
        }

        for (int i = 0; i < maxSlots; i++) {
            if (!occupiedSlots.contains(i)) {
                return i;
            }
        }
        return -1;
    }
}
