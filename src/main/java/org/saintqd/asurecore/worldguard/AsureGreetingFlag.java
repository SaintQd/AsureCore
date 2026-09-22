package org.saintqd.asurecore.worldguard;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

public class AsureGreetingFlag extends Handler {

    public static final AsureGreetingFlag.Factory FACTORY = new AsureGreetingFlag.Factory();
    public static class Factory extends Handler.Factory<AsureGreetingFlag> {
        @Override
        public AsureGreetingFlag create(Session session) {
            return new AsureGreetingFlag(session);
        }
    }

    private Set<String> lastMessageStack = Collections.emptySet();
    private Set<String> lastActionBarStack = Collections.emptySet();
    private Set<String> lastTitleStack = Collections.emptySet();

    public AsureGreetingFlag(Session session) {
        super(session);
    }

    private Set<String> getMessages(LocalPlayer player, ApplicableRegionSet set) {
        return Sets.newLinkedHashSet(set.queryAllValues(player, Flags.ASURE_GREET_MESSAGE));
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {

        Player bukkitPlayer = BukkitAdapter.adapt(player);
        if (bukkitPlayer.permissionValue("asurecore.regionmessage.none") == TriState.TRUE)
            return true;

        if (bukkitPlayer.permissionValue("asurecore.regionmessage.actionbar") == TriState.TRUE) {
            lastActionBarStack = sendAndCollect(player, toSet,
                    lastActionBarStack, AsureMessagingUtil::sendStringToActionBar);
        }
        else if (bukkitPlayer.permissionValue("asurecore.regionmessage.chat") == TriState.TRUE) {
            lastMessageStack = sendAndCollect(player, toSet,
                    lastMessageStack, AsureMessagingUtil::sendStringToChat);
        }
        else
            lastTitleStack = sendAndCollect(player, toSet, lastTitleStack, AsureMessagingUtil::sendStringToTitle);

        return true;
    }

    private Set<String> sendAndCollect(LocalPlayer player, ApplicableRegionSet toSet,
                                       Set<String> stack, BiConsumer<LocalPlayer, String> msgFunc) {
        Collection<String> messages = getMessages(player, toSet);

        for (String message : messages) {
            if (!stack.contains(message)) {
                msgFunc.accept(player, message);
                break;
            }
        }

        stack = Sets.newHashSet(messages);

        if (!stack.isEmpty()) {
            // Due to flag priorities, we have to collect the lower
            // priority flag values separately
            for (ProtectedRegion region : toSet) {
                String message = region.getFlag((Flag<String>) Flags.ASURE_GREET_MESSAGE);
                if (message != null) {
                    stack.add(message);
                }
            }
        }

        return stack;
    }
}
