package org.saintqd.asurecore.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;

public class Flags {
    public final static StateFlag GLIDE = new StateFlag("glide",true);
    public final static StateFlag VILLAGER_TRADE = new StateFlag("villager-trade",true);
    public final static StateFlag END_PORTAL_TELEPORT = new StateFlag("end-portal-teleport",true);
    public final static StateFlag NETHER_PORTAL_TELEPORT = new StateFlag("nether-portal-teleport",true);
    public final static StringFlag ASURE_GREET_MESSAGE = new StringFlag("asure-greeting-message");
    public final static StringFlag ASURE_FAREWELL_MESSAGE = new StringFlag("asure-farewell-message");

    public static void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registry.register(GLIDE);
        registry.register(VILLAGER_TRADE);
        registry.register(END_PORTAL_TELEPORT);
        registry.register(NETHER_PORTAL_TELEPORT);
        registry.register(ASURE_GREET_MESSAGE);
        registry.register(ASURE_FAREWELL_MESSAGE);
    }

    public static void registerHandlers() {
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(AsureGreetingFlag.FACTORY,null);
        sessionManager.registerHandler(AsureFarewellFlag.FACTORY,null);
    }
}
