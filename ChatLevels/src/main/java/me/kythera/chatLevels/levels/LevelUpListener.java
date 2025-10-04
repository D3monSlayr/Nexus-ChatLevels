package me.kythera.chatLevels.levels;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.kythera.chatLevels.ChatLevels;
import me.kythera.chatLevels.configs.DataSaving;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelUpListener implements Listener {

    // Cooldown map: UUID -> last chat timestamp
    private final Map<UUID, Long> chatCooldown = new HashMap<>();

    // Cooldown time in ms (e.g. 5 seconds)
    private static final long COOLDOWN_MS = ChatLevels.getInstance().getConfig().getInt("cooldown");

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (chatCooldown.containsKey(uuid) && (now - chatCooldown.get(uuid)) < COOLDOWN_MS) {
            return;
        }

        // Update last chat time
        chatCooldown.put(uuid, now);

        // Run XP logic on the main thread (since event is async)
        Bukkit.getScheduler().runTask(ChatLevels.getInstance(), () -> {
            LevelUpManager.addXP(uuid, ChatLevels.getInstance().getConfig().getDouble("multiplier"));
            LevelUpManager.computeLevel(uuid);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(DataSaving.getConfiguration().getConfigurationSection(player.getUniqueId().toString()) == null) {
            ConfigurationSection section = DataSaving.getConfiguration().createSection(player.getUniqueId().toString());
            section.set("level", 0);
            section.set("xp", 0.0);
            DataSaving.saveConfig();
        }

        if(!player.hasPlayedBefore()) {
            int defaultXP = ChatLevels.getInstance().getConfig().getInt("default-xp");
            LevelUpManager.addXP(player.getUniqueId(), defaultXP);

        }

    }
}
