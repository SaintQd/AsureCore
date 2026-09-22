package org.saintqd.asurecore.commands;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import kotlin.Pair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import net.kyori.adventure.util.TriState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.intellij.lang.annotations.Subst;
import org.saintqd.asurecore.AsureCore;
import org.saintqd.asurecore.gui.CalendarEventGUI;
import org.saintqd.asurecore.gui.DecorationGUI;
import org.saintqd.asurecore.gui.HMCCosmeticsGUI;
import org.saintqd.asurecore.gui.ItemSkinGUI;
import org.saintqd.asurecore.managers.*;
import org.saintqd.asurelib.AsureLib;
import org.saintqd.asurelib.managers.LangManager;
import org.saintqd.asurelib.managers.VaultManager;
import org.saintqd.asurelib.utils.AsureUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsureCommandsManager {

    public static void setupCommands(AsureCore plugin) {
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("asurecore")
                            .then(Commands.literal("reload")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                    .executes(ctx -> {
                                        reloadCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("joinmessage")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.joinmessage")
                                            && AsureLib.inst().getVaultManager() != null
                                            && AsureLib.inst().getVaultManager().getPermissionProvider() != null)
                                    .executes(ctx -> {
                                        setJoinMessageCommand(ctx.getSource().getSender(),null,null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("message", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                setJoinMessageCommand(ctx.getSource().getSender(),ctx.getArgument("message",String.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("leavemessage")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.leavemessage")
                                            && AsureLib.inst().getVaultManager() != null
                                            && AsureLib.inst().getVaultManager().getPermissionProvider() != null)
                                    .executes(ctx -> {
                                        setLeaveMessageCommand(ctx.getSource().getSender(),null,null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("message", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                setLeaveMessageCommand(ctx.getSource().getSender(),ctx.getArgument("message",String.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("adminjoinmessage")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin")
                                            && AsureLib.inst().getVaultManager() != null
                                            && AsureLib.inst().getVaultManager().getPermissionProvider() != null)
                                    .then(Commands.argument("message", StringArgumentType.string())
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .executes(ctx -> {
                                                        setJoinMessageCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("message",String.class),
                                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("adminleavemessage")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin")
                                            && AsureLib.inst().getVaultManager() != null
                                            && AsureLib.inst().getVaultManager().getPermissionProvider() != null)
                                    .then(Commands.argument("message", StringArgumentType.string())
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .executes(ctx -> {
                                                        setLeaveMessageCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("message",String.class),
                                                                ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("pvptoggle")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.pvptoggle"))
                                    .executes(ctx -> {
                                        pvpToggleCommand(ctx.getSource().getSender(),null);
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                            .executes(ctx -> {
                                                pvpToggleCommand(ctx.getSource().getSender(),ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("headdrop")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.headdroptoggle"))
                                    .executes(ctx -> {
                                        headDropToggleCommand(ctx.getSource().getSender(),null,null);
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("state", StringArgumentType.word())
                                            .suggests((ctx,builder) -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                headDropToggleCommand(ctx.getSource().getSender(),ctx.getArgument("state",String.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        headDropToggleCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("state",String.class),
                                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("deathknockout")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.deathknockouttoggle"))
                                    .executes(ctx -> {
                                        knockoutToggleCommand(ctx.getSource().getSender(),null,null);
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("state", StringArgumentType.word())
                                            .suggests((ctx,builder) -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                knockoutToggleCommand(ctx.getSource().getSender(),ctx.getArgument("state",String.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        knockoutToggleCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("state",String.class),
                                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("togglephantoms")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.togglephantoms"))
                                    .executes(ctx -> {
                                        phantomsToggleCommand(ctx.getSource().getSender(),null,null);
                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("state", StringArgumentType.word())
                                            .suggests((ctx,builder) -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                phantomsToggleCommand(ctx.getSource().getSender(),ctx.getArgument("state",String.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        phantomsToggleCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("state",String.class),
                                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("savedata")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                    .executes(ctx -> {
                                        saveDataCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("hint")
                                    .executes(ctx -> {
                                        sendHintCommand(ctx.getSource().getSender(),-1,null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("index", IntegerArgumentType.integer())
                                            .executes(ctx -> {
                                                sendHintCommand(ctx.getSource().getSender(),ctx.getArgument("index", Integer.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        sendHintCommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("index", Integer.class),
                                                                ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("transferaccount")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                    .then(Commands.argument("old_player_name", StringArgumentType.string())
                                            .suggests((ctx,builder) -> {
                                                String partName = builder.getRemaining();
                                                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                                                    if (onlinePlayer.getName().startsWith(partName))
                                                        builder.suggest(onlinePlayer.getName());
                                                });
                                                return builder.buildFuture();
                                            })
                                            .then(Commands.argument("new_player_name", StringArgumentType.string())
                                                    .suggests((ctx,builder) -> {
                                                        String partName = builder.getRemaining();
                                                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                                                            if (onlinePlayer.getName().startsWith(partName))
                                                                builder.suggest(onlinePlayer.getName());
                                                        });
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> {
                                                        transferAccountCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("old_player_name", String.class),
                                                                ctx.getArgument("new_player_name", String.class));
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("changenickname")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.changenickname")
                                    || predicate.getSender().hasPermission("asurecore.changenickname.once"))
                                    .then(Commands.argument("new_player_name", StringArgumentType.string())
                                            .suggests((ctx,builder) -> {
                                                String partName = builder.getRemaining();
                                                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                                                    if (onlinePlayer.getName().startsWith(partName))
                                                        builder.suggest(onlinePlayer.getName());
                                                });
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                changeAccountNicknameCommand(ctx.getSource().getSender(),
                                                        ctx.getLastChild().getArgument("new_player_name", String.class),
                                                        null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("old_player_name", StringArgumentType.string())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .suggests((ctx,builder) -> {
                                                        String partName = builder.getRemaining();
                                                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                                                            if (onlinePlayer.getName().startsWith(partName))
                                                                builder.suggest(onlinePlayer.getName());
                                                        });
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> {
                                                        changeAccountNicknameCommand(ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("new_player_name", String.class),
                                                                ctx.getArgument("old_player_name", String.class));
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("lockitem")
                                    .requires(predicate -> predicate.getSender() instanceof Player)
                                    .executes(ctx -> {
                                        lockItemCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("unlockitem")
                                    .requires(predicate -> predicate.getSender() instanceof Player)
                                    .executes(ctx -> {
                                        unlockItemCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("signitem")
                                    .requires(predicate -> predicate.getSender() instanceof Player)
                                    .executes(ctx -> {
                                        signItemCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("unsignitem")
                                    .requires(predicate -> predicate.getSender() instanceof Player)
                                    .executes(ctx -> {
                                        unsignItemCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("me")
                                    .requires(predicate -> predicate.getSender() instanceof Player
                                            && predicate.getSender().hasPermission("asurecore.me"))
                                    .then(Commands.argument("action", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                meCommand(ctx.getSource().getSender(),ctx.getArgument("action", String.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("try")
                                    .requires(predicate -> predicate.getSender() instanceof Player
                                            && predicate.getSender().hasPermission("asurecore.try"))
                                    .then(Commands.argument("action", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                tryCommand(ctx.getSource().getSender(),ctx.getArgument("action", String.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("do")
                                    .requires(predicate -> predicate.getSender() instanceof Player
                                            && predicate.getSender().hasPermission("asurecore.do"))
                                    .then(Commands.argument("action", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                doCommand(ctx.getSource().getSender(),ctx.getArgument("action", String.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("blockdata")
                                    .requires(predicate -> predicate.getSender() instanceof Player
                                            && predicate.getSender().hasPermission("asurecore.blockdata"))
                                    .executes(ctx -> {
                                        blockDataCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("itemskin")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.itemskin"))
                                    .executes(ctx -> {
                                        itemSkinCommand(ctx.getSource().getSender(),null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                            .executes(ctx -> {
                                                itemSkinCommand(ctx.getSource().getSender(),ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("cosmetic")
                                    .requires(predicate -> AsureCore.inst().isHmcCosmeticsEnabled() &&
                                            predicate.getSender().hasPermission("asurecore.cosmetic"))
                                    .then(Commands.argument("type", StringArgumentType.word())
                                            .suggests((ctx,builder) -> {
                                                String partName = builder.getRemaining();
                                                com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot.values().keySet().forEach(cosmeticSlotName -> {
                                                    if (cosmeticSlotName.toLowerCase().startsWith(partName.toLowerCase()))
                                                        builder.suggest(cosmeticSlotName);
                                                });
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                cosmeticCommand(
                                                        ctx.getSource().getSender(),
                                                        ctx.getArgument("type",String.class),
                                                        null
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        cosmeticCommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("type",String.class),
                                                                ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("decoration")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.decoration"))
                                    .executes(ctx -> {
                                        decorationCommand(ctx.getSource().getSender(),null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                            .executes(ctx -> {
                                                decorationCommand(ctx.getSource().getSender(),ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("opentrade")
                                    .then(Commands.argument("name", StringArgumentType.word())
                                            .suggests((ctx,builder) -> {
                                                String partName = builder.getRemaining();
                                                TradeManager.Companion.getInstance().getTradeSets().keySet().forEach(tradeName -> {
                                                    if (tradeName.startsWith(partName))
                                                        builder.suggest(tradeName);
                                                });
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                openTradeCommand(
                                                        ctx.getSource().getSender(),
                                                        ctx.getArgument("name", String.class),
                                                        null
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        openTradeCommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("name",String.class),
                                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
                                                        );
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("mailbook")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.mailbook"))
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
                                                sendMailbookCommand(
                                                        ctx.getSource().getSender(),
                                                        ctx.getArgument("player",String.class),
                                                        false,
                                                        "");
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("hide_author", BoolArgumentType.bool())
                                                    .executes(ctx -> {
                                                        sendMailbookCommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("player",String.class),
                                                                ctx.getArgument("hide_author",Boolean.class),
                                                                "");
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                                    .then(Commands.argument("title", StringArgumentType.greedyString())
                                                            .executes(ctx -> {
                                                                sendMailbookCommand(
                                                                        ctx.getSource().getSender(),
                                                                        ctx.getLastChild().getLastChild().getArgument("player",String.class),
                                                                        ctx.getLastChild().getArgument("hide_author",Boolean.class),
                                                                        ctx.getArgument("title",String.class));
                                                                return Command.SINGLE_SUCCESS;
                                                            })
                                                    )
                                            )
                                    )
                            )
                            .then(Commands.literal("mailreceive")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.mailbook"))
                                    .executes(ctx -> {
                                        receiveMailbookCommand(
                                                ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("confirmationstatus")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.confirmationstatus"))
                                    .then(Commands.argument("state", BoolArgumentType.bool())
                                            .executes(ctx -> {
                                                setConfirmationStatusCommand(ctx.getSource().getSender(),ctx.getArgument("state", Boolean.class),null);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                                    .executes(ctx -> {
                                                        setConfirmationStatusCommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("state", Boolean.class),
                                                                ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("events")
                                    .executes(ctx -> {
                                        eventsMenuCommand(ctx.getSource().getSender(),null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .requires(predicate -> predicate.getSender().hasPermission("asurecore.admin"))
                                            .executes(ctx -> {
                                                eventsMenuCommand(ctx.getSource().getSender(),ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("inspect")
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .requires(predicate -> predicate.getSender().hasPermission("asurecore.inspect"))
                                            .executes(ctx -> {
                                                inspectPlayerCommand(ctx.getSource().getSender(),
                                                        ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("guide")
                                    .requires( predicate -> !predicate.getSender().hasPermission(
                                            AsureCore.inst().getConfig().getString("Guide.CompletedPermission","asurecore.guide.completed"))
                                            && AsureCore.inst().getConfig().getBoolean("Guide.Enabled",true))
                                    .executes(ctx -> {
                                        guideApplyCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("guidecomplete")
                                    .requires(predicate -> predicate.getSender().hasPermission(
                                            AsureCore.inst().getConfig().getString("Guide.ReceivePermission","asurecore.guide.receive"))
                                            && AsureCore.inst().getConfig().getBoolean("Guide.Enabled",true))
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .executes(ctx -> {
                                                guideCompleteCommand(ctx.getSource().getSender(),
                                                        ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("guidepick")
                                    .requires(predicate -> predicate.getSender().hasPermission(
                                            AsureCore.inst().getConfig().getString("Guide.ReceivePermission","asurecore.guide.receive"))
                                            && AsureCore.inst().getConfig().getBoolean("Guide.Enabled",true))
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .executes(ctx -> {
                                                guidePickCommand(ctx.getSource().getSender(),
                                                        ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("consolemsg")
                                    .requires(predicate -> predicate.getSender().hasPermission("asurecore.consolemsg"))
                                    .then(Commands.argument("message", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                sendMessageToConsoleCommand(ctx.getSource().getSender(),ctx.getArgument("message",String.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(SuffixCommandsManager.getSuffixCommands())
                            .then(OresCommandsManager.getOresCommands())
                            .build(),
                    "Основная команда."
            );

        });
    }

    private static void reloadCommand(CommandSender sender) {
        AsureCore.inst().loadData();
        if (sender instanceof Player)
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"reload_message"));
    }

    private static void saveDataCommand(CommandSender sender) {
        AsureCore.inst().saveData();
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_save_data"));
    }

    private static void setJoinMessageCommand(CommandSender sender, String message, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender,player);
        VaultManager vaultManager = AsureLib.inst().getVaultManager();

        String joinMessagePermission = null;
        List<String> possibleJoinMessage = player.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission)
                .filter(permission -> permission.startsWith("meta.join-message.")).toList();
        if (!possibleJoinMessage.isEmpty())
            joinMessagePermission = possibleJoinMessage.getFirst();

        if (message == null || message.isEmpty()) {
            if (joinMessagePermission != null)
                vaultManager.getPermissionProvider().playerRemove(null,player,joinMessagePermission);
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"join_message_removed"));
            return;
        }
        if (!message.contains("*") && !message.contains(player.getName())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"join_message_hint"));
            return;
        }
        long nameCount = Pattern.compile("\\*")
                .matcher(message).results().count();
        if (nameCount > 1) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"join_leave_message_name_limit"));
            return;
        }
        message = message.replace("<newline>","");
        // При использовании Alias CMI знак % заменяется на %. , ломая плейсхолдеры
        // Исправляем это фиксом ниже
        message = message.replace("%.","%");

        String joinMessage = message.replace(player.getName(),"*").replace(".","[dot]");
        joinMessage = PlainTextComponentSerializer.plainText().serialize(AsureUtils.parseString(joinMessage));

        int maxLength = AsureCore.inst().getConfig().getInt("Messages.MaxLength",100);
        if (joinMessage.length() > maxLength) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"custom_message_too_long",Integer.toString(maxLength)));
        }

        joinMessage = joinMessage.replaceAll("(\\p{Lu})", "╝$1");
        joinMessage = joinMessage.toLowerCase();

        if (joinMessagePermission != null)
            vaultManager.getPermissionProvider().playerRemove(null,player,joinMessagePermission);
        vaultManager.getPermissionProvider().playerAdd(null,player,"meta.join-message."
                + joinMessage);

        if (sender == player)
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"join_message_applied"));
        else
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"join_message_applied",player.getName()));
        String joinMessageFormat = AsureCore.inst().getConfig().getString("Messages.Join.Format","<white>>> <gray>[message]");
        joinMessageFormat = joinMessageFormat.replace("[message]",joinMessage)
                .replace("[dot]",".");
        joinMessageFormat = Pattern.compile("╝+(.)?").matcher(joinMessageFormat).replaceAll(mr -> mr.group(1).toUpperCase());
        joinMessageFormat = joinMessageFormat.replace("*", AsureCore.inst().getConfig().getString("Messages.NicknameFormat",player.getName()));
        joinMessageFormat = (AsureCore.inst().getPlaceholders() != null)
                ? me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,joinMessageFormat))
                : joinMessageFormat;
        sender.sendMessage(AsureUtils.parseString(joinMessageFormat));
    }

    private static void setLeaveMessageCommand(CommandSender sender, String message, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender,player);
        VaultManager vaultManager = AsureLib.inst().getVaultManager();

        String leaveMessagePermission = null;
        List<String> possibleLeaveMessage = player.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission)
                .filter(permission -> permission.startsWith("meta.leave-message.")).toList();
        if (!possibleLeaveMessage.isEmpty())
            leaveMessagePermission = possibleLeaveMessage.getFirst();

        if (message == null || message.isEmpty()) {
            if (leaveMessagePermission != null)
                vaultManager.getPermissionProvider().playerRemove(null,player,leaveMessagePermission);
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"leave_message_removed"));
            return;
        }
        if (!message.contains("*") && !message.contains(player.getName())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"leave_message_hint"));
            return;
        }
        long nameCount = Pattern.compile("\\*")
                .matcher(message).results().count();
        if (nameCount > 1) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"join_leave_message_name_limit"));
            return;
        }
        message = message.replace("<newline>","");
        message = message.replace("%.","%");

        String leaveMessage = message.replace(player.getName(),"*").replace(".","[dot]");
        leaveMessage = PlainTextComponentSerializer.plainText().serialize(AsureUtils.parseString(leaveMessage));

        int maxLength = AsureCore.inst().getConfig().getInt("Messages.MaxLength",100);
        if (leaveMessage.length() > maxLength) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"custom_message_too_long",Integer.toString(maxLength)));
        }

        leaveMessage = leaveMessage.replaceAll("(\\p{Lu})", "╝$1");
        leaveMessage = leaveMessage.toLowerCase();

        if (leaveMessagePermission != null)
            vaultManager.getPermissionProvider().playerRemove(null,player,leaveMessagePermission);
        vaultManager.getPermissionProvider().playerAdd(null,player,"meta.leave-message."
                + leaveMessage);

        if (sender == player)
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"leave_message_applied"));
        else
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"leave_message_applied_admin",player.getName()));
        String leaveMessageFormat = AsureCore.inst().getConfig().getString("Messages.Leave.Format","<white<<< <gray>[message]");
        leaveMessageFormat = leaveMessageFormat.replace("[message]",leaveMessage)
                .replace("[dot]",".");
        leaveMessageFormat = Pattern.compile("╝+(.)?").matcher(leaveMessageFormat).replaceAll(mr -> mr.group(1).toUpperCase());
        leaveMessageFormat = leaveMessageFormat.replace("*", AsureCore.inst().getConfig().getString("Messages.NicknameFormat",player.getName()));
        leaveMessageFormat = (AsureCore.inst().getPlaceholders() != null)
                ? me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,leaveMessageFormat))
                : leaveMessageFormat;
        sender.sendMessage(AsureUtils.parseString(leaveMessageFormat));
    }

    private static void pvpToggleCommand(CommandSender sender, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);
        PlayerManager playerManager = AsureCore.inst().getPlayerManager();

        HashMap<Player, ImmutablePair<String,Long>> timers = playerManager.getTimers().getOrDefault("pvp_toggle_cooldown",new HashMap<>());
        ImmutablePair<String,Long> timerVariable = timers.getOrDefault(player,new ImmutablePair<>(null,0L));

        if (sender == player && !player.hasPermission("asurecore.admin")) {
            double minRadiusWithoutPlayers = AsureCore.inst().getConfig().getDouble("PvPMode.MinRadiusWithoutPlayers",-1);
            if (minRadiusWithoutPlayers > 0 && player.getWorld().getNearbyPlayers(player.getLocation(), minRadiusWithoutPlayers).size() > 1) {
                player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"pvp_min_radius_without_players",Double.toString(minRadiusWithoutPlayers)));
                return;
            }
            if (timerVariable.getRight() > AsureUtils.getCurrentTick()) {
                long remainingTime = (timerVariable.getRight() - AsureUtils.getCurrentTick()) / 20;
                player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"pvp_toggle_cooldown",Long.toString(remainingTime)));
                return;
            }
        }
        if (playerManager.getPvpModePlayers().contains(player)) {
            playerManager.getPvpModePlayers().remove(player);
            if (sender != player)
                player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"pvp_mode_off_for_player", player.getName()));
            player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"pvp_mode_off"));
        }
        else {
            playerManager.getPvpModePlayers().add(player);
            if (sender != player)
                player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"pvp_mode_on_for_player", player.getName()));
            player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"pvp_mode_on"));
        }
        timerVariable = new ImmutablePair<>(null,AsureUtils.getCurrentTick() + AsureCore.inst().getConfig()
                .getLong("TimersCooldown.pvp_toggle_cooldown",6000L));
        timers.put(player,timerVariable);
        playerManager.getTimers().put("pvp_toggle_cooldown",timers);
    }

    private static void headDropToggleCommand(CommandSender sender, String state, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);
        VaultManager vaultManager = AsureLib.inst().getVaultManager();

        boolean boolState;
        if (state == null) {
            TriState triState = player.permissionValue("asurecore.headdrop");
            boolState = triState != TriState.TRUE;
        }
        else
            boolState = Boolean.parseBoolean(state);

        if (boolState) {
            vaultManager.getPermissionProvider().playerAdd(null,player,"asurecore.headdrop");
            if (sender == player)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"head_drop_true"));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"head_drop_true_other",player.getName()));
        } else {
            vaultManager.getPermissionProvider().playerRemove(null,player,"asurecore.headdrop");
            if (sender == player)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"head_drop_false"));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"head_drop_false_other",player.getName()));
        }
    }

    private static void knockoutToggleCommand(CommandSender sender, String state, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);
        VaultManager vaultManager = AsureLib.inst().getVaultManager();

        boolean boolState;
        if (state == null) {
            TriState triState = player.permissionValue("asurecore.deathknockout");
            boolState = triState != TriState.TRUE;
        }
        else
            boolState = Boolean.parseBoolean(state);

        if (boolState) {
            vaultManager.getPermissionProvider().playerAdd(null,player,"asurecore.deathknockout");
            if (sender == player)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"death_knockout_toggle_false"));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"death_knockout_toggle_false_other",player.getName()));
        } else {
            vaultManager.getPermissionProvider().playerRemove(null,player,"asurecore.deathknockout");
            if (sender == player)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"death_knockout_toggle_true"));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"death_knockout_toggle_true_other",player.getName()));
        }
    }

    private static void phantomsToggleCommand(CommandSender sender, String state, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);
        VaultManager vaultManager = AsureLib.inst().getVaultManager();

        boolean boolState;
        if (state == null) {
            TriState triState = player.permissionValue("asurecore.phantomsdisabled");
            boolState = triState != TriState.TRUE;
        }
        else
            boolState = Boolean.parseBoolean(state);

        if (boolState) {
            vaultManager.getPermissionProvider().playerAdd(null,player,"asurecore.phantomsdisabled");
            if (sender == player)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"phantoms_toggle_false"));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"phantoms_toggle_false_other",player.getName()));
        } else {
            vaultManager.getPermissionProvider().playerRemove(null,player,"asurecore.phantomsdisabled");
            if (sender == player)
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"phantoms_toggle_true"));
            else
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"phantoms_toggle_true_other",player.getName()));
        }
    }

    private static void sendHintCommand(CommandSender sender, int hintIndex, Player player) {
        if (hintIndex < 0)
            hintIndex = ThreadLocalRandom.current().nextInt(0, AsureCore.inst().getHintManager().getHints().size());
        if (hintIndex >= AsureCore.inst().getHintManager().getHints().size()) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"hint_does_not_exist"));
            return;
        }
        String hint = AsureCore.inst().getHintManager().getHints().get(hintIndex);
        String hintPrefix = AsureLib.inst().getLangManager().getLangLines().getOrDefault(
                Key.key(AsureCore.inst(),"hint_prefix"),"hint_prefix").replace("{1}",Integer.toString(hintIndex));
        String finalHint = hintPrefix + hint;
        if (player != null) {
            player.sendRichMessage(finalHint);
        }
        else {
            sender.sendRichMessage(finalHint);
        }
    }

    private static void changeAccountNicknameCommand(CommandSender sender, String newPlayerName, String oldPlayerName) {
        Player player;
        if (oldPlayerName == null) {
            player = AsureUtils.checkForPlayerPresent(sender, null);
            if (player == null) {
                return;
            }
            oldPlayerName = player.getName();
        }
        player = Bukkit.getPlayer(oldPlayerName);
        if (player != null) {
            if (sender == player) {
                if (!AsureCore.inst().getConfigManager().getTransferConfirmations().getOrDefault(oldPlayerName,oldPlayerName).equals(newPlayerName)) {
                    sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"account_transfer_changenickname_confirm_message",newPlayerName));
                    AsureCore.inst().getConfigManager().getTransferConfirmations().put(oldPlayerName,newPlayerName);
                    return;
                }
            }
            if (player.hasPermission("asurecore.changenickname.once") && AsureLib.inst().getVaultManager() != null) {
                AsureLib.inst().getVaultManager().getPermissionProvider().playerRemove(null,player,"asurecore.changenickname.once");
            }
            player.kick(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"account_transfer_kick_message"));
        }
        AsureCore.inst().getConfigManager().getPendingAccountTransfers().put(oldPlayerName,new Pair<>(newPlayerName, Instant.now().getEpochSecond()));
        AsureCore.inst().getConfigManager().getAccountTransferNewToOldNicknames().put(newPlayerName,oldPlayerName);
        for (String command : AsureCore.inst().getConfig().getStringList("AccountTransfer.CommandsBeforeTransfer")) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),command
                    .replace("%old_player_name%",oldPlayerName).replace("%new_player_name%",newPlayerName));
        }
        if (sender != player)
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"account_transfer_changenickname_message",oldPlayerName,newPlayerName));
    }

    private static void transferAccountCommand(CommandSender sender, String oldPlayerName, String newPlayerName) {
        AsureCore.inst().getConfigManager().performAccountTransfer(sender,oldPlayerName,newPlayerName);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void lockItemCommand(CommandSender sender) {
        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        ItemStack templateItem = player.getInventory().getItemInMainHand();
        if (!AsureCore.inst().getConfigManager().getItemLockMaterials().contains(templateItem.getType().name())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"lock_item_hint"));
            return;
        }
        if (templateItem.getPersistentDataContainer().has(ConfigManager.getLockKey())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"lock_item_already_locked"));
            return;
        }
        templateItem.editPersistentDataContainer(pdc -> pdc.set(
                ConfigManager.getLockKey(), PersistentDataType.STRING,player.getName()));
        String customName;
        if (templateItem.hasData(DataComponentTypes.CUSTOM_NAME)) {
            customName = MiniMessage.miniMessage().serialize(templateItem.getData(DataComponentTypes.CUSTOM_NAME));
            customName = customName + " " + AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:lock_item_name"));
        }
        else
            customName = "<lang:" + templateItem.getType().translationKey() + "> " + AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:lock_item_name"));
        templateItem.setData(DataComponentTypes.CUSTOM_NAME,AsureUtils.parseString(customName));
        templateItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true);
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"lock_item_success"));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void unlockItemCommand(CommandSender sender) {
        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        ItemStack templateItem = player.getInventory().getItemInMainHand();
        if (!AsureCore.inst().getConfigManager().getItemLockMaterials().contains(templateItem.getType().name())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"lock_item_hint"));
            return;
        }
        if (!templateItem.getPersistentDataContainer().has(ConfigManager.getLockKey())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"unlock_item_already_unlocked"));
            return;
        }
        String ownerName = templateItem.getPersistentDataContainer().getOrDefault(ConfigManager.getLockKey(),PersistentDataType.STRING,"");
        if (!ownerName.equals(player.getName())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"unlock_item_not_owner"));
            return;
        }
        templateItem.editPersistentDataContainer(pdc -> pdc.remove(
                ConfigManager.getLockKey()));
        if (templateItem.hasData(DataComponentTypes.CUSTOM_NAME)) {
            String customName = MiniMessage.miniMessage().serialize(templateItem.getData(DataComponentTypes.CUSTOM_NAME));
            customName = customName.replace(" " + AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:lock_item_name")),"");
            if (customName.equals("<!italic><lang:"+templateItem.getType().translationKey()+">"))
                templateItem.resetData(DataComponentTypes.CUSTOM_NAME);
            else
                templateItem.setData(DataComponentTypes.CUSTOM_NAME,AsureUtils.parseString(customName));
        }
        templateItem.resetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"unlock_item_success"));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void signItemCommand(CommandSender sender) {
        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        ItemStack templateItem = player.getInventory().getItemInMainHand();
        if (templateItem.getType() == Material.AIR) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"sign_item_command_hint"));
            return;
        }
        if (templateItem.getPersistentDataContainer().has(ConfigManager.getSignKey())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"sign_item_already_signed"));
            return;
        }
        templateItem.editPersistentDataContainer(pdc -> pdc.set(
                ConfigManager.getSignKey(), PersistentDataType.STRING,player.getName()));
        List<Component> lore = templateItem.hasData(DataComponentTypes.LORE)
                ? templateItem.getData(DataComponentTypes.LORE).lines()
                : new ArrayList<>();
        ItemLore.Builder newLore = ItemLore.lore().addLines(lore)
                .addLine(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"sign_item_command_lore",player.getName()));
        templateItem.setData(DataComponentTypes.LORE,newLore);
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"sign_item_command_success"));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void unsignItemCommand(CommandSender sender) {
        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        ItemStack templateItem = player.getInventory().getItemInMainHand();
        if (templateItem.getType() == Material.AIR) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"sign_item_command_hint"));
            return;
        }
        String ownerName = templateItem.getPersistentDataContainer().getOrDefault(ConfigManager.getSignKey(),PersistentDataType.STRING,"");
        if (ownerName.isEmpty()) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"unsign_item_not_signed"));
            return;
        }
        if (!ownerName.equals(player.getName())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"unsign_item_not_owner"));
            return;
        }
        templateItem.editPersistentDataContainer(pdc -> pdc.remove(
                ConfigManager.getSignKey()));
        if (templateItem.hasData(DataComponentTypes.LORE)) {
            List<Component> lore = new ArrayList<>(templateItem.getData(DataComponentTypes.LORE).lines());
            if (!lore.isEmpty())
                lore.removeLast();
            templateItem.setData(DataComponentTypes.LORE,ItemLore.lore(lore));
        }
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"unsign_item_command_success"));
    }

    private static void meCommand(CommandSender sender, String action) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        int radius = AsureCore.inst().getConfig().getInt("Messages.Me.Distance",25);
        String format = AsureCore.inst().getConfig().getString("Messages.Me.Format","");
        if (format.isEmpty())
            return;
        action = MiniMessage.miniMessage().stripTags(action);
        format = format.replace("%name%", AsureCore.inst().getConfig().getString("Messages.NicknameFormat",player.getName()))
                .replace("%message%",action);

        Component text = AsureLib.inst().isPlaceholderAPIEnabled()
                ? AsureUtils.parseString(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,format)))
                : AsureUtils.parseString(format);

        Audience audience = Audience.audience(player.getWorld().getNearbyPlayers(player.getLocation(),radius));
        audience.sendMessage(text);
    }

    private static void doCommand(CommandSender sender, String action) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        int radius = AsureCore.inst().getConfig().getInt("Messages.Do.Distance",25);
        String format = AsureCore.inst().getConfig().getString("Messages.Do.Format","");
        if (format.isEmpty())
            return;
        action = MiniMessage.miniMessage().stripTags(action);
        format = format.replace("%name%", AsureCore.inst().getConfig().getString("Messages.NicknameFormat",player.getName()))
                .replace("%message%",action);

        Component text = AsureLib.inst().isPlaceholderAPIEnabled()
                ? AsureUtils.parseString(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,format)))
                : AsureUtils.parseString(format);

        Audience audience = Audience.audience(player.getWorld().getNearbyPlayers(player.getLocation(),radius));
        audience.sendMessage(text);
    }

    private static void tryCommand(CommandSender sender, String action) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        int radius = AsureCore.inst().getConfig().getInt("Messages.Try.Distance",25);
        String format = AsureCore.inst().getConfig().getString("Messages.Try.Format","");
        if (format.isEmpty())
            return;

        action = MiniMessage.miniMessage().stripTags(action);
        format = format.replace("%name%", AsureCore.inst().getConfig().getString("Messages.NicknameFormat",player.getName()))
                .replace("%message%",action);

        double chance = 0.5;
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?%");
        Matcher matcher = pattern.matcher(action);
        while (matcher.find()) {
            String match = matcher.group();
            String numberString = match.substring(0, match.length() - 1);
            chance = Double.parseDouble(numberString);
        }
        format = format.replaceAll("\\d+(\\.\\d+)?%","").replaceAll("\\s+", " ").trim();
        if (chance > 1)
            chance = chance / 100;
        if (chance < 0 || chance > 1)
            chance = 0.5;

        double result = ThreadLocalRandom.current().nextDouble();
        String textResult;
        if (result < chance) {
            if (result < 0.05)
                textResult = AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:command_try_success_critical"));
            else
                textResult = AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:command_try_success"));
        }
        else {
            if (result > 0.95)
                textResult = AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:command_try_fail_critical"));
            else
                textResult = AsureLib.inst().getLangManager().getLangLines().get(NamespacedKey.fromString("asurecore:command_try_fail"));
        }
        format = format.replace("%result%",textResult);

        Component text = AsureLib.inst().isPlaceholderAPIEnabled()
                ? AsureUtils.parseString(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,format)))
                : AsureUtils.parseString(format);
        Audience audience = Audience.audience(player.getWorld().getNearbyPlayers(player.getLocation(),radius));
        audience.sendMessage(text);
    }

    private static void itemSkinCommand(CommandSender sender, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);

        ItemSkinGUI skinGUI = new ItemSkinGUI(player);
        skinGUI.setItemSkinsMenu(player.getInventory().getItemInMainHand(),1);
        if (skinGUI.getInventory() != null)
            player.openInventory(skinGUI.getInventory());
    }

    private static void cosmeticCommand(CommandSender sender, String cosmeticSlotName, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);

        HMCCosmeticsGUI cosmeticsGUI = new HMCCosmeticsGUI(player);
        CosmeticSlot cosmeticSlot = CosmeticSlot.valueOf(cosmeticSlotName);

        if (cosmeticSlot == null) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"cosmetic_gui_wrong_slot",cosmeticSlotName));
            return;
        }

        cosmeticsGUI.setMainMenu(cosmeticSlot,1);
        if (cosmeticsGUI.getInventory() != null)
            player.openInventory(cosmeticsGUI.getInventory());
    }

    private static void decorationCommand(CommandSender sender, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);

        if (!AsureCore.inst().isNexoEnabled()) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"decoration_no_support"));
            return;
        }

        DecorationGUI decorationGUI = new DecorationGUI(player);
        decorationGUI.setDecorationMenu(null);
        player.openInventory(decorationGUI.getInventory());
    }

    private static void blockDataCommand(CommandSender sender) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        Block block = player.getTargetBlockExact(7);
        if (block == null) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_blockdata_no_block"));
            return;
        }
        if (!(block.getState() instanceof TileStateInventoryHolder state)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_blockdata_no_block"));
            return;
        }
        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_blockdata_title",block.getType().name()));
        if (state.getPersistentDataContainer().has(ShulkerAlertManager.SHULKER_UUID_KEY)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_blockdata_list_format",ShulkerAlertManager.SHULKER_UUID_KEY.getKey(),
                    state.getPersistentDataContainer().get(ShulkerAlertManager.SHULKER_UUID_KEY,PersistentDataType.STRING)));
        }
    }

    private static void openTradeCommand(CommandSender sender, String tradeName, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);

        TradeManager tradeManager = TradeManager.Companion.getInstance();

        if (!tradeManager.getTradeSets().containsKey(tradeName)) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"open_trade_command_does_not_exist",tradeName));
            return;
        }
        TradeManager.AsureTradeSet tradeSet = tradeManager.getTradeSets().get(tradeName);

        if (sender == player && (!sender.hasPermission(tradeSet.getPermission()) && !sender.hasPermission("asurecore.admin"))) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"no_permission"));
            return;
        }
        tradeManager.openMerchant(player,tradeSet,tradeManager.createMerchant(tradeSet));
        if (sender != player) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"open_trade_command_for_player",tradeName, player.getName()));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void sendMailbookCommand(CommandSender sender, String receiverName, boolean hidden, String title) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        if (receiverName == null) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_receiver_no_receiver"));
            return;
        }
        OfflinePlayer receiverPlayer = Bukkit.getOfflinePlayer(receiverName);
        if (!receiverPlayer.hasPlayedBefore()) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_receiver_not_found",receiverName));
            return;
        }

        MailbookManager mailbookManager = MailbookManager.INSTANCE;
        ItemStack possibleBookItem = player.getInventory().getItemInMainHand();

        if (possibleBookItem.getType() != Material.WRITABLE_BOOK && possibleBookItem.getType() != Material.WRITTEN_BOOK) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_wrong_item_type"));
            return;
        }
        if (MailbookManager.Companion.getMAILBOOK_KEY() != null) {
            if (!possibleBookItem.getPersistentDataContainer().has(MailbookManager.Companion.getMAILBOOK_KEY())) {
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "mailbook_no_key"));
                return;
            }
        }
        possibleBookItem.setData(DataComponentTypes.CUSTOM_NAME,AsureUtils.parseString(title));
        possibleBookItem.resetData(DataComponentTypes.LORE);
        if (MailbookManager.Companion.getMAILBOOK_KEY() != null) {
            possibleBookItem.editPersistentDataContainer(pdc -> pdc.remove(MailbookManager.Companion.getMAILBOOK_KEY()));
        }
        String uncoloredTitle = MiniMessage.miniMessage().stripTags(title);

        String encodedItemStack = mailbookManager.createMailbook(player,possibleBookItem,uncoloredTitle,hidden);
        possibleBookItem.setAmount(possibleBookItem.getAmount() - 1);

        List<String> unread = mailbookManager.getUnreadMailbooks().getOrDefault(receiverPlayer.getUniqueId(), new ArrayList<>());
        unread.add(encodedItemStack);
        mailbookManager.getUnreadMailbooks().put(receiverPlayer.getUniqueId(), unread);

        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_send_success",receiverName));
        if (receiverPlayer.isOnline() && receiverPlayer.getPlayer() != null) {
            receiverPlayer.getPlayer().sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_receive_message"));
        }
    }

    private static void receiveMailbookCommand(CommandSender sender) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);

        MailbookManager mailbookManager = MailbookManager.INSTANCE;

        if (!mailbookManager.getUnreadMailbooks().containsKey(player.getUniqueId())) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_receive_no_mails"));
            return;
        }

        List<ItemStack> mailbookItems = mailbookManager.decodeMailbooks(player.getUniqueId());
        player.give(mailbookItems);

        mailbookManager.getUnreadMailbooks().remove(player.getUniqueId());

        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"mailbook_receive_success",Integer.toString(mailbookItems.size())));
    }

    private static void setConfirmationStatusCommand(CommandSender sender, boolean state, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);
        if (player == null)
            return;
        PlayerManager playerManager = AsureCore.inst().getPlayerManager();
        VaultManager vaultManager = AsureLib.inst().getVaultManager();

        HashMap<Player, ImmutablePair<String,Long>> timers = playerManager.getTimers().getOrDefault("confirmation_status_cooldown",new HashMap<>());
        ImmutablePair<String,Long> timerVariable = timers.getOrDefault(player,new ImmutablePair<>(null,0L));
        String permission = AsureCore.inst().getConfig().getString("ConfirmationStatus.Permission","group.rp");

        if (sender == player) {
            if (state) {
                if (player.permissionValue(permission) == TriState.TRUE) {
                    sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_confirmation_status_already_has"));
                }
                else {
                    if (timerVariable.getRight() > AsureUtils.getCurrentTick()) {
                        vaultManager.getPermissionProvider().playerAdd(null, player, permission);
                        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_confirmation_status_success"));
                    } else {
                        sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_confirmation_status"));
                        timerVariable = new ImmutablePair<>(null, AsureUtils.getCurrentTick() + AsureCore.inst().getConfig()
                                .getLong("TimersCooldown.pvp_toggle_cooldown", 6000L));
                        timers.put(player, timerVariable);
                        playerManager.getTimers().put("confirmation_status_cooldown", timers);
                    }
                }
            }
            else {
                if (player.hasPermission("asurecore.admin")) {
                    vaultManager.getPermissionProvider().playerRemove(null,player,permission);
                    sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_confirmation_status_remove"));
                }
                else {
                    sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "no_permission"));
                }
            }
        }
        else {
            if (state) {
                vaultManager.getPermissionProvider().playerAdd(null,player,permission);
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_confirmation_status_success_for_player",player.getName()));
            }
            else {
                vaultManager.getPermissionProvider().playerRemove(null,player,permission);
                sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_confirmation_status_remove_for_player",player.getName()));
            }
        }
    }

    private static void eventsMenuCommand(CommandSender sender, Player player) {

        player = AsureUtils.checkForPlayerPresent(sender, player);
        if (player == null)
            return;

        CalendarEventGUI timedEventGUI = new CalendarEventGUI(player);
        timedEventGUI.setMainMenu();
        player.openInventory(timedEventGUI.getInventory());
    }

    private static void inspectPlayerCommand(CommandSender sender, Player inspectedPlayer) {

        if (inspectedPlayer.hasPermission("asurecore.admin")) {
            sender.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_inspect_wrong_player"));
            return;
        }
        if (AsureCore.inst().getPlayerManager().getInspectedPlayers().containsKey(inspectedPlayer.getUniqueId())) {
            BukkitRunnable runnable = AsureCore.inst().getPlayerManager().getInspectedPlayers().remove(inspectedPlayer.getUniqueId());
            runnable.cancel();
            inspectedPlayer.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_inspect_resume"));
            Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("asurecore.inspect")).forEach(player -> {
                player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_inspect_resume_message",inspectedPlayer.getName()));
            });
        }
        else {
            inspectedPlayer.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_inspect_chat_message",inspectedPlayer.getName()));
            Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("asurecore.inspect")).forEach(player -> {
                player.sendMessage(AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(),"command_inspect_message",inspectedPlayer.getName()));
            });
            UUID playerUuid = inspectedPlayer.getUniqueId();
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Player playerByUuid = Bukkit.getPlayer(playerUuid);
                    if (playerByUuid != null && playerByUuid.isValid()) {
                        playerByUuid.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,100,4,false,false));
                        playerByUuid.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,100,0,false,false));
                        playerByUuid.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,100,200,false,false));
                        playerByUuid.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,100,0,false,false));
                        playerByUuid.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,100,200,false,false));

                        playerByUuid.showTitle(Title.title(
                                AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_inspect_title"),
                                AsureLib.inst().getLangManager().parseLangString(AsureCore.inst(), "command_inspect_subtitle"),
                                Title.Times.times(Ticks.duration(0), Ticks.duration(30), Ticks.duration(20))
                        ));
                        playerByUuid.setFallDistance(0.0f);
                    }
                    else {
                        AsureCore.inst().getPlayerManager().getInspectedPlayers().remove(playerUuid);
                        this.cancel();
                    }
                }
            };
            runnable.runTaskTimer(AsureCore.inst(), 20, 20);
            AsureCore.inst().getPlayerManager().getInspectedPlayers().put(inspectedPlayer.getUniqueId(),runnable);
        }
    }

    private static void guideApplyCommand(CommandSender sender) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);
        if (player == null)
            return;

        player.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_apply_message"));
        String receivePermission = AsureCore.inst().getConfig().getString("Guide.ReceivePermission","asurecore.guide.receive");

        String[] soundData = AsureCore.inst().getConfig().getString("Guide.ApplySound","").split(",");
        @Subst("minecraft:entity.player.levelup") String soundName = soundData[0];
        float pitch = (soundData.length > 1) ? Float.parseFloat(soundData[1]) : 1.0f;
        net.kyori.adventure.sound.Sound sound = (!soundName.isEmpty())
                ? net.kyori.adventure.sound.Sound.sound(Key.key(soundName), Sound.Source.PLAYER,1f,pitch)
                : null;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(receivePermission)) {
                onlinePlayer.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_receive_message", player.getName()));
                if (sound != null)
                    onlinePlayer.playSound(sound,player);
            }
        }
    }

    private static void guideCompleteCommand(CommandSender sender, Player appliedPlayer) {

        String completePermission = AsureCore.inst().getConfig().getString("Guide.CompletedPermission","asurecore.guide.completed");

        if (appliedPlayer.hasPermission(completePermission)) {
            sender.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_already_completed_message", appliedPlayer.getName()));
            return;
        }
        VaultManager vaultManager = AsureLib.inst().getVaultManager();
        if (vaultManager == null)
            return;
        vaultManager.getPermissionProvider().playerAdd(null,appliedPlayer,completePermission);

        AsureCore.inst().getConfigManager().getGuidePickedPlayers().remove(appliedPlayer.getUniqueId());

        appliedPlayer.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_complete_message", sender.getName()));
        sender.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_complete_sender_message", appliedPlayer.getName()));
    }

    private static void guidePickCommand(CommandSender sender, Player appliedPlayer) {

        Player player = AsureUtils.checkForPlayerPresent(sender, null);
        if (player == null)
            return;

        UUID receiverPlayer = AsureCore.inst().getConfigManager().getGuidePickedPlayers().get(appliedPlayer.getUniqueId());
        if (receiverPlayer != null) {
            Player pickerPlayer =  Bukkit.getPlayer(receiverPlayer);
            if (pickerPlayer != null && pickerPlayer.isOnline()) {
                sender.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_picked_already_message", appliedPlayer.getName(),pickerPlayer.getName()));
                return;
            }
        }
        AsureCore.inst().getConfigManager().getGuidePickedPlayers().put(appliedPlayer.getUniqueId(),player.getUniqueId());
        player.teleportAsync(appliedPlayer.getLocation());

        appliedPlayer.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_picked_message", player.getName()));

        String receivePermission = AsureCore.inst().getConfig().getString("Guide.ReceivePermission","asurecore.guide.receive");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(receivePermission)) {
                onlinePlayer.sendMessage(LangManager.INSTANCE.parseLangString(AsureCore.inst(),"guide_picked_sender_message", player.getName(), appliedPlayer.getName()));
            }
        }
    }

    private static void sendMessageToConsoleCommand(CommandSender sender, String message) {

        Bukkit.getServer().getConsoleSender().sendRichMessage(message);
    }
}
