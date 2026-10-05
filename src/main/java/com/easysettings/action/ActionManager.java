package com.easysettings.action;

import com.easysettings.EasySettings;
import com.easysettings.api.action.ActionHandler;
import com.easysettings.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes configured and custom actions for players when settings are interacted with.
 */
public class ActionManager {

    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\[([a-zA-Z0-9_-]+)\\]\\s*(.*)$");

    private final EasySettings plugin;
    private final Map<String, ActionHandler> customActionHandlers = new ConcurrentHashMap<>();

    public ActionManager(EasySettings plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a custom action handler for a tag (e.g., [eco] or [api]).
     *
     * @param tag     Action prefix tag (case-insensitive)
     * @param handler Handler to execute
     */
    public void registerHandler(String tag, ActionHandler handler) {
        if (tag != null && handler != null) {
            customActionHandlers.put(tag.toLowerCase(), handler);
        }
    }

    /**
     * Unregisters a custom action handler.
     *
     * @param tag Action prefix tag
     */
    public void unregisterHandler(String tag) {
        if (tag != null) {
            customActionHandlers.remove(tag.toLowerCase());
        }
    }

    /**
     * Executes a list of action strings for a player.
     *
     * @param player  Player to execute actions for
     * @param actions List of action strings
     */
    public void executeActions(Player player, List<String> actions) {
        if (player == null || actions == null || actions.isEmpty()) {
            return;
        }

        for (String action : actions) {
            executeAction(player, action);
        }
    }

    /**
     * Executes a single action string for a player.
     *
     * @param player    Player to execute action for
     * @param rawAction Action string
     */
    public void executeAction(Player player, String rawAction) {
        if (player == null || rawAction == null || rawAction.trim().isEmpty()) {
            return;
        }

        String trimmed = rawAction.trim();
        Matcher matcher = ACTION_PATTERN.matcher(trimmed);

        if (matcher.matches()) {
            String type = matcher.group(1).toLowerCase();
            String argument = matcher.group(2).trim();

            // Replace placeholders
            argument = plugin.getPlaceholderHook().parse(player, argument);

            switch (type) {
                case "player":
                    executePlayerCommand(player, argument);
                    break;
                case "console":
                    executeConsoleCommand(argument);
                    break;
                case "message":
                    sendMessage(player, argument);
                    break;
                case "broadcast":
                    broadcastMessage(player, argument);
                    break;
                case "sound":
                    playSound(player, argument);
                    break;
                case "close":
                    closeInventory(player);
                    break;
                case "refresh":
                    refreshGUI(player);
                    break;
                case "api":
                    handleApiAction(player, argument);
                    break;
                default:
                    // Check custom handlers
                    ActionHandler customHandler = customActionHandlers.get(type);
                    if (customHandler != null) {
                        try {
                            customHandler.handle(player, argument);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error executing custom action [" + type + "]: " + e.getMessage());
                        }
                    } else {
                        plugin.getLogger().warning("Unknown action prefix: [" + type + "] in action: " + rawAction);
                    }
                    break;
            }
        } else {
            // Fallback: if starts with '/', run as player command
            if (trimmed.startsWith("/")) {
                executePlayerCommand(player, trimmed.substring(1));
            } else {
                sendMessage(player, trimmed);
            }
        }
    }

    private void executePlayerCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        final String cmd = command;
        Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(cmd));
    }

    private void executeConsoleCommand(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        final String cmd = command;
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(ColorUtil.colorize(message));
    }

    private void broadcastMessage(Player player, String message) {
        Bukkit.broadcastMessage(ColorUtil.colorize(message));
    }

    private void playSound(Player player, String soundArg) {
        try {
            String[] parts = soundArg.split(":");
            String soundName = parts[0].toUpperCase();
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;

            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound or pitch/volume in action: " + soundArg);
        }
    }

    private void closeInventory(Player player) {
        Bukkit.getScheduler().runTask(plugin, player::closeInventory);
    }

    private void refreshGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getGuiManager().openGUI(player));
    }

    private void handleApiAction(Player player, String argument) {
        String key = argument;
        String extraArg = "";
        if (argument.contains(" ")) {
            int spaceIdx = argument.indexOf(' ');
            key = argument.substring(0, spaceIdx);
            extraArg = argument.substring(spaceIdx + 1);
        } else if (argument.contains(":")) {
            int colonIdx = argument.indexOf(':');
            key = argument.substring(0, colonIdx);
            extraArg = argument.substring(colonIdx + 1);
        }

        ActionHandler handler = customActionHandlers.get(key.toLowerCase());
        if (handler != null) {
            try {
                handler.handle(player, extraArg);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing API action '" + key + "': " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("No API action handler registered for key: '" + key + "'");
        }
    }
}
