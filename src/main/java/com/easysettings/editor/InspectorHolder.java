package com.easysettings.editor;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * InventoryHolder for the Setting Inspector view.
 */
public class InspectorHolder implements InventoryHolder {

    private final Player player;
    private final Setting setting;
    private Inventory inventory;

    public InspectorHolder(Player player, Setting setting) {
        this.player = player;
        this.setting = setting;
    }

    public Player getPlayer() {
        return player;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
