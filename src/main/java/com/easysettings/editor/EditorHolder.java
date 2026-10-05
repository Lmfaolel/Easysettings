package com.easysettings.editor;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * InventoryHolder for the main /setting editor GUI.
 */
public class EditorHolder implements InventoryHolder {

    private final Player player;
    private Inventory inventory;
    private final Map<Integer, Setting> slotToSettingMap = new HashMap<>();
    private Setting movingSetting = null;

    public EditorHolder(Player player) {
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

    public Setting getMovingSetting() {
        return movingSetting;
    }

    public void setMovingSetting(Setting movingSetting) {
        this.movingSetting = movingSetting;
    }
}
