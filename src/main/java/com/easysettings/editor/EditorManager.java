package com.easysettings.editor;

import com.easysettings.EasySettings;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import com.easysettings.config.ConfigManager;
import com.easysettings.util.ColorUtil;
import com.easysettings.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constructs and manages modern GUI editor windows for EasySettings.
 */
public class EditorManager {

    private final EasySettings plugin;
    private final ChatInputManager chatInputManager;

    public EditorManager(EasySettings plugin) {
        this.plugin = plugin;
        this.chatInputManager = new ChatInputManager(plugin);
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    /**
     * Opens the main layout editor.
     *
     * @param player Player opening the editor
     */
    public void openMainEditor(Player player) {
        openMainEditor(player, null);
    }

    /**
     * Opens the main layout editor, optionally in moving mode for a specific setting.
     *
     * @param player        Player opening the editor
     * @param movingSetting Setting currently being moved, or null
     */
    public void openMainEditor(Player player, Setting movingSetting) {
        if (player == null) return;

        ConfigManager config = plugin.getConfigManager();
        int size = config.getGuiSize();
        String title = movingSetting == null
                ? ColorUtil.colorize("&#66FCF1&lsᴇᴛᴛɪɴɢs ᴇᴅɪᴛᴏʀ &8(" + size + " slots)")
                : ColorUtil.colorize("&#FFA94D&lᴍᴏᴠɪɴɢ: &#CED4DA" + movingSetting.getId() + " &8(Select slot)");

        EditorHolder holder = new EditorHolder(player);
        holder.setMovingSetting(movingSetting);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        // Pre-fill empty slots with Add / Move indicators
        for (int i = 0; i < size; i++) {
            if (movingSetting == null) {
                ItemStack addPane = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                        .name("&#51CF66&l+ ᴀᴅᴅ sᴇᴛᴛɪɴɢ")
                        .lore(Arrays.asList(
                                "&#868E96sʟᴏᴛ: &#CED4DA#" + i,
                                "",
                                "&#51CF66▸ Click to configure setting here"
                        ))
                        .hideAllAttributes()
                        .build();
                inv.setItem(i, addPane);
            } else {
                ItemStack movePane = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
                        .name("&#FFA94D&lᴍᴏᴠᴇ sᴇᴛᴛɪɴɢ ʜᴇʀᴇ")
                        .lore(Arrays.asList(
                                "&#868E96Move &#CED4DA" + movingSetting.getId() + " &#868E96to slot &#CED4DA#" + i,
                                "",
                                "&#FFA94D▸ Click to place here"
                        ))
                        .hideAllAttributes()
                        .build();
                inv.setItem(i, movePane);
            }
        }

        // Place existing settings
        for (Setting s : plugin.getSettingManager().getAllSettings()) {
            int slot = s.getSlot();
            if (slot < 0 || slot >= size) continue;

            holder.registerSetting(slot, s);

            if (movingSetting != null && s.getId().equalsIgnoreCase(movingSetting.getId())) {
                ItemStack movingItem = new ItemBuilder(Material.BARRIER)
                        .name("&#FFA94D&l[ᴄᴜʀʀᴇɴᴛʟʏ ᴍᴏᴠɪɴɢ]")
                        .lore(Arrays.asList(
                                "&#868E96ɪᴅ: &#CED4DA" + s.getId(),
                                "",
                                "&#FFA94DClick another slot to move it,",
                                "&#FF6B6Bor click this slot to cancel."
                        ))
                        .glowing(true)
                        .build();
                inv.setItem(slot, movingItem);
            } else {
                int actionCount = (s.getType() == SettingType.ACTION)
                        ? s.getActions().size()
                        : (s.getActionsOn().size() + s.getActionsOff().size());

                ItemStack item = new ItemBuilder(s.getItem())
                        .name("&#66FCF1&l" + ColorUtil.stripColor(s.getDisplayName()))
                        .lore(Arrays.asList(
                                "&#495057&m─────────────────────────",
                                "&#868E96ɪᴅ: &#CED4DA" + s.getId(),
                                "&#868E96sʟᴏᴛ: &#CED4DA#" + s.getSlot(),
                                "&#868E96ᴛʏᴘᴇ: &#CED4DA" + ColorUtil.toSmallCaps(s.getType().name()),
                                "&#868E96ᴀᴄᴛɪᴏɴs: &#CED4DA" + actionCount,
                                "&#868E96ᴘᴇʀᴍ: &#CED4DA" + (s.getPermission().isEmpty() ? "&#51CF66None" : s.getPermission()),
                                "&#495057&m─────────────────────────",
                                "&#66FCF1▸ Left-Click &#868E96to inspect & edit",
                                "&#FFA94D▸ Shift-Click &#868E96to move slot",
                                "&#FF6B6B▸ Right-Click &#868E96to delete"
                        ))
                        .customModelData(s.getCustomModelData())
                        .glowing(s.isGlowing())
                        .hideAllAttributes()
                        .build();

                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Opens the detailed inspector menu for a single setting.
     *
     * @param player  Player
     * @param setting Setting being inspected
     */
    public void openInspector(Player player, Setting setting) {
        if (player == null || setting == null) return;

        InspectorHolder holder = new InspectorHolder(player, setting);
        Inventory inv = Bukkit.createInventory(holder, 27, ColorUtil.colorize("&#66FCF1&lᴇᴅɪᴛ &#868E96│ &#CED4DA" + setting.getId()));
        holder.setInventory(inv);

        // Fill background with gray pane
        ItemStack bg = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, bg);
        }

        // Preview item at top (Slot 4)
        ItemStack preview = plugin.getGuiManager().createSettingItem(player, setting);
        inv.setItem(4, preview);

        // Slot 10: Display Name
        ItemStack nameItem = new ItemBuilder(Material.NAME_TAG)
                .name("&#66FCF1&lᴅɪsᴘʟᴀʏ ɴᴀᴍᴇ")
                .lore(Arrays.asList(
                        "&#868E96Current: &#CED4DA" + setting.getDisplayName(),
                        "",
                        "&#66FCF1▸ Click to type new name in chat",
                        "&#868E96Supports color codes & hex!"
                ))
                .build();
        inv.setItem(10, nameItem);

        // Slot 11: Icon Material
        ItemStack iconItem = new ItemBuilder(setting.getItem())
                .name("&#66FCF1&lɪᴛᴇᴍ ɪᴄᴏɴ")
                .lore(Arrays.asList(
                        "&#868E96Material: &#CED4DA" + setting.getItem().name(),
                        "",
                        "&#66FCF1▸ Click with cursor or main hand",
                        "&#868E96or click to type material name"
                ))
                .build();
        inv.setItem(11, iconItem);

        // Slot 12: Type (TOGGLE / ACTION)
        Material typeMat = (setting.getType() == SettingType.TOGGLE) ? Material.REPEATER : Material.REDSTONE_TORCH;
        ItemStack typeItem = new ItemBuilder(typeMat)
                .name("&#66FCF1&lsᴇᴛᴛɪɴɢ ᴛʏᴘᴇ")
                .lore(Arrays.asList(
                        "&#868E96Type: &#CED4DA" + ColorUtil.toSmallCaps(setting.getType().name()),
                        "",
                        "&#66FCF1▸ Click to switch TOGGLE / ACTION"
                ))
                .build();
        inv.setItem(12, typeItem);

        // Slot 13: Glowing
        ItemStack glowItem = new ItemBuilder(setting.isGlowing() ? Material.NETHER_STAR : Material.GLOWSTONE_DUST)
                .name("&#66FCF1&lᴇɴᴄʜᴀɴᴛᴍᴇɴᴛ ɢʟɪɴᴛ")
                .lore(Arrays.asList(
                        "&#868E96Status: " + (setting.isGlowing() ? "&#51CF66● ᴇɴᴀʙʟᴇᴅ" : "&#FF6B6B○ ᴅɪsᴀʙʟᴇᴅ"),
                        "",
                        "&#66FCF1▸ Click to toggle glow effect"
                ))
                .build();
        inv.setItem(13, glowItem);

        // Slot 14: Actions
        int actionCount = (setting.getType() == SettingType.ACTION)
                ? setting.getActions().size()
                : (setting.getActionsOn().size() + setting.getActionsOff().size());
        ItemStack actionItem = new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&#66FCF1&lᴀᴄᴛɪᴏɴs & ᴄᴏᴍᴍᴀɴᴅs")
                .lore(Arrays.asList(
                        "&#868E96Configured: &#CED4DA" + actionCount,
                        "",
                        "&#66FCF1▸ Click to view and edit actions"
                ))
                .build();
        inv.setItem(14, actionItem);

        // Slot 15: Description (Lore)
        ItemStack loreItem = new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&#66FCF1&lᴅᴇsᴄʀɪᴘᴛɪᴏɴ & ʟᴏʀᴇ")
                .lore(Arrays.asList(
                        "&#868E96Lines: &#CED4DA" + setting.getDescription().size(),
                        "",
                        "&#66FCF1▸ Click to view and edit lore"
                ))
                .build();
        inv.setItem(15, loreItem);

        // Slot 16: Permission
        ItemStack permItem = new ItemBuilder(Material.TRIPWIRE_HOOK)
                .name("&#66FCF1&lᴘᴇʀᴍɪssɪᴏɴ ɴᴏᴅᴇ")
                .lore(Arrays.asList(
                        "&#868E96Node: &#CED4DA" + (setting.getPermission().isEmpty() ? "&#51CF66None (Public)" : setting.getPermission()),
                        "",
                        "&#66FCF1▸ Click to change permission"
                ))
                .build();
        inv.setItem(16, permItem);

        // Slot 18: Back Button
        ItemStack back = new ItemBuilder(Material.ARROW)
                .name("&#FFA94D&l◀ ʙᴀᴄᴋ ᴛᴏ ᴇᴅɪᴛᴏʀ")
                .lore(Collections.singletonList("&#868E96Return to the main layout editor."))
                .build();
        inv.setItem(18, back);

        // Slot 22: Slot Position
        ItemStack slotItem = new ItemBuilder(Material.COMPASS)
                .name("&#66FCF1&lsʟᴏᴛ ᴘᴏsɪᴛɪᴏɴ")
                .lore(Arrays.asList(
                        "&#868E96Current: &#CED4DA#" + setting.getSlot(),
                        "",
                        "&#66FCF1▸ Click to enter new slot in chat"
                ))
                .build();
        inv.setItem(22, slotItem);

        // Slot 26: Delete
        ItemStack deleteItem = new ItemBuilder(Material.TNT)
                .name("&#FF6B6B&l✖ ᴅᴇʟᴇᴛᴇ sᴇᴛᴛɪɴɢ")
                .lore(Arrays.asList(
                        "&#868E96Permanently remove &#FF8787" + setting.getId() + "&#868E96.",
                        "",
                        "&#FF6B6B▸ Shift-Click to confirm deletion!"
                ))
                .build();
        inv.setItem(26, deleteItem);

        player.openInventory(inv);
    }

    /**
     * Opens the actions editor menu.
     *
     * @param player  Player
     * @param setting Setting
     */
    public void openActionEditor(Player player, Setting setting) {
        if (player == null || setting == null) return;

        ActionEditorHolder holder = new ActionEditorHolder(player, setting);
        Inventory inv = Bukkit.createInventory(holder, 36, ColorUtil.colorize("&#66FCF1&lᴀᴄᴛɪᴏɴs: &#CED4DA" + setting.getId()));
        holder.setInventory(inv);

        ItemStack bg = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, bg);
        }

        // List existing actions
        List<String> allActions = new ArrayList<>();
        if (setting.getType() == SettingType.ACTION) {
            allActions.addAll(setting.getActions());
        } else {
            for (String a : setting.getActionsOn()) allActions.add("[ON] " + a);
            for (String a : setting.getActionsOff()) allActions.add("[OFF] " + a);
            for (String a : setting.getActions()) allActions.add(a);
        }

        int slotIdx = 0;
        for (String act : allActions) {
            if (slotIdx >= 18) break;
            ItemStack actItem = new ItemBuilder(Material.PAPER)
                    .name("&#66FCF1&lᴀᴄᴛɪᴏɴ #" + (slotIdx + 1))
                    .lore(Arrays.asList(
                            "&#CED4DA" + act,
                            "",
                            "&#FF6B6B▸ Click to remove this action"
                    ))
                    .build();
            inv.setItem(slotIdx++, actItem);
        }

        // Controls
        ItemStack addPlayerCmd = new ItemBuilder(Material.EMERALD)
                .name("&#51CF66&l+ ᴘʟᴀʏᴇʀ ᴄᴏᴍᴍᴀɴᴅ")
                .lore(Arrays.asList("&#868E96Runs command as the player.", "&#66FCF1▸ Click to type command in chat."))
                .build();
        inv.setItem(20, addPlayerCmd);

        ItemStack addConsoleCmd = new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&#51CF66&l+ ᴄᴏɴsᴏʟᴇ ᴄᴏᴍᴍᴀɴᴅ")
                .lore(Arrays.asList("&#868E96Runs from console (%player% supported).", "&#66FCF1▸ Click to type command in chat."))
                .build();
        inv.setItem(21, addConsoleCmd);

        ItemStack addMessage = new ItemBuilder(Material.FEATHER)
                .name("&#51CF66&l+ ᴍᴇssᴀɢᴇ")
                .lore(Arrays.asList("&#868E96Sends colored message to player.", "&#66FCF1▸ Click to type message in chat."))
                .build();
        inv.setItem(22, addMessage);

        ItemStack addSound = new ItemBuilder(Material.NOTE_BLOCK)
                .name("&#51CF66&l+ sᴏᴜɴᴅ ᴇғғᴇᴄᴛ")
                .lore(Arrays.asList("&#868E96Plays a sound effect.", "&#66FCF1▸ Click to type sound name in chat."))
                .build();
        inv.setItem(23, addSound);

        ItemStack clearAll = new ItemBuilder(Material.BARRIER)
                .name("&#FF6B6B&l✕ ᴄʟᴇᴀʀ ᴀʟʟ")
                .lore(Arrays.asList("&#868E96Removes all configured actions.", "&#FF6B6B▸ Click to clear."))
                .build();
        inv.setItem(24, clearAll);

        ItemStack back = new ItemBuilder(Material.ARROW)
                .name("&#FFA94D&l◀ ʙᴀᴄᴋ ᴛᴏ sᴇᴛᴛɪɴɢ")
                .lore(Collections.singletonList("&#868E96Return to setting inspector."))
                .build();
        inv.setItem(31, back);

        player.openInventory(inv);
    }

