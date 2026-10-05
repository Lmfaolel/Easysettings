package com.easysettings.api.action;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;

/**
 * Functional interface called when a player toggles a toggleable setting.
 */
@FunctionalInterface
public interface SettingToggleHandler {

    /**
     * Called when a player toggles this setting.
     *
     * @param player   Player who toggled the setting
     * @param setting  Setting that was toggled
     * @param newState The new boolean state (true = enabled, false = disabled)
     */
    void onToggle(Player player, Setting setting, boolean newState);
}
