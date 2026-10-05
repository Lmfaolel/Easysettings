package com.easysettings.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when EasySettings configuration and settings are reloaded.
 */
public class EasySettingsReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public EasySettingsReloadEvent() {
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
