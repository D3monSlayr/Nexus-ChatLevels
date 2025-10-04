package me.kythera.chatLevels.rewards;

import me.kythera.chatLevels.ChatLevels;
import me.kythera.chatLevels.configs.RewardLoader;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LvlRewards {

    public static List<Reward> getRewardsFor(int level) {
        String lvlToString = String.valueOf(level);
        ConfigurationSection section = RewardLoader.getConfiguration().getConfigurationSection(lvlToString);
        List<Reward> rewardsCreated = new ArrayList<>();

        if (section != null) {
            // Handle materials with optional amounts
            List<String> materials = section.getStringList("materials");
            for (String entry : materials) {
                if (entry == null || entry.trim().isEmpty()) continue;

                String[] parts = entry.split(":");
                String matName = parts[0].trim().toUpperCase();
                int amount = 1; // default if no amount given

                if (parts.length > 1) {
                    try {
                        amount = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        ChatLevels.getInstance().getLogger().warning("Invalid amount for material '" + entry + "' at level " + lvlToString + ", defaulting to 1");
                    }
                }

                Material mat = Material.getMaterial(matName);
                if (mat != null) {
                    rewardsCreated.add(new Reward(mat, amount));
                } else {
                    ChatLevels.getInstance().getLogger().severe("Invalid material for level " + lvlToString + ": " + matName);
                }
            }

            // Handle commands
            List<String> commands = section.getStringList("commands");
            for (String cmd : commands) {
                if (cmd != null && !cmd.trim().isEmpty()) {
                    rewardsCreated.add(new Reward(cmd));
                }
            }
        }

        return rewardsCreated;
    }

    public static List<String> getRewards(int level) {
        List<String> formatted = new ArrayList<>();

        for (Reward reward : getRewardsFor(level)) {
            if (reward.getMaterial() != null) {
                formatted.add("Material: " + reward.getMaterial().name() + " x" + reward.getAmount());
            } else if (reward.getCommand() != null) {
                formatted.add("Command: " + reward.getCommand());
            }
        }

        return formatted;
    }

    /**
     * Add a material reward to a specific level.
     */
    public static void addMaterialReward(int level, Material material, int amount) {
        String lvlToString = String.valueOf(level);

        List<String> materials = RewardLoader.getConfiguration().getStringList(lvlToString + ".materials");

        String rewardEntry = material.name() + ":" + amount;

        // Prevent duplicates like adding DIAMOND_SWORD:3 twice
        if (!materials.contains(rewardEntry)) {
            materials.add(rewardEntry);
            RewardLoader.getConfiguration().set(lvlToString + ".materials", materials);
            RewardLoader.saveConfig();
        }
    }

    /**
     * Remove a material reward from a specific level.
     */
    public static void removeMaterialReward(int level, Material material) {
        String lvlToString = String.valueOf(level);

        List<String> materials = RewardLoader.getConfiguration().getStringList(lvlToString + ".materials");
        boolean removed = materials.removeIf(entry -> entry.toUpperCase().startsWith(material.name()));
        if (removed) {
            RewardLoader.getConfiguration().set(lvlToString + ".materials", materials);
            RewardLoader.saveConfig();
        }
    }

    /**
     * Add a command reward to a specific level.
     */
    public static void addCommandReward(int level, String command) {
        String lvlToString = String.valueOf(level);

        List<String> commands = RewardLoader.getConfiguration().getStringList(lvlToString + ".commands");
        if (!commands.contains(command)) {
            commands.add(command);
            RewardLoader.getConfiguration().set(lvlToString + ".commands", commands);
            RewardLoader.saveConfig();
        }
    }

    /**
     * Remove a command reward from a specific level.
     */
    public static void removeCommandReward(int level, String command) {
        String lvlToString = String.valueOf(level);

        List<String> commands = RewardLoader.getConfiguration().getStringList(lvlToString + ".commands");
        if (commands.remove(command)) {
            RewardLoader.getConfiguration().set(lvlToString + ".commands", commands);
            RewardLoader.saveConfig();
        }
    }

    public static boolean removeReward(int level, String reward) {
        String lvlToString = String.valueOf(level);
        ConfigurationSection section = RewardLoader.getConfiguration().getConfigurationSection(lvlToString);

        if (section == null) return false;

        // Check materials
        List<String> materials = section.getStringList("materials");
        if (materials.removeIf(mat -> mat.equalsIgnoreCase(reward))) {
            section.set("materials", materials);
            RewardLoader.saveConfig();
            return true;
        }

        // Check commands
        List<String> commands = section.getStringList("commands");
        if (commands.removeIf(cmd -> cmd.equalsIgnoreCase(reward))) {
            section.set("commands", commands);
            RewardLoader.saveConfig();
            return true;
        }

        return false;
    }

    public static List<String> getRewardNamesForTab(int level) {
        List<String> options = new ArrayList<>();

        for (Reward reward : getRewardsFor(level)) {
            if (reward.getMaterial() != null) {
                options.add(reward.getMaterial().name());
            } else if (reward.getCommand() != null) {
                options.add(reward.getCommand());
            }
        }

        return options;
    }

    public static String getLevelUpMsg(Player referral, int level) {
        ConfigurationSection section = ChatLevels.getInstance().getConfig().getConfigurationSection("messages");

        if(section == null) {
            ChatLevels.getInstance().getLogger().severe("The 'messages' section in the config is missing!");
            return "<green>Congrats! You leveled up to <gold>{level}<green>!"
                    .replace("{level}", String.valueOf(level));
        }

        return Objects.requireNonNull(section.getString("level-up"))
                .replace("{level}", String.valueOf(level))
                .replace("{player}", referral.getName());

    }

    public static String getNoRewardsMsg(Player referral, int level) {
        ConfigurationSection section = ChatLevels.getInstance().getConfig().getConfigurationSection("messages");

        if(section == null) {
            ChatLevels.getInstance().getLogger().severe("The 'messages' section in the config is missing!");
            return "<red>There are no rewards for this level.";
        }

        return Objects.requireNonNull(section.getString("no-rewards"))
                .replace("{level}", String.valueOf(level))
                .replace("{player}", referral.getName());

    }

    public static String getNoRewardsMsg(int level) {
        ConfigurationSection section = ChatLevels.getInstance().getConfig().getConfigurationSection("messages");

        if(section == null) {
            ChatLevels.getInstance().getLogger().severe("The 'messages' section in the config is missing!");
            return "<red>There are no rewards for this level.";
        }

        return Objects.requireNonNull(section.getString("no-rewards"))
                .replace("{level}", String.valueOf(level));

    }

    public static String getRewardGivenMsg(Player referral, int level) {
        ConfigurationSection section = ChatLevels.getInstance().getConfig().getConfigurationSection("messages");

        if(section == null) {
            ChatLevels.getInstance().getLogger().severe("The 'messages' section in the config is missing!");
            return "<green>You have received your rewards for level <gold>{level}<green>!"
                    .replace("{level}", String.valueOf(level));
        }

        return Objects.requireNonNull(section.getString("reward-given"))
                .replace("{level}", String.valueOf(level))
                .replace("{player}", referral.getName());
    }

}
