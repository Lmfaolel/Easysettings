package com.easysettings.storage;

import com.easysettings.EasySettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * High-performance, zero-leak player data manager.
 * Features:
 * - In-memory cache for active players (unloads on quit to eliminate memory leaks)
 * - Debounced asynchronous disk flushing with atomic file replacement (zero main thread freeze)
 * - Safe fallback for offline queries
 */
public class PlayerDataManager implements Listener {

    private final EasySettings plugin;
    private final File dataFile;
    private final File tempFile;

    // In-memory cache for active players
    private final Map<UUID, Map<String, Boolean>> activePlayerStates = new ConcurrentHashMap<>();

    // Offline data cache
    private FileConfiguration cachedDiskConfig = new YamlConfiguration();

    // Debounce & background saving flags
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean isSaving = new AtomicBoolean(false);
    private BukkitTask debounceSaveTask = null;
    private BukkitTask autoSaveTask = null;

    public PlayerDataManager(EasySettings plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.tempFile = new File(plugin.getDataFolder(), "playerdata.yml.tmp");
        loadData();

        // Register join/quit listener for automatic memory management
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Schedule periodic auto-save every 60 seconds (1200 ticks) as safety net
        this.autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (dirty.get()) {
                saveData();
            }
        }, 1200L, 1200L);
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
        String key = settingId.toLowerCase();
        Map<String, Boolean> states = activePlayerStates.get(uuid);
        if (states != null) {
            Boolean val = states.get(key);
            if (val != null) {
                return val;
            }
        }

        // Check disk config if player isn't loaded in active cache
        if (cachedDiskConfig != null) {
            String path = "players." + uuid + "." + key;
            if (cachedDiskConfig.contains(path)) {
                boolean val = cachedDiskConfig.getBoolean(path);
                // Cache into memory if player is online
                if (Bukkit.getPlayer(uuid) != null) {
                    activePlayerStates.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, val);
                }
                return val;
            }
        }

        return defaultValue;
    }

    /**
     * Sets the state of a setting for a player and triggers debounced async flush.
     *
     * @param uuid      Player UUID
     * @param settingId Setting ID
     * @param state     New state
     */
    public void setState(UUID uuid, String settingId, boolean state) {
        String key = settingId.toLowerCase();
        activePlayerStates.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(key, state);

        // Update cached disk representation
        synchronized (this) {
            cachedDiskConfig.set("players." + uuid + "." + key, state);
        }

        // Mark dirty and schedule debounced async save
        markDirtyAndDebounce();
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
     * Marks data as dirty and debounces async save to coalesce rapid clicks.
     */
    public void markDirtyAndDebounce() {
        dirty.set(true);
        if (debounceSaveTask != null) {
            debounceSaveTask.cancel();
            debounceSaveTask = null;
        }
        if (!plugin.isEnabled()) {
            return;
        }
        try {
            // Save asynchronously 40 ticks (2.0 seconds) after last click
            debounceSaveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (dirty.get()) {
                    saveData();
                }
            }, 40L);
        } catch (Throwable ignored) {
            // Plugin might be disabling
        }
    }

    /**
     * Loads player data from playerdata.yml.
     */
    public synchronized void loadData() {
        activePlayerStates.clear();
        if (!dataFile.exists()) {
            cachedDiskConfig = new YamlConfiguration();
            return;
        }

        cachedDiskConfig = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection playersSection = cachedDiskConfig.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        // Preload online players into active cache
        for (Player online : Bukkit.getOnlinePlayers()) {
            loadPlayerIntoMemory(online.getUniqueId());
        }
    }

    private void loadPlayerIntoMemory(UUID uuid) {
        if (cachedDiskConfig == null) return;
        ConfigurationSection settingsSec = cachedDiskConfig.getConfigurationSection("players." + uuid);
        if (settingsSec != null) {
            Map<String, Boolean> states = new ConcurrentHashMap<>();
            for (String key : settingsSec.getKeys(false)) {
                states.put(key.toLowerCase(), settingsSec.getBoolean(key));
            }
            activePlayerStates.put(uuid, states);
        }
    }

    /**
     * Saves player data to disk safely and atomically.
     */
    public void saveData() {
        if (!dirty.get() && dataFile.exists()) {
            return;
        }

        if (!isSaving.compareAndSet(false, true)) {
            // Already saving in another thread, it will write current data
            return;
        }

        try {
            FileConfiguration toSave;
            synchronized (this) {
                // Create snapshot copy
                toSave = new YamlConfiguration();
                if (cachedDiskConfig != null) {
                    ConfigurationSection sec = cachedDiskConfig.getConfigurationSection("players");
                    if (sec != null) {
                        toSave.set("players", sec);
                    }
                }
                dirty.set(false);
            }

            // Write to temporary file first to guarantee atomic write
            toSave.save(tempFile);
            Files.move(tempFile.toPath(), dataFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not atomically save playerdata.yml!", e);
            dirty.set(true); // Re-mark dirty on failure
        } finally {
            isSaving.set(false);
        }
    }

    /**
     * Saves player data asynchronously.
     */
    public void saveDataAsync() {
        markDirtyAndDebounce();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerIntoMemory(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Memory leak prevention: unload active memory cache on quit after ensuring states are saved
        Map<String, Boolean> states = activePlayerStates.remove(uuid);
        if (states != null && !states.isEmpty()) {
            synchronized (this) {
                for (Map.Entry<String, Boolean> entry : states.entrySet()) {
                    cachedDiskConfig.set("players." + uuid + "." + entry.getKey(), entry.getValue());
                }
            }
            markDirtyAndDebounce();
        }
    }

    /**
     * Cleans up tasks and flushes pending data on plugin disable.
     */
    public void shutdown() {
        if (debounceSaveTask != null) {
            debounceSaveTask.cancel();
            debounceSaveTask = null;
        }
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        if (dirty.get()) {
            saveData();
        }
        activePlayerStates.clear();
    }
}
