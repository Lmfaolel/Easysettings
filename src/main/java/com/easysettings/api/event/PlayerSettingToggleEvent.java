package com.easysettings.api.event;

import com.easysettings.api.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player toggles a TOGGLE type setting.
 */
public class PlayerSettingToggleEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Setting setting;
    private boolean newState;
    private boolean cancelled;

    public PlayerSettingToggleEvent(Player player, Setting setting, boolean newState) {
        this.player = player;
        this.setting = setting;
        this.newState = newState;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Setting getSetting() {
        return setting;
    }

    public boolean getNewState() {
        return newState;
    }

    public void setNewState(boolean newState) {
        this.newState = newState;
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
