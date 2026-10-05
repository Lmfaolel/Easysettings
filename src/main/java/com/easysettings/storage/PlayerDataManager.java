package com.easysettings.storage;

import com.easysettings.EasySettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages per-player persistent setting states (for TOGGLE settings).
 */
public class PlayerDataManager {

    private final EasySettings plugin;
    private final File dataFile;
    private final Map<UUID, Map<String, Boolean>> playerStates = new ConcurrentHashMap<>();

    public PlayerDataManager(EasySettings plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    /**
     * Retrieves the state of a setting for a player.
     *
     * @param uuid         Player UUID
     * @param settingId    Setting ID
     * @param defaultValue Default state if not previously set
     * @return Current state
     */
    public boolean getState(UUID uuid, String settingId, boolean defaultValue) {
        Map<String, Boolean> states = playerStates.get(uuid);
        if (states == null || !states.containsKey(settingId.toLowerCase())) {
            return defaultValue;
        }
        return states.get(settingId.toLowerCase());
    }

    /**
     * Sets the state of a setting for a player.
     *
     * @param uuid      Player UUID
     * @param settingId Setting ID
     * @param state     New state
     */
    public void setState(UUID uuid, String settingId, boolean state) {
        playerStates.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(settingId.toLowerCase(), state);
    }

    /**
     * Toggles the state of a setting for a player and returns the new state.
     *
     * @param uuid         Player UUID
     * @param settingId    Setting ID
     * @param defaultValue Default state if not previously set
     * @return New toggled state
     */
    public boolean toggleState(UUID uuid, String settingId, boolean defaultValue) {
        boolean current = getState(uuid, settingId, defaultValue);
        boolean newState = !current;
        setState(uuid, settingId, newState);
        return newState;
    }

    /**
     * Loads player data from playerdata.yml.
     */
    public synchronized void loadData() {
        playerStates.clear();
        if (!dataFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (String uuidStr : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection settingsSec = playersSection.getConfigurationSection(uuidStr);
                if (settingsSec != null) {
                    Map<String, Boolean> states = new ConcurrentHashMap<>();
                    for (String key : settingsSec.getKeys(false)) {
                        states.put(key.toLowerCase(), settingsSec.getBoolean(key));
                    }
                    playerStates.put(uuid, states);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid UUID strings
            }
        }
    }

    /**
     * Saves player data to disk synchronously.
     */
    public synchronized void saveData() {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Boolean>> entry : playerStates.entrySet()) {
            String path = "players." + entry.getKey().toString();
            for (Map.Entry<String, Boolean> settingEntry : entry.getValue().entrySet()) {
                config.set(path + "." + settingEntry.getKey(), settingEntry.getValue());
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml!", e);
        }
    }

    /**
     * Saves player data asynchronously.
     */
    public void saveDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveData);
    }
}
