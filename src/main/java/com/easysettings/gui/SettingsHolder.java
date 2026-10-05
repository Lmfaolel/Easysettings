package com.easysettings.gui;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom InventoryHolder to identify EasySettings menus and track displayed settings.
 */
public class SettingsHolder implements InventoryHolder {

    private final Player player;
    private Inventory inventory;
    private final Map<Integer, Setting> slotToSettingMap = new HashMap<>();
    private int closeButtonSlot = -1;

    public SettingsHolder(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void registerSetting(int slot, Setting setting) {
        slotToSettingMap.put(slot, setting);
    }

    public Setting getSettingAt(int slot) {
        return slotToSettingMap.get(slot);
    }

    public int getCloseButtonSlot() {
        return closeButtonSlot;
    }

    public void setCloseButtonSlot(int closeButtonSlot) {
        this.closeButtonSlot = closeButtonSlot;
    }
}
