package org.saintqd.asurecore.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.saintqd.asurecore.AsureCore;
import org.saintqd.asurecore.gui.SuffixGUI;
import org.saintqd.asurecore.managers.SuffixManager;
import org.saintqd.asurecore.suffix.CommunitySuffix;
import org.saintqd.asurecore.suffix.AsureSuffix;
import org.saintqd.asurelib.AsureLib;
import org.saintqd.asurelib.utils.AsureUtils;

import java.util.List;

public class SuffixCommandsManager {

    private static final LiteralArgumentBuilder<CommandSourceStack> suffixCommands = Commands.literal("suffix")
            .executes(commandContext -> {
                openSuffixMenuCommand(commandContext.getSource().getSender(),null);
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("menu")
                    .executes(ctx -> {
                        openSuffixMenuCommand(ctx.getSource().getSender(),null);
                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("player", ArgumentTypes.player())
                            .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                            .executes(ctx -> {
                                openSuffixMenuCommand(ctx.getSource().getSender(),ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                            })
                    )
            )
            .then(Commands.literal("set")
                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                    .executes(ctx -> {
                        setSuffixCommand(ctx.getSource().getSender(),null,null);
                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("suffix", StringArgumentType.word())
                            .suggests((ctx,builder) -> {
                                String partName = builder.getRemaining();
                                AsureCore.inst().getSuffixManager().getSuffixes().forEach((suffixName, suffix) -> {
                                    if (suffixName.toLowerCase().startsWith(partName.toLowerCase()))
                                        builder.suggest(suffixName);
                                });
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                setSuffixCommand(ctx.getSource().getSender(),ctx.getArgument("suffix", String.class),null);
                                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("player", ArgumentTypes.player())
                                    .executes(ctx -> {
                                        setSuffixCommand(ctx.getSource().getSender(),
                                                ctx.getLastChild().getArgument("suffix", String.class),
                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            .then(Commands.literal("clear")
                    .executes(ctx -> {
                        clearSuffixCommand(ctx.getSource().getSender(),null);
                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    })
                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                    .then(Commands.argument("player", ArgumentTypes.player())
                            .executes(ctx -> {
                                clearSuffixCommand(ctx.getSource().getSender(),
                                        ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                            })
                    )
            )
            .then(Commands.literal("adduser")
                    .then(Commands.argument("suffix", StringArgumentType.word())
                            .suggests((ctx,builder) -> {
                                String partName = builder.getRemaining();
                                AsureCore.inst().getSuffixManager().getCommunitySuffixes().forEach((suffixName, suffix) -> {
                                    if (suffixName.toLowerCase().startsWith(partName.toLowerCase()))
                                        builder.suggest(suffixName);
                                });
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests((ctx,builder) -> {
                                        String partName = builder.getRemaining();
                                         Bukkit.getOnlinePlayers().forEach((player) -> {
                                             if (player.getName().toLowerCase().startsWith(partName.toLowerCase()))
                                                 builder.suggest(player.getName());
                                        });
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        communitySuffixAddUserCommand(ctx.getSource().getSender(),
                                                ctx.getLastChild().getArgument("suffix", String.class),
                                                ctx.getArgument("player", String.class));
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            .then(Commands.literal("removeuser")
                    .then(Commands.argument("suffix", StringArgumentType.word())
                            .suggests((ctx,builder) -> {
                                String partName = builder.getRemaining();
                                AsureCore.inst().getSuffixManager().getCommunitySuffixes().forEach((suffixName, suffix) -> {
                                    if (suffixName.toLowerCase().startsWith(partName.toLowerCase()))
                                        builder.suggest(suffixName);
                                });
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests((ctx,builder) -> {
                                        CommunitySuffix suffix = AsureCore.inst().getSuffixManager()
                                                .getCommunitySuffixes().get(ctx.getLastChild().getArgument("suffix", String.class));
                                        if (suffix == null)
                                            return builder.buildFuture();
                                        List<String> suffixPlayers = suffix.getUsers();
                                        String partName = builder.getRemaining();
                                        suffixPlayers.forEach((player) -> {
                                             if (player.toLowerCase().startsWith(partName.toLowerCase()))
                                                 builder.suggest(player);
                                        });
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        communitySuffixRemoveUserCommand(ctx.getSource().getSender(),
                                                ctx.getLastChild().getArgument("suffix", String.class),
                                                ctx.getArgument("player", String.class));
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            .then(Commands.literal("list")
                    .then(Commands.argument("suffix", StringArgumentType.word())
                            .suggests((ctx,builder) -> {
                                String partName = builder.getRemaining();
                                AsureCore.inst().getSuffixManager().getCommunitySuffixes().forEach((suffixName, suffix) -> {
                                    if (suffixName.toLowerCase().startsWith(partName.toLowerCase()))
                                        builder.suggest(suffixName);
                                });
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                communitySuffixListCommand(ctx.getSource().getSender(),
                                        ctx.getArgument("suffix", String.class));
                                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                            })
                    )
            )
            .then(Commands.literal("setlimit")
                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                    .then(Commands.argument("suffix", StringArgumentType.word())
                            .suggests((ctx,builder) -> {
                                String partName = builder.getRemaining();
                                AsureCore.inst().getSuffixManager().getCommunitySuffixes().forEach((suffixName, suffix) -> {
                                    if (suffixName.toLowerCase().startsWith(partName.toLowerCase()))
                                        builder.suggest(suffixName);
                                });
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("limit", IntegerArgumentType.integer(0))
                                    .executes(ctx -> {
                                        communitySuffixSetLimitCommand(ctx.getSource().getSender(),
                                                ctx.getLastChild().getArgument("suffix", String.class),
                                                ctx.getArgument("limit", Integer.class));
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            ;

    public static LiteralArgumentBuilder<CommandSourceStack> getSuffixCommands() {
        return suffixCommands;
    }

    private static void openSuffixMenuCommand(CommandSender sender, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender,player);
        if (player == null) return;

        SuffixGUI suffixGUI = new SuffixGUI(player);
        suffixGUI.setMainMenu(1);
        suffixGUI.openInventory();
        if (sender != player)
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"suffix_menu_opened_for_player", player.getName()));
    }

    private static void setSuffixCommand(CommandSender sender, String suffixName, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender,player);
        if (player == null) return;
        if (suffixName == null) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"suffix_command_usage"));
            return;
        }
        AsureSuffix suffix = AsureCore.inst().getSuffixManager().getSuffixes().get(suffixName);
        if (suffix == null) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"suffix_does_not_exist",suffixName));
            return;
        }
        AsureCore.inst().getSuffixManager().changeSuffix(sender,player,suffix);
    }

    private static void clearSuffixCommand(CommandSender sender, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender,player);
        if (player == null) return;
        AsureCore.inst().getSuffixManager().clearSuffix(sender,player);
    }

    private static void communitySuffixListCommand(CommandSender sender, String suffixName) {

        SuffixManager suffixManager = AsureCore.inst().getSuffixManager();
        if (!suffixManager.getCommunitySuffixes().containsKey(suffixName)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_does_not_exist",suffixName));
            return;
        }
        AsureSuffix suffix = suffixManager.getSuffixes().get(suffixName);
        CommunitySuffix communitySuffix = suffixManager.getCommunitySuffixes().get(suffixName);
        if (!sender.hasPermission(communitySuffix.getPermission())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_no_permission"));
            return;
        }
        int index = 1;
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_list_message",
                suffix.getParsedPlaceholder(),suffixName,Integer.toString(communitySuffix.getUsers().size()),Integer.toString(communitySuffix.getSuffixLimit())));
        for (String username : communitySuffix.getUsers()) {
            Player onlinePlayer = Bukkit.getPlayer(username);
            if (onlinePlayer != null)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),
                        "community_suffix_list_online_message_format",Integer.toString(index),username));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),
                    "community_suffix_list_message_format",Integer.toString(index),username));
            index++;
        }
    }

    private static void communitySuffixAddUserCommand(CommandSender sender, String suffixName, String playerName) {

        SuffixManager suffixManager = AsureCore.inst().getSuffixManager();
        if (!suffixManager.getCommunitySuffixes().containsKey(suffixName)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_does_not_exist",suffixName));
            return;
        }
        AsureSuffix suffix = suffixManager.getSuffixes().get(suffixName);
        CommunitySuffix communitySuffix = suffixManager.getCommunitySuffixes().get(suffixName);
        if (!sender.hasPermission(communitySuffix.getPermission())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_no_permission"));
            return;
        }
        if (!communitySuffix.isPossibleToAdd()) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_over_limit"));
            return;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_player_not_found",playerName));
            return;
        }
        communitySuffix.getUsers().remove(playerName);
        communitySuffix.getUsers().add(playerName);
        if (AsureLib.inst().getVaultManager() != null && AsureLib.inst().getVaultManager().getPermissionProvider() != null)
            Bukkit.getAsyncScheduler().runNow(AsureCore.inst(), task -> {
                AsureLib.inst().getVaultManager().getPermissionProvider().playerAdd(null,offlinePlayer,suffix.getPermission());
            });
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_add_message",suffix.getParsedPlaceholder()));
        }
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_added_successfully",playerName,suffix.getParsedPlaceholder()));
    }

    private static void communitySuffixRemoveUserCommand(CommandSender sender, String suffixName, String playerName) {

        SuffixManager suffixManager = AsureCore.inst().getSuffixManager();
        if (!suffixManager.getCommunitySuffixes().containsKey(suffixName)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_does_not_exist",suffixName));
            return;
        }
        AsureSuffix suffix = suffixManager.getSuffixes().get(suffixName);
        CommunitySuffix communitySuffix = suffixManager.getCommunitySuffixes().get(suffixName);
        if (!sender.hasPermission(communitySuffix.getPermission())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_no_permission"));
            return;
        }
        if (!communitySuffix.getUsers().contains(playerName)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_community_player_not_found",playerName));
            return;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        communitySuffix.getUsers().remove(playerName);
        if (AsureLib.inst().getVaultManager() != null && AsureLib.inst().getVaultManager().getPermissionProvider() != null)
            Bukkit.getAsyncScheduler().runNow(AsureCore.inst(), task -> {
                AsureLib.inst().getVaultManager().getPermissionProvider().playerRemove(null,offlinePlayer,suffix.getPermission());
            });
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            AsureCore.inst().getSuffixManager().checkSuffixPermission(offlinePlayer.getPlayer());
            offlinePlayer.getPlayer().sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_remove_message",suffix.getParsedPlaceholder()));
        }
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_removed_successfully",playerName,suffix.getParsedPlaceholder()));
    }

    private static void communitySuffixSetLimitCommand(CommandSender sender, String suffixName, int limit) {

        SuffixManager suffixManager = AsureCore.inst().getSuffixManager();
        if (!suffixManager.getCommunitySuffixes().containsKey(suffixName)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_does_not_exist",suffixName));
            return;
        }
        AsureSuffix suffix = suffixManager.getSuffixes().get(suffixName);
        CommunitySuffix communitySuffix = suffixManager.getCommunitySuffixes().get(suffixName);
        communitySuffix.setSuffixLimit(limit);
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"community_suffix_set_limit_successfully",suffix.getParsedPlaceholder(),Integer.toString(limit)));
    }
}
