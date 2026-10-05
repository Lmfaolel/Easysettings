package com.easysettings.command;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides tab completion for the /setting command.
 */
public class SettingTabCompleter implements TabCompleter {

    private final EasySettings plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("editor", "add", "delete", "edit", "list", "reload", "help");
    private static final List<String> PROPERTIES = Arrays.asList("displayname", "item", "slot", "action", "description", "type", "permission");
    private static final List<String> TYPES = Arrays.asList("TOGGLE", "ACTION");
    private static final List<String> ACTION_SUB = Arrays.asList("add", "clear");

    public SettingTabCompleter(EasySettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("easysettings.admin")) {
                StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
            }
            Collections.sort(completions);
            return completions;
        }

        if (!sender.hasPermission("easysettings.admin")) {
            return Collections.emptyList();
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            if (sub.equals("edit") || sub.equals("delete") || sub.equals("remove")) {
                List<String> settingIds = new ArrayList<>();
                for (Setting s : plugin.getSettingManager().getAllSettings()) {
                    settingIds.add(s.getId());
                }
                StringUtil.copyPartialMatches(args[1], settingIds, completions);
                Collections.sort(completions);
                return completions;
            }
        }

        if (args.length == 3 && sub.equals("edit")) {
            StringUtil.copyPartialMatches(args[2], PROPERTIES, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 4 && sub.equals("edit")) {
            String prop = args[2].toLowerCase();
            if (prop.equals("item") || prop.equals("material")) {
                List<String> materials = new ArrayList<>();
                materials.add("hand");
                for (Material m : Material.values()) {
                    if (m.isItem()) {
                        materials.add(m.name().toLowerCase());
                    }
                }
                StringUtil.copyPartialMatches(args[3], materials, completions);
                Collections.sort(completions);
                return completions;
            } else if (prop.equals("type")) {
                StringUtil.copyPartialMatches(args[3], TYPES, completions);
                Collections.sort(completions);
                return completions;
            } else if (prop.equals("action")) {
                StringUtil.copyPartialMatches(args[3], ACTION_SUB, completions);
                Collections.sort(completions);
                return completions;
            }
        }

        return Collections.emptyList();
    }
}
