package com.easysettings.editor;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import com.easysettings.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles all click interactions inside EasySettings editor GUIs.
 */
public class EditorListener implements Listener {

    private final EasySettings plugin;

    public EditorListener(EasySettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getHolder() instanceof EditorHolder) {
            handleMainEditorClick(event, player, (EditorHolder) event.getInventory().getHolder());
        } else if (event.getInventory().getHolder() instanceof InspectorHolder) {
            handleInspectorClick(event, player, (InspectorHolder) event.getInventory().getHolder());
        } else if (event.getInventory().getHolder() instanceof ActionEditorHolder) {
            handleActionEditorClick(event, player, (ActionEditorHolder) event.getInventory().getHolder());
        } else if (event.getInventory().getHolder() instanceof LoreEditorHolder) {
            handleLoreEditorClick(event, player, (LoreEditorHolder) event.getInventory().getHolder());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof EditorHolder
                || event.getInventory().getHolder() instanceof InspectorHolder
                || event.getInventory().getHolder() instanceof ActionEditorHolder
                || event.getInventory().getHolder() instanceof LoreEditorHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMainEditorClick(InventoryClickEvent event, Player player, EditorHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        Setting moving = holder.getMovingSetting();

        // If in moving mode
        if (moving != null) {
            if (slot == moving.getSlot()) {
                // Cancelled move
                player.sendMessage(ColorUtil.colorize("&#FFA94D✖ &#CED4DAᴍᴏᴠɪɴɢ ᴄᴀɴᴄᴇʟʟᴇᴅ."));
                plugin.getEditorManager().openMainEditor(player, null);
                return;
            }

            // Move setting to new slot
            Setting targetExisting = holder.getSettingAt(slot);
            if (targetExisting != null) {
                // Swap slots
                targetExisting.setSlot(moving.getSlot());
                plugin.getSettingManager().saveSettingToConfig(targetExisting);
            }
            moving.setSlot(slot);
            plugin.getSettingManager().saveSettingToConfig(moving);

            player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴍᴏᴠᴇᴅ &#66FCF1" + moving.getId() + " &#CED4DAᴛᴏ sʟᴏᴛ &#66FCF1#" + slot + "!"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            plugin.getEditorManager().openMainEditor(player, null);
            return;
        }

        // Regular mode
        Setting existing = holder.getSettingAt(slot);
        if (existing != null) {
            if (event.isRightClick()) {
                // Quick delete
                plugin.getSettingManager().deleteSetting(existing.getId());
                player.sendMessage(ColorUtil.colorize("&#FF6B6B✔ &#CED4DAsᴇᴛᴛɪɴɢ &#FF8787" + existing.getId() + " &#CED4DAᴅᴇʟᴇᴛᴇᴅ."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                plugin.getEditorManager().openMainEditor(player, null);
            } else if (event.isShiftClick()) {
                // Shift click: enter moving mode
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                plugin.getEditorManager().openMainEditor(player, existing);
            } else {
                // Left click: open inspector
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                plugin.getEditorManager().openInspector(player, existing);
            }
        } else {
            // Clicked empty slot: create new setting here
            final int targetSlot = slot;
            plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter an ID for the new setting at slot #" + targetSlot + ":", idInput -> {
                String cleanId = idInput.toLowerCase().replace(" ", "_");
                if (plugin.getSettingManager().hasSetting(cleanId)) {
                    player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAᴀ sᴇᴛᴛɪɴɢ ᴡɪᴛʜ ɪᴅ &#FF8787'" + cleanId + "' &#CED4DAᴀʟʀᴇᴀᴅʏ ᴇxɪsᴛs!"));
                    plugin.getEditorManager().openMainEditor(player);
                    return;
                }

                Setting newSetting = new Setting(cleanId);
                newSetting.setDisplayName("&#66FCF1&l" + ColorUtil.toSmallCaps(cleanId));
                newSetting.setSlot(targetSlot);
                newSetting.setItem(Material.STONE);
                newSetting.setType(SettingType.TOGGLE);
                newSetting.setDescription(Arrays.asList(
                        "&#868E96Description for " + cleanId + ".",
                        "",
                        "&#495057│ &#868E96Status: %status%",
                        "",
                        "&#66FCF1▸ Click to toggle"
                ));
                newSetting.setActionsOn(Collections.singletonList("[message] &#66FCF1" + cleanId + " &#51CF66● ᴇɴᴀʙʟᴇᴅ"));
                newSetting.setActionsOff(Collections.singletonList("[message] &#66FCF1" + cleanId + " &#FF6B6B○ ᴅɪsᴀʙʟᴇᴅ"));

                plugin.getSettingManager().addSetting(newSetting);
                player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴄʀᴇᴀᴛᴇᴅ sᴇᴛᴛɪɴɢ &#66FCF1" + cleanId + " &#CED4DAᴀᴛ sʟᴏᴛ &#66FCF1#" + targetSlot + "!"));
                plugin.getEditorManager().openInspector(player, newSetting);
            });
        }
    }

    private void handleInspectorClick(InventoryClickEvent event, Player player, InspectorHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        Setting setting = holder.getSetting();

        switch (slot) {
            case 10: // Display Name
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter new display name for '" + setting.getId() + "' (supports color & hex):", input -> {
                    setting.setDisplayName(input);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴜᴘᴅᴀᴛᴇᴅ ᴅɪsᴘʟᴀʏ ɴᴀᴍᴇ: &r" + input));
                    plugin.getEditorManager().openInspector(player, setting);
                });
                break;

            case 11: // Icon Material
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    setting.setItem(cursor.getType());
                    if (cursor.hasItemMeta() && cursor.getItemMeta().hasCustomModelData()) {
                        setting.setCustomModelData(cursor.getItemMeta().getCustomModelData());
                    }
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴜᴘᴅᴀᴛᴇᴅ ɪᴄᴏɴ ᴛᴏ: &#66FCF1" + cursor.getType().name()));
                    plugin.getEditorManager().openInspector(player, setting);
                    return;
                }

