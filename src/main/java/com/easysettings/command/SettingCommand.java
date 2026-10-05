package com.easysettings.command;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import com.easysettings.api.event.EasySettingsReloadEvent;
import com.easysettings.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles the /setting command and all its subcommands.
 */
public class SettingCommand implements CommandExecutor {

    private final EasySettings plugin;

    public SettingCommand(EasySettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // No args: open GUI
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("easysettings.use")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            plugin.getGuiManager().openGUI(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                handleReload(sender);
                return true;

            case "list":
                handleList(sender);
                return true;

            case "add":
                handleAdd(sender, args);
                return true;

            case "delete":
            case "remove":
                handleDelete(sender, args);
                return true;

            case "editor":
                handleEditor(sender);
                return true;

            case "edit":
                if (args.length == 1 && sender instanceof Player) {
                    handleEditor(sender);
                    return true;
                }
                handleEdit(sender, args);
                return true;

            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("unknown-command"));
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        for (String line : plugin.getConfigManager().getMessageList("usage")) {
            sender.sendMessage(line);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("easysettings.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        plugin.getConfigManager().loadConfig();
        plugin.getSettingManager().loadSettings();
        Bukkit.getPluginManager().callEvent(new EasySettingsReloadEvent());

        // Refresh open menus for any online players viewing settings
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() != null) {
                if (p.getOpenInventory().getTopInventory().getHolder() instanceof com.easysettings.gui.SettingsHolder) {
                    plugin.getGuiManager().openGUI(p);
                }
            }
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("easysettings.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        sender.sendMessage(ColorUtil.colorize("&#495057&m──────────────────────────────────────────────────"));
        sender.sendMessage(ColorUtil.colorize("&#66FCF1&lᴇᴀsʏsᴇᴛᴛɪɴɢs &#868E96│ &#CED4DAʀᴇɢɪsᴛᴇʀᴇᴅ sᴇᴛᴛɪɴɢs"));
        if (plugin.getSettingManager().getAllSettings().isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(" &#868E96No settings are currently registered."));
        } else {
            for (Setting s : plugin.getSettingManager().getAllSettings()) {
                String source = s.isFromConfig() ? "&#868E96[ᴄᴏɴғɪɢ]" : "&#51CF66[ᴀᴘɪ]";
                sender.sendMessage(ColorUtil.colorize(" &#495057• &#66FCF1" + s.getId() + " &8» &#CED4DA" + s.getDisplayName() + " &#868E96│ sʟᴏᴛ: &#CED4DA#" + s.getSlot() + " &#868E96│ " + source));
            }
        }
        sender.sendMessage(ColorUtil.colorize("&#495057&m──────────────────────────────────────────────────"));
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("easysettings.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAᴜsᴀɢᴇ: &#66FCF1/setting add <setting_name>"));
            return;
        }

