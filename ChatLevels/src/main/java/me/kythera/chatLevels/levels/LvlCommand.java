package me.kythera.chatLevels.levels;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.kythera.chatLevels.ChatLevels;
import me.kythera.chatLevels.configs.DataSaving;
import me.kythera.chatLevels.configs.RewardLoader;
import me.kythera.chatLevels.rewards.LvlRewards;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LvlCommand {

    private static boolean hasAdminPerm(CommandSender sender) {
        if (!(sender instanceof Player player)) return true; // console bypass
        return player.hasPermission("chatlevels.admin");
    }

    private static boolean hasUsePerm(CommandSender sender) {
        if (!(sender instanceof Player player)) return true; // console bypass
        return player.hasPermission("chatlevels.use");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("chatlevels")
                // --- RELOAD ---
                .then(Commands.literal("reload")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();

                            if (!hasAdminPerm(sender)) {
                                sender.sendRichMessage("<red>You don't have permission to reload configurations!");
                                return Command.SINGLE_SUCCESS;
                            }

                            ChatLevels.getInstance().reloadConfig();
                            DataSaving.reloadConfig();
                            RewardLoader.reloadConfig();

                            sender.sendRichMessage("<green>✔ All configurations reloaded successfully — Data, Rewards & Main Config!");
                            return Command.SINGLE_SUCCESS;
                        })
                )

                // --- HELP ---
                .then(Commands.literal("help")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!hasUsePerm(sender)) {
                                sender.sendRichMessage("<red>You don't have permission to use Chat Levels commands!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if (sender instanceof Player player) {
                                LevelUpManager.showNormalHelp(sender);
                                if (player.hasPermission("chatlvls.admin")) LevelUpManager.showOpHelp(sender);
                            } else {
                                LevelUpManager.showNormalHelp(sender);
                                LevelUpManager.showOpHelp(sender);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )

                // --- ADMIN COMMANDS ---
                .then(Commands.literal("admin")
                        .requires(src -> src.getSender().hasPermission("chatlvls.admin"))
                        .then(Commands.literal("rewards")

                                // --- ADD ---
                                .then(Commands.literal("add")
                                        // MATERIAL
                                        .then(Commands.literal("material")
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("material", StringArgumentType.word())
                                                                .suggests((ctx, builder) -> {
                                                                    String remaining = builder.getRemainingLowerCase();
                                                                    for (Material mat : Material.values()) {
                                                                        String name = mat.name().toLowerCase();
                                                                        if (name.startsWith(remaining)) {
                                                                            for (int i = 1; i <= 64; i++) builder.suggest(name + ":" + i);
                                                                        }
                                                                    }
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(context -> {
                                                                    CommandSender sender = context.getSource().getSender();
                                                                    int level = IntegerArgumentType.getInteger(context, "level");
                                                                    String input = StringArgumentType.getString(context, "material");

                                                                    if (!hasAdminPerm(sender)) {
                                                                        sender.sendRichMessage("<red>You don't have permission to add rewards!");
                                                                        return Command.SINGLE_SUCCESS;
                                                                    }

                                                                    String[] parts = input.split(":");
                                                                    String matName = parts[0].toUpperCase();
                                                                    int amount = 1;
                                                                    if (parts.length > 1) {
                                                                        try {
                                                                            amount = Integer.parseInt(parts[1]);
                                                                        } catch (NumberFormatException e) {
                                                                            sender.sendRichMessage("<red>Invalid format! Use MATERIAL:AMOUNT");
                                                                            return Command.SINGLE_SUCCESS;
                                                                        }
                                                                    }

                                                                    Material mat = Material.matchMaterial(matName);
                                                                    if (mat == null) {
                                                                        sender.sendRichMessage("<red>Invalid material: <bold>" + matName);
                                                                        return Command.SINGLE_SUCCESS;
                                                                    }

                                                                    LvlRewards.addMaterialReward(level, mat, amount);
                                                                    sender.sendRichMessage("<green>Added <yellow>" + mat.name() + ":" + amount +
                                                                            " <green>as a reward for level <gold>" + level);
                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        )
                                                )
                                        )
                                        // COMMAND
                                        .then(Commands.literal("command")
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("cmd", StringArgumentType.greedyString())
                                                                .executes(context -> {
                                                                    CommandSender sender = context.getSource().getSender();

                                                                    if (!hasAdminPerm(sender)) {
                                                                        sender.sendRichMessage("<red>You don't have permission to add command rewards!");
                                                                        return Command.SINGLE_SUCCESS;
                                                                    }

                                                                    int level = IntegerArgumentType.getInteger(context, "level");
                                                                    String cmd = StringArgumentType.getString(context, "cmd");

                                                                    LvlRewards.addCommandReward(level, cmd);
                                                                    sender.sendRichMessage("<green>Added command reward <yellow>" + cmd +
                                                                            " <green>for level <gold>" + level);
                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        )
                                                )
                                        )
                                )

                                // --- GET ---
                                .then(Commands.literal("get")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    CommandSender sender = context.getSource().getSender();

                                                    if (!hasAdminPerm(sender)) {
                                                        sender.sendRichMessage("<red>You don't have permission to view rewards!");
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    int level = IntegerArgumentType.getInteger(context, "level");
                                                    var rewards = LvlRewards.getRewards(level);

                                                    if (rewards.isEmpty()) {
                                                        sender.sendRichMessage("<red>No rewards set for level <bold>" + level);
                                                    } else {
                                                        sender.sendRichMessage("<green>Rewards for level <gold>" + level + ":");
                                                        for (String reward : rewards) {
                                                            sender.sendRichMessage("<gray>- " + reward);
                                                        }
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )

                                // --- REMOVE ---
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("reward", StringArgumentType.greedyString())
                                                        .suggests((ctx, builder) -> {
                                                            int level = ctx.getArgument("level", Integer.class);
                                                            for (String option : LvlRewards.getRewardNamesForTab(level)) {
                                                                if (option.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                                    builder.suggest(option);
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(context -> {
                                                            CommandSender sender = context.getSource().getSender();

                                                            if (!hasAdminPerm(sender)) {
                                                                sender.sendRichMessage("<red>You don't have permission to remove rewards!");
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            int level = IntegerArgumentType.getInteger(context, "level");
                                                            String reward = StringArgumentType.getString(context, "reward");

                                                            boolean removed = LvlRewards.removeReward(level, reward);
                                                            if (removed) {
                                                                sender.sendRichMessage("<green>Removed reward <yellow>" + reward +
                                                                        " <green>from level <gold>" + level);
                                                            } else {
                                                                sender.sendRichMessage("<red>Reward <yellow>" + reward + " <red>not found!");
                                                            }
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )

                        // --- SET XP / LEVEL ---
                        .then(Commands.literal("set")
                                .then(Commands.literal("xp")
                                        .then(Commands.argument("player", ArgumentTypes.player())
                                                .then(Commands.argument("xp", DoubleArgumentType.doubleArg(1))
                                                        .executes(context -> {
                                                            CommandSender sender = context.getSource().getSender();
                                                            if (!hasAdminPerm(sender)) {
                                                                sender.sendRichMessage("<red>You don't have permission to set XP!");
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            final PlayerSelectorArgumentResolver targetResolver =
                                                                    context.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                            final Player target = targetResolver.resolve(context.getSource()).getFirst();

                                                            double newXP = context.getArgument("xp", Double.class);
                                                            LevelUpManager.setXP(target.getUniqueId(), newXP);

                                                            sender.sendRichMessage("<green>Set <gold>" + target.getName() +
                                                                    "<green>'s XP to <yellow>" + newXP);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("level")
                                        .then(Commands.argument("player", ArgumentTypes.player())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .executes(context -> {
                                                            CommandSender sender = context.getSource().getSender();
                                                            if (!hasAdminPerm(sender)) {
                                                                sender.sendRichMessage("<red>You don't have permission to set levels!");
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            final PlayerSelectorArgumentResolver targetResolver =
                                                                    context.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                            final Player target = targetResolver.resolve(context.getSource()).getFirst();

                                                            int newLevel = context.getArgument("level", Integer.class);
                                                            LevelUpManager.setLevel(target.getUniqueId(), newLevel);

                                                            sender.sendRichMessage("<green>Set <gold>" + target.getName() +
                                                                    "<green>'s level to <yellow>" + newLevel);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )

                // --- XP COMMANDS ---
                .then(Commands.literal("xp")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!hasUsePerm(sender)) {
                                sender.sendRichMessage("<red>You don't have permission to view XP!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Entity executor = context.getSource().getExecutor();
                            if (!(executor instanceof Player player)) {
                                sender.sendRichMessage("<red>Only players can use this command!");
                                return Command.SINGLE_SUCCESS;
                            }

                            player.sendRichMessage("<green>Your current Chat XP: <yellow>" +
                                    LevelUpManager.getXP(player.getUniqueId()));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(context -> {
                                            CommandSender sender = context.getSource().getSender();
                                            if (!hasAdminPerm(sender)) {
                                                sender.sendRichMessage("<red>You don't have permission to check others' XP!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                                                    .resolve(context.getSource()).getFirst();

                                            double xp = LevelUpManager.getXP(target.getUniqueId());
                                            sender.sendRichMessage("<green>" + target.getName() + " has <yellow>" + xp + " XP");
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("for")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            CommandSender sender = context.getSource().getSender();
                                            if (!hasUsePerm(sender)) {
                                                sender.sendRichMessage("<red>You don't have permission to view XP requirements!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            double xpRequired = LevelUpManager.getXpRequiredForLevel(level);

                                            sender.sendRichMessage("<green>XP required for level <yellow>" + level +
                                                    "<green>: <bold>" + xpRequired);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )

                        .then(Commands.literal("required")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(context -> {

                                            CommandSender sender = context.getSource().getSender();

                                            if(!(sender instanceof Player player)) {
                                                sender.sendRichMessage("<red>Only players can use this command!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if (!hasUsePerm(sender)) {
                                                sender.sendRichMessage("<red>You don't have permission to view XP requirements!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            double xpRequired = LevelUpManager.getXpRequiredToReach(player.getUniqueId(), level);

                                            sender.sendRichMessage("<green>You need <bold>" +
                                                    xpRequired +
                                                    "<reset> more ChatXP to reach level <bold>" +
                                                    level +
                                                    "<reset>.");

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )

                // --- LEVEL COMMANDS ---
                .then(Commands.literal("level")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!hasUsePerm(sender)) {
                                sender.sendRichMessage("<red>You don't have permission to view your level!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Entity executor = context.getSource().getExecutor();
                            if (!(executor instanceof Player player)) {
                                sender.sendRichMessage("<red>Only players can use this command!");
                                return Command.SINGLE_SUCCESS;
                            }

                            int level = LevelUpManager.getLevel(player.getUniqueId());
                            player.sendRichMessage("<green>Your current Chat Level: <yellow>" + level);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.literal("for")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(context -> {
                                            CommandSender sender = context.getSource().getSender();
                                            if (!hasAdminPerm(sender)) {
                                                sender.sendRichMessage("<red>You don't have permission to check others' levels!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                                                    .resolve(context.getSource())
                                                    .getFirst();

                                            int level = LevelUpManager.getLevel(target.getUniqueId());
                                            sender.sendRichMessage("<green>" + target.getName() + " is level <yellow>" + level);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                );
    }
}
