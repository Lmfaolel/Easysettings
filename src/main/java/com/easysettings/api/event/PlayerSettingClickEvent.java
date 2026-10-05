package com.easysettings.api.event;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;

/**
 * Event called when a player clicks a setting in the EasySettings GUI.
 */
public class PlayerSettingClickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Setting setting;
    private final ClickType clickType;
    private boolean cancelled;

    public PlayerSettingClickEvent(Player player, Setting setting, ClickType clickType) {
        this.player = player;
        this.setting = setting;
        this.clickType = clickType;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Setting getSetting() {
        return setting;
    }

    public ClickType getClickType() {
        return clickType;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
