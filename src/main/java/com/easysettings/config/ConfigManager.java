package com.easysettings.config;

import com.easysettings.EasySettings;
import com.easysettings.util.ColorUtil;
import com.easysettings.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles reading, caching, and serving main configuration values from config.yml with zero garbage.
 */
public class ConfigManager {

    private final EasySettings plugin;

    private String guiTitle;
    private int guiSize;
    private String statusEnabled;
    private String statusDisabled;

    private boolean fillerEnabled;
    private Material fillerMaterial;
    private String fillerName;
    private ItemStack cachedFillerItem;

    private boolean closeButtonEnabled;
    private int closeButtonSlot;
    private Material closeButtonMaterial;
    private String closeButtonName;
    private List<String> closeButtonLore;
    private ItemStack cachedCloseItem;

    // Cache for messages
    private final Map<String, String> messageCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> messageListCache = new ConcurrentHashMap<>();

    public ConfigManager(EasySettings plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        messageCache.clear();
        messageListCache.clear();
        ColorUtil.clearCache();

        // GUI configuration
        this.guiTitle = config.getString("gui.title", "&#66FCF1&lᴘʟᴀʏᴇʀ sᴇᴛᴛɪɴɢs");
        int rawSize = config.getInt("gui.size", 27);
        if (rawSize < 9) rawSize = 9;
        if (rawSize > 54) rawSize = 54;
        if (rawSize % 9 != 0) {
            rawSize = ((rawSize / 9) + 1) * 9;
            if (rawSize > 54) rawSize = 54;
        }
        this.guiSize = rawSize;

        this.statusEnabled = config.getString("gui.status.enabled", "&#51CF66● ᴇɴᴀʙʟᴇᴅ");
        this.statusDisabled = config.getString("gui.status.disabled", "&#FF6B6B○ ᴅɪsᴀʙʟᴇᴅ");

        // Filler
        this.fillerEnabled = config.getBoolean("gui.filler.enabled", true);
        String fillerMatName = config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE");
        Material mat = Material.matchMaterial(fillerMatName);
        this.fillerMaterial = mat != null ? mat : Material.GRAY_STAINED_GLASS_PANE;
        this.fillerName = config.getString("gui.filler.name", " ");

        // Pre-build cached filler ItemStack to avoid rebuilding 54 times per GUI open
        this.cachedFillerItem = new ItemBuilder(this.fillerMaterial)
                .name(this.fillerName)
                .hideAllAttributes()
                .build();

        // Close button
        this.closeButtonEnabled = config.getBoolean("gui.close-button.enabled", true);
        this.closeButtonSlot = config.getInt("gui.close-button.slot", 22);
        String closeMatName = config.getString("gui.close-button.material", "BARRIER");
        Material closeMat = Material.matchMaterial(closeMatName);
        this.closeButtonMaterial = closeMat != null ? closeMat : Material.BARRIER;
        this.closeButtonName = config.getString("gui.close-button.name", "&#FF6B6B&l✕ ᴄʟᴏsᴇ ᴍᴇɴᴜ");
        this.closeButtonLore = config.getStringList("gui.close-button.lore");
        if (this.closeButtonLore.isEmpty()) {
            this.closeButtonLore = Collections.singletonList("&#868E96Click to exit the settings menu.");
        }

        // Pre-build cached close button
        this.cachedCloseItem = new ItemBuilder(this.closeButtonMaterial)
                .name(this.closeButtonName)
                .lore(this.closeButtonLore)
                .hideAllAttributes()
                .build();
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiSize() {
        return guiSize;
    }

    public String getStatusEnabled() {
        return statusEnabled;
    }

    public String getStatusDisabled() {
        return statusDisabled;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public String getFillerName() {
        return fillerName;
    }

    public ItemStack getCachedFillerItem() {
        return cachedFillerItem != null ? cachedFillerItem.clone() : new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    }

    public boolean isCloseButtonEnabled() {
        return closeButtonEnabled;
    }

    public int getCloseButtonSlot() {
        return closeButtonSlot;
    }

    public Material getCloseButtonMaterial() {
        return closeButtonMaterial;
    }

    public String getCloseButtonName() {
        return closeButtonName;
    }

    public List<String> getCloseButtonLore() {
        return closeButtonLore;
    }

    public ItemStack getCachedCloseItem() {
        return cachedCloseItem != null ? cachedCloseItem.clone() : new ItemStack(Material.BARRIER);
    }

    /**
     * Gets a cached message string from config with prefix prepended.
     *
     * @param key Config message key
     * @return Formatted message
     */
    public String getMessage(String key) {
        return messageCache.computeIfAbsent(key, k -> {
            String prefix = plugin.getConfig().getString("messages.prefix", "");
            String msg = plugin.getConfig().getString("messages." + k, "");
            return ColorUtil.colorize(prefix + msg);
        });
    }

    /**
     * Gets a list of messages with caching.
     *
     * @param key Config message list key
     * @return List of formatted messages
     */
    public List<String> getMessageList(String key) {
        return messageListCache.computeIfAbsent(key, k -> {
            List<String> list = plugin.getConfig().getStringList("messages." + k);
            List<String> result = new ArrayList<>(list.size());
            for (String line : list) {
                result.add(ColorUtil.colorize(line));
            }
            return Collections.unmodifiableList(result);
        });
    }
}