    /**
     * Opens the lore / description editor menu.
     *
     * @param player  Player
     * @param setting Setting
     */
    public void openLoreEditor(Player player, Setting setting) {
        if (player == null || setting == null) return;

        LoreEditorHolder holder = new LoreEditorHolder(player, setting);
        Inventory inv = Bukkit.createInventory(holder, 36, ColorUtil.colorize("&#66FCF1&lʟᴏʀᴇ: &#CED4DA" + setting.getId()));
        holder.setInventory(inv);

        ItemStack bg = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, bg);
        }

        List<String> desc = setting.getDescription();
        int slotIdx = 0;
        for (String line : desc) {
            if (slotIdx >= 18) break;
            ItemStack lineItem = new ItemBuilder(Material.PAPER)
                    .name("&#66FCF1&lʟɪɴᴇ #" + (slotIdx + 1))
                    .lore(Arrays.asList(
                            line,
                            "",
                            "&#FF6B6B▸ Click to remove this line"
                    ))
                    .build();
            inv.setItem(slotIdx++, lineItem);
        }

        ItemStack addLine = new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&#51CF66&l+ ᴀᴅᴅ ʟᴏʀᴇ ʟɪɴᴇ")
                .lore(Arrays.asList("&#868E96Add a single line to the end.", "&#66FCF1▸ Click to type in chat."))
                .build();
        inv.setItem(20, addLine);

        ItemStack setEntireLore = new ItemBuilder(Material.BOOK)
                .name("&#66FCF1&l✎ sᴇᴛ ᴇɴᴛɪʀᴇ ʟᴏʀᴇ")
                .lore(Arrays.asList("&#868E96Set all lines at once using | separator.", "&#66FCF1▸ Click to type in chat."))
                .build();
        inv.setItem(22, setEntireLore);

        ItemStack clearLore = new ItemBuilder(Material.BARRIER)
                .name("&#FF6B6B&l✕ ᴄʟᴇᴀʀ ᴀʟʟ ʟᴏʀᴇ")
                .lore(Arrays.asList("&#868E96Remove all description lines.", "&#FF6B6B▸ Click to clear."))
                .build();
        inv.setItem(24, clearLore);

        ItemStack back = new ItemBuilder(Material.ARROW)
                .name("&#FFA94D&l◀ ʙᴀᴄᴋ ᴛᴏ sᴇᴛᴛɪɴɢ")
                .lore(Collections.singletonList("&#868E96Return to setting inspector."))
                .build();
        inv.setItem(31, back);

        player.openInventory(inv);
    }
}
