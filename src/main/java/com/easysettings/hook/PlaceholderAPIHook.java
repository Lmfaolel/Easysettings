package com.easysettings.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles soft integration with PlaceholderAPI if present on the server.
 */
public class PlaceholderAPIHook {

    private final boolean enabled;

    public PlaceholderAPIHook() {
        this.enabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Parses placeholders in a string.
     *
     * @param player Player to parse for
     * @param text   Text containing placeholders
     * @return Text with parsed placeholders
     */
    public String parse(Player player, String text) {
        if (text == null) {
            return null;
        }

        if (player != null) {
            text = text.replace("%player%", player.getName());
        }

        if (enabled && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    /**
     * Parses placeholders in a list of strings.
     *
     * @param player Player to parse for
     * @param lines  Lines containing placeholders
     * @return List of lines with parsed placeholders
     */
    public List<String> parse(Player player, List<String> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(parse(player, line));
        }
        return result;
    }
}
