package me.kythera.chatLevels.configs;

import me.kythera.chatLevels.ChatLevels;
import me.kythera.chatLevels.levels.LevelUpManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataSaving {

    private static File file;
    private static YamlConfiguration configuration;

    public static void loadConfig() {
        File dataFolder = ChatLevels.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            ChatLevels.getInstance().getLogger().info("plugins/NexusChatLevels created successfully.");
        }

        file = new File(dataFolder, "saving.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                ChatLevels.getInstance().getLogger().info("'saving.yml' created successfully.");
            } catch (IOException e) {
                ChatLevels.getInstance().getLogger().severe("Failed to create 'saving.yml'");
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
            ChatLevels.getInstance().getLogger().severe("Failed to save 'saving.yml'");
            throw new RuntimeException(e);
        }

    }

    public static void reloadConfig() {
        saveConfig(); // save current data to file
        DataSaving.loadConfig(); // reload the actual YAML file
        LevelUpManager.reloadData(); // rebuild in-memory data
    }

    public static YamlConfiguration getConfiguration() {
        return configuration;
    }

}
