package me.kythera.chatLevels;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.kythera.chatLevels.configs.DataSaving;
import me.kythera.chatLevels.configs.RewardLoader;
import me.kythera.chatLevels.levels.LevelUpListener;
import me.kythera.chatLevels.levels.LvlCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatLevels extends JavaPlugin {

    private static ChatLevels instance;

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

        getComponentLogger().info(Component.text("✅ NexusChatLevels 1.0 has been enabled.")
                .color(NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        // Save configs before shutdown
        DataSaving.saveConfig();
        RewardLoader.saveConfig();

        getComponentLogger().info(Component.text("❌ NexusChatLevels 1.0 has been disabled.")
                .color(NamedTextColor.RED));
    }

    public static ChatLevels getInstance() {
        return instance;
    }
}
