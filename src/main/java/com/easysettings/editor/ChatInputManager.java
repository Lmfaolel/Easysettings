package com.easysettings.editor;

import com.easysettings.EasySettings;
import com.easysettings.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages awaiting chat input from players (e.g. when typing display names, lore, actions).
 */
public class ChatInputManager implements Listener {

    private final EasySettings plugin;
    private final Map<UUID, Consumer<String>> pendingPrompts = new ConcurrentHashMap<>();

    public ChatInputManager(EasySettings plugin) {
        this.plugin = plugin;
    }

    /**
     * Awaits text input from the player in chat.
     *
     * @param player      Player to await input from
     * @param promptTitle Description shown to the player
     * @param callback    Callback called on the main thread with the input
     */
    public void awaitInput(Player player, String promptTitle, Consumer<String> callback) {
        if (player == null || callback == null) return;

        player.closeInventory();
        pendingPrompts.put(player.getUniqueId(), callback);

        player.sendMessage(ColorUtil.colorize("&#495057&m──────────────────────────────────────────"));
        player.sendMessage(ColorUtil.colorize("&#66FCF1&lsᴇᴛᴛɪɴɢs ᴇᴅɪᴛᴏʀ &#495057» &#CED4DA" + promptTitle));
        player.sendMessage(ColorUtil.colorize("&#868E96Type in chat, or type &#FF6B6B&lcancel &#868E96to abort."));
        player.sendMessage(ColorUtil.colorize("&#495057&m──────────────────────────────────────────"));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
    }

    public boolean isAwaitingInput(Player player) {
        return player != null && pendingPrompts.containsKey(player.getUniqueId());
    }

    public void cancelInput(Player player) {
        if (player != null) {
            pendingPrompts.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = pendingPrompts.remove(player.getUniqueId());

        if (callback != null) {
            event.setCancelled(true);
            String message = event.getMessage().trim();

            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAᴇᴅɪᴛᴏʀ ᴀᴄᴛɪᴏɴ ᴄᴀɴᴄᴇʟʟᴇᴅ."));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    plugin.getEditorManager().openMainEditor(player);
                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                try {
                    callback.accept(message);
                } catch (Exception e) {
                    player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAError processing input: " + e.getMessage()));
                    plugin.getLogger().warning("Error processing chat editor input: " + e.getMessage());
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingPrompts.remove(event.getPlayer().getUniqueId());
    }
}