        String id = args[1].toLowerCase().replace(" ", "_");
        if (plugin.getSettingManager().hasSetting(id)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("setting-already-exists")
                    .replace("%setting%", id));
            return;
        }

        int size = plugin.getConfigManager().getGuiSize();
        int slot = plugin.getSettingManager().getNextFreeSlot(size);
        if (slot == -1) {
            slot = 0;
            sender.sendMessage(ColorUtil.colorize("&#FFA94D⚠ &#CED4DAᴡᴀʀɴɪɴɢ: ɢᴜɪ ɪs ғᴜʟʟ. sᴇᴛᴛɪɴɢ ᴀssɪɢɴᴇᴅ ᴛᴏ sʟᴏᴛ #0."));
        }

        Setting setting = new Setting(id);
        setting.setDisplayName("&#66FCF1&l" + ColorUtil.toSmallCaps(id));
        setting.setSlot(slot);
        setting.setItem(Material.STONE);
        setting.setType(SettingType.TOGGLE);
        setting.setDescription(Arrays.asList(
                "&#868E96Toggle the " + id + " setting.",
                "",
                "&#495057│ &#868E96Status: %status%",
                "",
                "&#66FCF1▸ Click to toggle"
        ));
        setting.setActionsOn(Collections.singletonList("[message] &#66FCF1" + id + " &#51CF66● ᴇɴᴀʙʟᴇᴅ"));
        setting.setActionsOff(Collections.singletonList("[message] &#66FCF1" + id + " &#FF6B6B○ ᴅɪsᴀʙʟᴇᴅ"));

        plugin.getSettingManager().addSetting(setting);

        sender.sendMessage(plugin.getConfigManager().getMessage("setting-added")
                .replace("%setting%", id)
                .replace("%slot%", String.valueOf(slot)));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("easysettings.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&#FF5555Usage: /setting delete <setting_name>"));
            return;
        }

        String id = args[1].toLowerCase();
        boolean deleted = plugin.getSettingManager().deleteSetting(id);

        if (deleted) {
            sender.sendMessage(plugin.getConfigManager().getMessage("setting-deleted")
                    .replace("%setting%", id));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("setting-not-found")
                    .replace("%setting%", id));
        }
    }

    private void handleEditor(CommandSender sender) {
        if (!sender.hasPermission("easysettings.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return;
        }

        plugin.getEditorManager().openMainEditor((Player) sender);
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("easysettings.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        // If player runs /setting edit <name> -> open setting inspector GUI directly!
        if (args.length == 2 && sender instanceof Player) {
            String id = args[1].toLowerCase();
            Setting setting = plugin.getSettingManager().getSetting(id);
            if (setting != null) {
                plugin.getEditorManager().openInspector((Player) sender, setting);
                return;
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessage("setting-not-found")
                        .replace("%setting%", id));
                return;
            }
        }

        // Format: /setting edit <name> <property> <value...>
        if (args.length < 4) {
            sender.sendMessage(ColorUtil.colorize("&#FF5555Usage: /setting edit <setting_name> <displayname|item|slot|action|description|type|permission> <value...>"));
            return;
        }

        String id = args[1].toLowerCase();
        Setting setting = plugin.getSettingManager().getSetting(id);
        if (setting == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("setting-not-found")
                    .replace("%setting%", id));
            return;
        }

        String property = args[2].toLowerCase();

        switch (property) {
            case "displayname":
            case "name":
                String displayName = joinArgs(args, 3);
                setting.setDisplayName(displayName);
                plugin.getSettingManager().saveSettingToConfig(setting);
                sendEditSuccess(sender, id, "displayname", ColorUtil.colorize(displayName));
                break;

            case "item":
            case "material":
                String itemArg = args[3];
                if (itemArg.equalsIgnoreCase("hand")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
                        return;
                    }
                    Player p = (Player) sender;
                    ItemStack inHand = p.getInventory().getItemInMainHand();
                    if (inHand.getType() == Material.AIR) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("no-item-in-hand"));
                        return;
                    }
                    setting.setItem(inHand.getType());
                    if (inHand.hasItemMeta() && inHand.getItemMeta().hasCustomModelData()) {
                        setting.setCustomModelData(inHand.getItemMeta().getCustomModelData());
                    }
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    sendEditSuccess(sender, id, "item", inHand.getType().name());
                } else {
                    Material mat = Material.matchMaterial(itemArg.toUpperCase());
                    if (mat == null) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("invalid-material")
                                .replace("%material%", itemArg));
                        return;
                    }
                    setting.setItem(mat);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    sendEditSuccess(sender, id, "item", mat.name());
                }
                break;

            case "slot":
                try {
                    int slot = Integer.parseInt(args[3]);
                    int max = plugin.getConfigManager().getGuiSize() - 1;
                    if (slot < 0 || slot > max) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("invalid-slot")
                                .replace("%max%", String.valueOf(max)));
                        return;
                    }
                    setting.setSlot(slot);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    sendEditSuccess(sender, id, "slot", String.valueOf(slot));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&#FF5555Slot must be a valid integer number."));
                }
                break;

            case "description":
            case "desc":
            case "lore":
                String fullLoreString = joinArgs(args, 3);
                String[] loreParts = fullLoreString.split("\\|");
                List<String> loreLines = new ArrayList<>();
                for (String part : loreParts) {
                    loreLines.add(part.trim());
                }
                setting.setDescription(loreLines);
                plugin.getSettingManager().saveSettingToConfig(setting);
                sendEditSuccess(sender, id, "description", fullLoreString);
                break;

            case "action":
            case "actions":
                handleEditAction(sender, setting, args);
                break;

            case "type":
                String typeArg = args[3].toUpperCase();
                try {
                    SettingType type = SettingType.valueOf(typeArg);
                    setting.setType(type);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    sendEditSuccess(sender, id, "type", type.name());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.colorize("&#FF5555Type must be either TOGGLE or ACTION."));
                }
                break;

            case "permission":
                String perm = args[3];
                if (perm.equalsIgnoreCase("none") || perm.equalsIgnoreCase("clear")) {
                    perm = "";
                }
                setting.setPermission(perm);
                plugin.getSettingManager().saveSettingToConfig(setting);
                sendEditSuccess(sender, id, "permission", perm.isEmpty() ? "none" : perm);
                break;

            default:
                sender.sendMessage(ColorUtil.colorize("&#FF5555Unknown property '&f" + property + "&c'. Available: displayname, item, slot, action, description, type, permission"));
                break;
        }
    }

    private void handleEditAction(CommandSender sender, Setting setting, String[] args) {
        // args: [0:edit, 1:<id>, 2:action, 3:<subaction or action string>, ...]
        String subAction = args[3].toLowerCase();

        if (subAction.equals("clear")) {
            setting.getActions().clear();
            setting.getActionsOn().clear();
            setting.getActionsOff().clear();
            plugin.getSettingManager().saveSettingToConfig(setting);
            sendEditSuccess(sender, setting.getId(), "actions", "cleared all");
            return;
        }

        if (subAction.equals("add")) {
            if (args.length < 5) {
                sender.sendMessage(ColorUtil.colorize("&#FF5555Usage: /setting edit <name> action add <[player|console|message|sound|...] args>"));
                return;
            }
            String actionToAdd = joinArgs(args, 4);
            if (setting.getType() == SettingType.ACTION) {
                setting.getActions().add(actionToAdd);
            } else {
                if (actionToAdd.startsWith("[on]")) {
                    setting.getActionsOn().add(actionToAdd.substring(4).trim());
                } else if (actionToAdd.startsWith("[off]")) {
                    setting.getActionsOff().add(actionToAdd.substring(5).trim());
                } else {
                    setting.getActions().add(actionToAdd);
                }
            }
            plugin.getSettingManager().saveSettingToConfig(setting);
            sendEditSuccess(sender, setting.getId(), "action added", actionToAdd);
            return;
        }

        // Direct action assignment
        String singleAction = joinArgs(args, 3);
        if (setting.getType() == SettingType.ACTION) {
            setting.setActions(Collections.singletonList(singleAction));
        } else {
            setting.setActions(Collections.singletonList(singleAction));
        }
        plugin.getSettingManager().saveSettingToConfig(setting);
        sendEditSuccess(sender, setting.getId(), "actions", singleAction);
    }

    private void sendEditSuccess(CommandSender sender, String settingId, String property, String value) {
        sender.sendMessage(plugin.getConfigManager().getMessage("setting-edited")
                .replace("%setting%", settingId)
                .replace("%property%", property)
                .replace("%value%", value));
    }

    private String joinArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
