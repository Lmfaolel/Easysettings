package com.easysettings.api.action;

import org.bukkit.entity.Player;

/**
 * Functional interface for custom action handlers registered by other plugins.
 * For example: [economy] take 100
 */
@FunctionalInterface
public interface ActionHandler {

    /**
     * Executes the custom action for the given player.
     *
     * @param player   Player who triggered the action
     * @param argument Argument passed after the action tag
     */
    void handle(Player player, String argument);
}
