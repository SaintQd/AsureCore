package org.saintqd.asurecore;

import io.lumine.mythic.core.skills.CustomComponentRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.saintqd.asurelib.AsureLib;
import org.saintqd.asurelib.utils.AsureUtils;
import org.saintqd.asurelib.utils.ResourceUtils;
import org.saintqd.asurecore.commands.AsureCommandsManager;
import org.saintqd.asurecore.listeners.*;
import org.saintqd.asurecore.managers.*;
import org.saintqd.asurecore.placeholders.AsureCorePlaceholders;
import org.saintqd.asurecore.worldguard.Flags;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Getter
public class AsureCore extends JavaPlugin {

    @Getter(AccessLevel.NONE)
    private static AsureCore plugin;
    private ConfigManager configManager;
    private AsureCorePlaceholders placeholders;
    private SuffixManager suffixManager;
    private PlayerManager playerManager;
    private DynamicParamsManager dynamicParamsManager;
    private HintManager hintManager;
    private OreManager oreManager;
    private BukkitTask dynamicParamsTask = null;

    // Совместимость с другими плагинами
    private boolean CMIEnabled = false;
    private boolean liteBansEnabled = false;
    private LuckPermsManager luckPermsManager = null;
    private boolean nexoEnabled = false;
    private boolean mythicMobsEnabled = false;
    private boolean hmcCosmeticsEnabled = false;
    private boolean worldGuardEnabled = false;

