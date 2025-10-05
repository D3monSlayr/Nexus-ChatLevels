package me.kythera.chatLevels;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.kythera.chatLevels.configs.DataSaving;
import me.kythera.chatLevels.configs.RewardLoader;
import me.kythera.chatLevels.levels.LevelUpListener;
import me.kythera.chatLevels.levels.LevelUpManager;
import me.kythera.chatLevels.levels.LvlCommand;
import me.kythera.chatLevels.placeholders.AllPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatLevels extends JavaPlugin {

    private static ChatLevels instance;
    private static boolean placeholderapi = false;

    @Override
    public void onEnable() {
        instance = this;

        // Load configs before anything else
        saveDefaultConfig();
        DataSaving.loadConfig();
        RewardLoader.loadConfig();

        // Register events
        getServer().getPluginManager().registerEvents(new LevelUpListener(), this);

        // Register commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(LvlCommand.createCommand().build())
        );

        // PlaceholderAPI support
        if(!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().warning("PlaceholderAPI support is not enabled.");
            getLogger().warning("If you want to enable it, please download PlaceholderAPI.");
        } else {
            new AllPlaceholders().register();
            getComponentLogger().info(Component.text("PlaceholderAPI has been hooked successfully!").color(NamedTextColor.GREEN));
            placeholderapi = true;
        }

        // Autosave
        LevelUpManager.startAutoSaveTask();

        getComponentLogger().info(Component.text("NexusChatLevels 2.0 has been enabled.")
                .color(NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        // Save configs before shutdown
        DataSaving.saveConfig();
        RewardLoader.saveConfig();

        // Placeholder API
        if(placeholderapi) {
            new AllPlaceholders().unregister();
        }

        getComponentLogger().info(Component.text("NexusChatLevels 2.0 has been disabled.")
                .color(NamedTextColor.RED));
    }

    public static ChatLevels getInstance() {
        return instance;
    }
    public static boolean isPlaceholdeApiEnabled() {return  placeholderapi;}
}
