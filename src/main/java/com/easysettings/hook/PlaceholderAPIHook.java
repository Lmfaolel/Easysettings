package com.easysettings.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles soft integration with PlaceholderAPI with zero-allocation fast paths.
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
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Fast-path: If string doesn't contain '%', there are no placeholders to replace!
        if (text.indexOf('%') == -1) {
            return text;
        }

        if (player != null && text.contains("%player%")) {
            text = text.replace("%player%", player.getName());
        }

        if (enabled && player != null && text.indexOf('%') != -1) {
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
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(parse(player, line));
        }
        return result;
    }
}
