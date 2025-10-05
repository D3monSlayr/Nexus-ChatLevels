package me.kythera.chatLevels.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kythera.chatLevels.levels.LevelUpManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class AllPlaceholders extends PlaceholderExpansion {
    // %chatlevels_xp%, %chatlevels_level%
    @Override
    public @NotNull String getIdentifier() {
        return "chatlevels";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Kythera";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(params.equalsIgnoreCase("xp")) {
            return String.valueOf(LevelUpManager.getXP(player.getUniqueId()));
        } else if (params.equalsIgnoreCase("level")) {
            return String.valueOf(LevelUpManager.getLevel(player.getUniqueId()));
        }

        return null;
    }
}
