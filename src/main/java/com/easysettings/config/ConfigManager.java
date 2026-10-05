package com.easysettings.config;

import com.easysettings.EasySettings;
import com.easysettings.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles reading and providing main configuration values from config.yml.
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

    private boolean closeButtonEnabled;
    private int closeButtonSlot;
    private Material closeButtonMaterial;
    private String closeButtonName;
    private List<String> closeButtonLore;

    public ConfigManager(EasySettings plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // GUI configuration
        this.guiTitle = config.getString("gui.title", "&#00D2FF&lPlayer Settings");
        int rawSize = config.getInt("gui.size", 27);
        // Normalize size to valid inventory slot count (multiple of 9, between 9 and 54)
        if (rawSize < 9) rawSize = 9;
        if (rawSize > 54) rawSize = 54;
        if (rawSize % 9 != 0) {
            rawSize = ((rawSize / 9) + 1) * 9;
            if (rawSize > 54) rawSize = 54;
        }
        this.guiSize = rawSize;

        this.statusEnabled = config.getString("gui.status.enabled", "&#55FF55Enabled");
        this.statusDisabled = config.getString("gui.status.disabled", "&#FF5555Disabled");

        // Filler
        this.fillerEnabled = config.getBoolean("gui.filler.enabled", true);
        String fillerMatName = config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE");
        Material mat = Material.matchMaterial(fillerMatName);
        this.fillerMaterial = mat != null ? mat : Material.GRAY_STAINED_GLASS_PANE;
        this.fillerName = config.getString("gui.filler.name", " ");

        // Close button
        this.closeButtonEnabled = config.getBoolean("gui.close-button.enabled", true);
        this.closeButtonSlot = config.getInt("gui.close-button.slot", 22);
        String closeMatName = config.getString("gui.close-button.material", "BARRIER");
        Material closeMat = Material.matchMaterial(closeMatName);
        this.closeButtonMaterial = closeMat != null ? closeMat : Material.BARRIER;
        this.closeButtonName = config.getString("gui.close-button.name", "&#FF5555&lClose Menu");
        this.closeButtonLore = config.getStringList("gui.close-button.lore");
        if (this.closeButtonLore.isEmpty()) {
            this.closeButtonLore = Collections.singletonList("&7Click to close settings.");
        }
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

    /**
     * Gets a message string from config with prefix prepended.
     *
     * @param key Config message key
     * @return Formatted message
     */
    public String getMessage(String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String msg = plugin.getConfig().getString("messages." + key, "");
        return ColorUtil.colorize(prefix + msg);
    }

    /**
     * Gets a raw message without prefix.
     *
     * @param key Config message key
     * @return Formatted message
     */
    public String getRawMessage(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "");
        return ColorUtil.colorize(msg);
    }

    /**
     * Gets a list of messages (e.g. usage).
     *
     * @param key Config message list key
     * @return List of formatted messages
     */
    public List<String> getMessageList(String key) {
        List<String> list = plugin.getConfig().getStringList("messages." + key);
        List<String> result = new ArrayList<>();
        for (String line : list) {
            result.add(ColorUtil.colorize(line));
        }
        return result;
    }
}