                ItemStack inHand = player.getInventory().getItemInMainHand();
                if (inHand.getType() != Material.AIR && inHand.getType() != setting.getItem()) {
                    setting.setItem(inHand.getType());
                    if (inHand.hasItemMeta() && inHand.getItemMeta().hasCustomModelData()) {
                        setting.setCustomModelData(inHand.getItemMeta().getCustomModelData());
                    }
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴜᴘᴅᴀᴛᴇᴅ ɪᴄᴏɴ ғʀᴏᴍ ʜᴀɴᴅ: &#66FCF1" + inHand.getType().name()));
                    plugin.getEditorManager().openInspector(player, setting);
                    return;
                }

                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter material name for icon (e.g. DIAMOND_SWORD):", input -> {
                    Material mat = Material.matchMaterial(input.toUpperCase());
                    if (mat == null) {
                        player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAᴜɴᴋɴᴏᴡɴ ᴍᴀᴛᴇʀɪᴀʟ: &#FF8787" + input));
                    } else {
                        setting.setItem(mat);
                        plugin.getSettingManager().saveSettingToConfig(setting);
                        player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴜᴘᴅᴀᴛᴇᴅ ɪᴄᴏɴ ᴛᴏ: &#66FCF1" + mat.name()));
                    }
                    plugin.getEditorManager().openInspector(player, setting);
                });
                break;

            case 12: // Type (TOGGLE / ACTION)
                SettingType newType = (setting.getType() == SettingType.TOGGLE) ? SettingType.ACTION : SettingType.TOGGLE;
                setting.setType(newType);
                plugin.getSettingManager().saveSettingToConfig(setting);
                player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAsᴡɪᴛᴄʜᴇᴅ ᴛʏᴘᴇ ᴛᴏ: &#66FCF1" + ColorUtil.toSmallCaps(newType.name())));
                plugin.getEditorManager().openInspector(player, setting);
                break;

            case 13: // Glowing
                setting.setGlowing(!setting.isGlowing());
                plugin.getSettingManager().saveSettingToConfig(setting);
                player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAɢʟᴏᴡ ᴇғғᴇᴄᴛ: " + (setting.isGlowing() ? "&#51CF66● ᴇɴᴀʙʟᴇᴅ" : "&#FF6B6B○ ᴅɪsᴀʙʟᴇᴅ")));
                plugin.getEditorManager().openInspector(player, setting);
                break;

            case 14: // Actions
                plugin.getEditorManager().openActionEditor(player, setting);
                break;

            case 15: // Description (Lore)
                plugin.getEditorManager().openLoreEditor(player, setting);
                break;

            case 16: // Permission
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter permission required (or type 'none' to remove):", input -> {
                    if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("clear")) {
                        setting.setPermission("");
                        player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴘᴇʀᴍɪssɪᴏɴ ʀᴇᴍᴏᴠᴇᴅ."));
                    } else {
                        setting.setPermission(input);
                        player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴘᴇʀᴍɪssɪᴏɴ sᴇᴛ ᴛᴏ: &#66FCF1" + input));
                    }
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    plugin.getEditorManager().openInspector(player, setting);
                });
                break;

            case 18: // Back Button
                plugin.getEditorManager().openMainEditor(player);
                break;

            case 22: // Slot Position
                int max = plugin.getConfigManager().getGuiSize() - 1;
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter new slot index (0 - " + max + "):", input -> {
                    try {
                        int newSlot = Integer.parseInt(input);
                        if (newSlot < 0 || newSlot > max) {
                            player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAsʟᴏᴛ ᴍᴜsᴛ ʙᴇ ʙᴇᴛᴡᴇᴇɴ 0 ᴀɴᴅ " + max + "."));
                        } else {
                            setting.setSlot(newSlot);
                            plugin.getSettingManager().saveSettingToConfig(setting);
                            player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAsʟᴏᴛ ᴄʜᴀɴɢᴇᴅ ᴛᴏ: &#66FCF1#" + newSlot));
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAɪɴᴠᴀʟɪᴅ ɴᴜᴍʙᴇʀ."));
                    }
                    plugin.getEditorManager().openInspector(player, setting);
                });
                break;

            case 26: // Delete
                if (event.isShiftClick()) {
                    plugin.getSettingManager().deleteSetting(setting.getId());
                    player.sendMessage(ColorUtil.colorize("&#FF6B6B✔ &#CED4DAᴘᴇʀᴍᴀɴᴇɴᴛʟʏ ᴅᴇʟᴇᴛᴇᴅ &#FF8787" + setting.getId() + "&#CED4DA!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    plugin.getEditorManager().openMainEditor(player);
                } else {
                    player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAᴘʟᴇᴀsᴇ &#FF8787&lsʜɪғᴛ-ᴄʟɪᴄᴋ &#CED4DAᴛᴏ ᴄᴏɴғɪʀᴍ ᴅᴇʟᴇᴛɪᴏɴ."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
                break;
        }
    }

    private void handleActionEditorClick(InventoryClickEvent event, Player player, ActionEditorHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        Setting setting = holder.getSetting();

        // Click existing action paper (0 to 17) to delete it
        if (slot < 18) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PAPER) {
                if (setting.getType() == SettingType.ACTION) {
                    if (slot < setting.getActions().size()) {
                        setting.getActions().remove(slot);
                    }
                } else {
                    if (slot < setting.getActionsOn().size()) {
                        setting.getActionsOn().remove(slot);
                    } else {
                        int offIdx = slot - setting.getActionsOn().size();
                        if (offIdx < setting.getActionsOff().size()) {
                            setting.getActionsOff().remove(offIdx);
                        } else {
                            int genIdx = offIdx - setting.getActionsOff().size();
                            if (genIdx < setting.getActions().size()) {
                                setting.getActions().remove(genIdx);
                            }
                        }
                    }
                }
                plugin.getSettingManager().saveSettingToConfig(setting);
                player.sendMessage(ColorUtil.colorize("&#FF6B6B✔ &#CED4DAʀᴇᴍᴏᴠᴇᴅ ᴀᴄᴛɪᴏɴ!"));
                plugin.getEditorManager().openActionEditor(player, setting);
                return;
            }
        }

        switch (slot) {
            case 20: // + Add Player Command
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter player command (e.g. fly, warp spawn):", input -> {
                    addActionToSetting(setting, "[player] " + input);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴀᴅᴅᴇᴅ ᴘʟᴀʏᴇʀ ᴄᴏᴍᴍᴀɴᴅ: &#66FCF1[player] " + input));
                    plugin.getEditorManager().openActionEditor(player, setting);
                });
                break;

            case 21: // + Add Console Command
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter console command (%player% supported):", input -> {
                    addActionToSetting(setting, "[console] " + input);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴀᴅᴅᴇᴅ ᴄᴏɴsᴏʟᴇ ᴄᴏᴍᴍᴀɴᴅ: &#66FCF1[console] " + input));
                    plugin.getEditorManager().openActionEditor(player, setting);
                });
                break;

            case 22: // + Add Message
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter message to send player (supports hex & color):", input -> {
                    addActionToSetting(setting, "[message] " + input);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴀᴅᴅᴇᴅ ᴍᴇssᴀɢᴇ: &r" + input));
                    plugin.getEditorManager().openActionEditor(player, setting);
                });
                break;

            case 23: // + Add Sound
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter sound name (e.g. ENTITY_EXPERIENCE_ORB_PICKUP:1.0:1.0):", input -> {
                    addActionToSetting(setting, "[sound] " + input);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴀᴅᴅᴇᴅ sᴏᴜɴᴅ: &#66FCF1[sound] " + input));
                    plugin.getEditorManager().openActionEditor(player, setting);
                });
                break;

            case 24: // Clear All
                setting.getActions().clear();
                setting.getActionsOn().clear();
                setting.getActionsOff().clear();
                plugin.getSettingManager().saveSettingToConfig(setting);
                player.sendMessage(ColorUtil.colorize("&#FF6B6B✔ &#CED4DAᴄʟᴇᴀʀᴇᴅ ᴀʟʟ ᴀᴄᴛɪᴏɴs."));
                plugin.getEditorManager().openActionEditor(player, setting);
                break;

            case 31: // Back
                plugin.getEditorManager().openInspector(player, setting);
                break;
        }
    }

    private void addActionToSetting(Setting setting, String action) {
        if (setting.getType() == SettingType.ACTION) {
            setting.getActions().add(action);
        } else {
            setting.getActionsOn().add(action);
        }
        plugin.getSettingManager().saveSettingToConfig(setting);
    }

    private void handleLoreEditorClick(InventoryClickEvent event, Player player, LoreEditorHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        Setting setting = holder.getSetting();

        // Click existing line (0 to 17) to delete
        if (slot < 18) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PAPER) {
                if (slot < setting.getDescription().size()) {
                    setting.getDescription().remove(slot);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    player.sendMessage(ColorUtil.colorize("&#FF6B6B✔ &#CED4DAʀᴇᴍᴏᴠᴇᴅ ʟᴏʀᴇ ʟɪɴᴇ!"));
                    plugin.getEditorManager().openLoreEditor(player, setting);
                }
                return;
            }
        }

        switch (slot) {
            case 20: // Add single line
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter single lore line to add:", input -> {
                    setting.getDescription().add(input);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴀᴅᴅᴇᴅ ʟᴏʀᴇ ʟɪɴᴇ: &r" + input));
                    plugin.getEditorManager().openLoreEditor(player, setting);
                });
                break;

            case 22: // Set entire lore using |
                plugin.getEditorManager().getChatInputManager().awaitInput(player, "Enter full lore separated by | (e.g. Line 1 | &7Status: %status% | &eClick to toggle):", input -> {
                    String[] parts = input.split("\\|");
                    List<String> lines = new ArrayList<>();
                    for (String p : parts) lines.add(p.trim());
                    setting.setDescription(lines);
                    plugin.getSettingManager().saveSettingToConfig(setting);
                    player.sendMessage(ColorUtil.colorize("&#51CF66✔ &#CED4DAᴜᴘᴅᴀᴛᴇᴅ ᴇɴᴛɪʀᴇ ʟᴏʀᴇ!"));
                    plugin.getEditorManager().openLoreEditor(player, setting);
                });
                break;

            case 24: // Clear All Lore
                setting.getDescription().clear();
                plugin.getSettingManager().saveSettingToConfig(setting);
                player.sendMessage(ColorUtil.colorize("&#FF6B6B✔ &#CED4DAᴄʟᴇᴀʀᴇᴅ ᴀʟʟ ʟᴏʀᴇ ʟɪɴᴇs."));
                plugin.getEditorManager().openLoreEditor(player, setting);
                break;

            case 31: // Back
                plugin.getEditorManager().openInspector(player, setting);
                break;
        }
    }
}
