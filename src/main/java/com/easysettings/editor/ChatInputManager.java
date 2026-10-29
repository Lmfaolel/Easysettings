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
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages awaiting chat input with automatic timeout to prevent memory leaks.
 */
public class ChatInputManager implements Listener {

    private final EasySettings plugin;

    private static class PromptSession {
        final Consumer<String> callback;
        final BukkitTask timeoutTask;

        PromptSession(Consumer<String> callback, BukkitTask timeoutTask) {
            this.callback = callback;
            this.timeoutTask = timeoutTask;
        }
    }

    private final Map<UUID, PromptSession> pendingSessions = new ConcurrentHashMap<>();

    public ChatInputManager(EasySettings plugin) {
        this.plugin = plugin;
    }

    /**
     * Awaits text input from the player in chat with automatic 60-second expiration.
     *
     * @param player      Player to await input from
     * @param promptTitle Description shown to the player
     * @param callback    Callback called on the main thread with the input
     */
    public void awaitInput(Player player, String promptTitle, Consumer<String> callback) {
        if (player == null || callback == null) return;

        cancelInput(player);
        player.closeInventory();

        UUID uuid = player.getUniqueId();

        // Auto-expire after 60 seconds (1200 ticks) to prevent memory leaks from abandoned prompts
        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PromptSession session = pendingSessions.remove(uuid);
            if (session != null && player.isOnline()) {
                player.sendMessage(ColorUtil.colorize("&#FFA94D✖ &#CED4DAᴇᴅɪᴛᴏʀ ɪɴᴘᴜᴛ ᴛɪᴍᴇᴅ ᴏᴜᴛ."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                plugin.getEditorManager().openMainEditor(player);
            }
        }, 1200L);

        pendingSessions.put(uuid, new PromptSession(callback, timeoutTask));

        player.sendMessage(ColorUtil.colorize("&#495057&m──────────────────────────────────────────"));
        player.sendMessage(ColorUtil.colorize("&#66FCF1&lsᴇᴛᴛɪɴɢs ᴇᴅɪᴛᴏʀ &#495057» &#CED4DA" + promptTitle));
        player.sendMessage(ColorUtil.colorize("&#868E96Type in chat, or type &#FF6B6B&lcancel &#868E96to abort."));
        player.sendMessage(ColorUtil.colorize("&#495057&m──────────────────────────────────────────"));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
    }

    public boolean isAwaitingInput(Player player) {
        return player != null && pendingSessions.containsKey(player.getUniqueId());
    }

    public void cancelInput(Player player) {
        if (player != null) {
            PromptSession session = pendingSessions.remove(player.getUniqueId());
            if (session != null && session.timeoutTask != null) {
                session.timeoutTask.cancel();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PromptSession session = pendingSessions.remove(player.getUniqueId());

        if (session != null) {
            event.setCancelled(true);
            if (session.timeoutTask != null) {
                session.timeoutTask.cancel();
            }

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
                    session.callback.accept(message);
                } catch (Exception e) {
                    player.sendMessage(ColorUtil.colorize("&#FF6B6B✖ &#CED4DAError processing input: " + e.getMessage()));
                    plugin.getLogger().warning("Error processing chat editor input: " + e.getMessage());
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelInput(event.getPlayer());
    }

    public void shutdown() {
        for (PromptSession session : pendingSessions.values()) {
            if (session.timeoutTask != null) {
                session.timeoutTask.cancel();
            }
        }
        pendingSessions.clear();
    }
}