    public static AsureCore inst() {
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;

        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
            worldGuardEnabled = true;
            AsureUtils.sendDebugMessage(0,"WorldGuard found, compatibility features enabled.");
            Flags.registerFlags();
        }
    }

    @Override
    public void onEnable() {
        if (worldGuardEnabled) {
            Flags.registerHandlers();
        }
        try {
            ResourceUtils.fetchAllResources(this,getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.configManager = new ConfigManager();
        this.suffixManager = new SuffixManager();
        this.playerManager = new PlayerManager();
        this.dynamicParamsManager = new DynamicParamsManager();
        this.hintManager = new HintManager();
        this.oreManager = new OreManager();

        this.configManager.checkConfigs();

        // Подключаем плейсхолдеры
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders = new AsureCorePlaceholders(this);
            placeholders.registerPlaceholders();
            placeholders.register();
        } else {
            placeholders = null;

            getLogger().warning("Could not find PlaceholderAPI! Placeholders won't be registered.");
        }

        Plugin cmi = Bukkit.getPluginManager().getPlugin("CMI");
        if (cmi != null && cmi.isEnabled()) {
            CMIEnabled = true;
            getServer().getPluginManager().registerEvents(new CMIListener(), this);
            getLogger().info("CMI found, compatibility features enabled.");
        }

        Plugin liteBans = Bukkit.getPluginManager().getPlugin("LiteBans");
        if (liteBans != null && liteBans.isEnabled()) {
            liteBansEnabled = true;
            LiteBansListener.registerEvents();
            getLogger().info("LiteBans found, compatibility features enabled.");
        }

        Plugin luckPerms = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckPerms != null && luckPerms.isEnabled()) {
            this.luckPermsManager = new LuckPermsManager();
            getLogger().info("LuckPerms found, compatibility features enabled.");
        }

        Plugin nexo = Bukkit.getPluginManager().getPlugin("Nexo");
        if (nexo != null && nexo.isEnabled()) {
            this.nexoEnabled = true;
            getLogger().info("Nexo found, compatibility features enabled.");
        }

        Plugin hmcCosmetics = Bukkit.getPluginManager().getPlugin("HMCCosmetics");
        if (hmcCosmetics != null && hmcCosmetics.isEnabled()) {
            this.hmcCosmeticsEnabled = true;
            getLogger().info("HMCCosmetics found, compatibility features enabled.");
        }

        Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (mythicMobs != null && mythicMobs.isEnabled()) {
            this.mythicMobsEnabled = true;
            getLogger().info("MythicMobs found, compatibility features enabled.");

            new CustomComponentRegistry(this,"org.saintqd.asurecore.mythicmobs");
        }

        loadData();

        AsureCommandsManager.setupCommands(this);

        getServer().getPluginManager().registerEvents(new MobListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);

        //Создаем задачу регулярного сохранения данных раз в полчаса
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::saveData, 36000L, 36000L);
    }

    @Override
    public void onDisable() {
        AsureUtils.updateJarFile(this,this.getFile());
        saveData();
    }

    public void loadData() {
        reloadConfig();

        String selectedLang = getConfig().getString("Language");
        HashMap<Key,String> langLines = AsureLib.inst().getLangManager().loadLanguageFile(this,
                plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + selectedLang + ".yml");
        AsureLib.inst().getLangManager().registerLangLines(langLines);

        playerManager.loadParams(this);
        dynamicParamsManager.loadParams(this);
        if (dynamicParamsTask != null)
            dynamicParamsTask.cancel();
        dynamicParamsTask = getServer().getScheduler().runTaskTimer(this,
                () -> dynamicParamsManager.updateWorldCaps(Bukkit.getOnlinePlayers().size()),
                1L,
                getConfig().getLong("DynamicParamsUpdateTime",12000L));

        configManager.loadParams(this);
        configManager.getInjectedVillagerTrades().updateParams();

        long prevTime = System.currentTimeMillis();

        suffixManager.loadSuffixes(this);
        long time = System.currentTimeMillis();
        getLogger().info("Loaded " + suffixManager.getSuffixes().size() + " suffixes. ("+(time-prevTime)+" ms)");
        getLogger().info("Loaded " + suffixManager.getCommunitySuffixes().size() + " community suffixes. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        hintManager.loadHints(this);
        time = System.currentTimeMillis();
        getLogger().info("Loaded " + hintManager.getHints().size() + " hints. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        hintManager.setupStarterHintTask(this);

        ItemSkinManager.INSTANCE.loadItemSkins(this);
        time = System.currentTimeMillis();
        getLogger().info("Loaded " + ItemSkinManager.INSTANCE.getItemSkins().size() + " item skins. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        DecorationManager.Companion.getInstance().loadParams(this);
        time = System.currentTimeMillis();
        getLogger().info("Loaded " + DecorationManager.Companion.getInstance().getDecorationElements().size() + " decoration elements. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        TradeManager.Companion.getInstance().loadParams(this);
        time = System.currentTimeMillis();
        getLogger().info("Loaded " + TradeManager.Companion.getInstance().getTradeSets().size() + " custom trades. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        MailbookManager.INSTANCE.loadParams(this);
        time = System.currentTimeMillis();
        if (!MailbookManager.INSTANCE.getUnreadMailbooks().isEmpty())
            getLogger().info("Loaded " + MailbookManager.INSTANCE.getUnreadMailbooks().size() + " players with mailbooks. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        CalendarEventsManager.Companion.getInstance().loadTimedEvents(this);
        time = System.currentTimeMillis();
        if (!CalendarEventsManager.Companion.getInstance().getEvents().isEmpty())
            getLogger().info("Loaded " + CalendarEventsManager.Companion.getInstance().getEvents().size() + " calendar events. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        CauldronRecipesManager.INSTANCE.loadRecipes(this);
        time = System.currentTimeMillis();
        if (!CauldronRecipesManager.INSTANCE.getRecipes().isEmpty())
            getLogger().info("Loaded " + CauldronRecipesManager.INSTANCE.getRecipes().size() + " cauldron recipes. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();

        AsureLib.inst().getCustomGUIManager().unregisterGuis(this);
        AsureLib.inst().getCustomGUIManager().registerGuis(this);

        oreManager.updateData(this);
        oreManager.loadData(this);
        ShulkerAlertManager.INSTANCE.loadData(this);
    }

    public void saveData() {
        oreManager.updateData(this);
        AsureUtils.sendDebugMessage(0,"Saving pending account transfers...");
        configManager.savePendingTransfers();
        AsureUtils.sendDebugMessage(0,"Saved "+configManager.getPendingAccountTransfers().size()+" pending transfer data.");
        AsureUtils.sendDebugMessage(0,"Saving community suffixes data...");
        suffixManager.saveCommunitySuffixes();
        AsureUtils.sendDebugMessage(0,"Saved "+suffixManager.getCommunitySuffixes().size()+" community suffixes.");

        if (!MailbookManager.INSTANCE.getUnreadMailbooks().isEmpty()) {
            AsureUtils.sendDebugMessage(0, "Saving mailbooks data...");
            MailbookManager.INSTANCE.saveMailbooks(this);
            AsureUtils.sendDebugMessage(0, "Saved " + MailbookManager.INSTANCE.getUnreadMailbooks().size() + " players with mailbooks.");
        }
    }
}
