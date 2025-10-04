package me.kythera.chatLevels.configs;

import me.kythera.chatLevels.ChatLevels;
import me.kythera.chatLevels.levels.LevelUpManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class RewardLoader {
    private static File file;
    private static YamlConfiguration configuration;

    public static void loadConfig() {
        File dataFolder = ChatLevels.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            ChatLevels.getInstance().getLogger().info("plugins/NexusChatLevels created successfully.");
        }

        file = new File(dataFolder, "rewards.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                ChatLevels.getInstance().getLogger().info("'rewards.yml' created successfully.");
            } catch (IOException e) {
                ChatLevels.getInstance().getLogger().severe("Failed to create 'rewards.yml'");
                throw new RuntimeException(e);
            }
        }

        configuration = YamlConfiguration.loadConfiguration(file);
        LevelUpManager.reloadData();
    }

    public static void saveConfig() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            ChatLevels.getInstance().getLogger().severe("Failed to save 'rewards.yml'");
            throw new RuntimeException(e);
        }

    }

    public static void reloadConfig() {
        saveConfig(); // Save any unsaved reward edits
        loadConfig(); // Reload rewards.yml from disk
        ChatLevels.getInstance().getLogger().info("'rewards.yml' reloaded successfully.");
    }

    public static YamlConfiguration getConfiguration() {
        return configuration;
    }

}

