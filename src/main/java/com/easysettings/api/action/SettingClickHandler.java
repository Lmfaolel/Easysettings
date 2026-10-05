package com.easysettings.api.action;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Functional interface called when a player clicks a specific setting.
 */
@FunctionalInterface
public interface SettingClickHandler {

    /**
     * Called when a player clicks the setting item in the GUI.
     *
     * @param player    Player who clicked
     * @param setting   Setting that was clicked
     * @param clickType Type of click (e.g. LEFT, RIGHT, SHIFT_LEFT)
     */
    void onClick(Player player, Setting setting, ClickType clickType);
}
