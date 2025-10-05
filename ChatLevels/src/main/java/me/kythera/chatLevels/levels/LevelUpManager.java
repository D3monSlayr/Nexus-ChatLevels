package me.kythera.chatLevels.levels;

import me.kythera.chatLevels.ChatLevels;
import me.kythera.chatLevels.configs.DataSaving;
import me.kythera.chatLevels.rewards.LvlRewards;
import me.kythera.chatLevels.rewards.Reward;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.kythera.chatLevels.configs.DataSaving.getConfiguration;

public class LevelUpManager {

    // In-memory XP tracking
    private static final Map<UUID, Double> xpRecord = new HashMap<>();

    // --- XP Methods ---

    public static double getXP(UUID uuid) {
        DataSaving.saveConfig();
        ConfigurationSection section = getConfiguration().getConfigurationSection(uuid.toString());
        if (section != null) {
            return section.getDouble("xp", xpRecord.getOrDefault(uuid, 0.0));
        }
        return xpRecord.getOrDefault(uuid, 0.0);
    }

    public static void setXP(UUID uuid, double xp) {
        xpRecord.put(uuid, xp);

        // Update config in memory but don’t save immediately
        ConfigurationSection section = getOrCreateSection(uuid);
        section.set("xp", xp);
        section.set("level", getLevel(uuid));

        computeLevel(uuid);
    }

    public static void addXP(UUID uuid, double amount) {
        setXP(uuid, getXP(uuid) + amount);
    }

    // --- Level Methods ---

    public static int getLevel(UUID uuid) {
        DataSaving.saveConfig();
        double xp = getXP(uuid);
        int level = 0;

        while (xp >= getXpRequiredForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public static void setLevel(UUID uuid, int level) {
        ConfigurationSection section = getOrCreateSection(uuid);
        section.set("xp", getXpRequiredForLevel(level));
        section.set("level", level);

        computeLevel(uuid);
    }

    public static void computeLevel(UUID uuid) {
        double xp = getXP(uuid);
        int currentLevel = getLevel(uuid);
        int newLevel = currentLevel;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        while (xp >= getXpRequiredForLevel(newLevel + 1)) {
            newLevel++;
        }

        if (newLevel > currentLevel) {
            player.sendRichMessage(LvlRewards.getLevelUpMsg(player, newLevel));

            for (Reward reward : LvlRewards.getRewardsFor(newLevel)) {
                if (reward.isMaterial()) {
                    player.getInventory().addItem(new ItemStack(reward.getMaterial(), reward.getAmount()));
                } else if (reward.isCommand()) {
                    String cmd = reward.getCommand()
                            .replace("%xp%", String.valueOf(getXP(player.getUniqueId())))
                            .replace("%level%", String.valueOf(getLevel(player.getUniqueId())))
                            .replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }

            ConfigurationSection section = getOrCreateSection(uuid);
            section.set("level", newLevel);
        }
    }

    // --- Utility Methods ---

    public static double getXpRequiredForLevel(int level) {
        return 50 * level * level;
    }

    public static double getXpRequiredToReach(UUID player, int level) {
        return 50 * level * level - getXP(player);
    }

    public static void reloadData() {
        xpRecord.clear();

        for (String id : getConfiguration().getKeys(false)) {
            ConfigurationSection section = getConfiguration().getConfigurationSection(id);
            if (section == null) continue;

            try {
                UUID uuid = UUID.fromString(id);
                double xp = section.getDouble("xp", 0.0);
                xpRecord.put(uuid, xp);

                int level = getLevel(uuid);
                section.set("level", level);
                section.set("xp", xp);
            } catch (IllegalArgumentException ex) {
                ChatLevels.getInstance().getLogger().warning("Skipping invalid UUID: " + id);
            }
        }
    }

    private static ConfigurationSection getOrCreateSection(UUID uuid) {
        ConfigurationSection section = getConfiguration().getConfigurationSection(uuid.toString());
        if (section == null) {
            section = getConfiguration().createSection(uuid.toString());
        }
        return section;
    }

    // --- Periodic Auto-Save ---
    public static void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ChatLevels.getInstance(), DataSaving::saveConfig, 20L * 300, 20L * 300); // Every 5 minutes
    }

    // --- Utility / other ----

    public static void showNormalHelp(CommandSender sender) {
        sender.sendRichMessage("<gray>-----------------------------");
        sender.sendRichMessage("<gold>/chatlevels help <gray>-<aqua> Shows this menu.");
        sender.sendRichMessage("<gold>/chatlevels xp <gray>-<aqua> Shows your current Chat XP.");
        sender.sendRichMessage("<gold>/chatlevels xp player <player> <gray>-<aqua> Shows the Chat XP of a player.");
        sender.sendRichMessage("<gold>/chatlevels for <level> <gray>-<aqua> Shows the XP needed for a Chat level.");
        sender.sendRichMessage("<gold>/chatlevels level <gray>-<aqua> Shows your current Chat Level.");
        sender.sendRichMessage("<gold>/chatlevels level for <player> <gray>-<aqua> Shows a player’s level.");
        sender.sendRichMessage("<gray>-----------------------------");
    }

    public static void showOpHelp(CommandSender sender) {
        sender.sendRichMessage("<gray>-----------------------------");
        sender.sendRichMessage("<gold>/chatlevels reload <gray>-<aqua> Reloads rewards and levels.");
        sender.sendRichMessage("<gold>/chatlevels admin set xp <player> <xp> <gray>-<aqua> Sets ChatXP for a player.");
        sender.sendRichMessage("<gold>/chatlevels admin set level <player> <level> <gray>-<aqua> Sets Chat Level for a player.");
        sender.sendRichMessage("<gold>/chatlevels rewards add material <level> <material>[:amount] <gray>-<aqua> Adds a material reward.");
        sender.sendRichMessage("<gold>/chatlevels rewards add command <level> <command> <gray>-<aqua> Adds a command reward.");
        sender.sendRichMessage("<gold>/chatlevels rewards remove <level> <reward> <gray>-<aqua> Removes a reward.");
        sender.sendRichMessage("<gold>/chatlevels rewards get <level> <gray>-<aqua> Shows all rewards for a level.");
        sender.sendRichMessage("<gray>-----------------------------");
    }
}
