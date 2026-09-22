package org.saintqd.asurecore.listeners;

import litebans.api.Entry;
import litebans.api.Events;
import org.bukkit.Bukkit;
import org.saintqd.asurecore.AsureCore;

import java.util.List;

public class LiteBansListener extends Events.Listener {

    public static void registerEvents() {
        Events.get().register(new LiteBansListener());
    }

    @Override
    public void entryAdded(Entry entry) {
        if (!AsureCore.inst().getConfig().getBoolean("Compatibility.LiteBans.Enabled")) return;
        List<String> commands;
        switch (entry.getType()) {
            case "ban" -> {
                if (entry.getDuration() > 0) {
                    commands = AsureCore.inst().getConfig().getStringList("Compatibility.LiteBans.CommandsOnTempBan");
                }
                else
                    commands = AsureCore.inst().getConfig().getStringList("Compatibility.LiteBans.CommandsOnBan");
                executeEntryCommands(entry,commands);
            }
            case "mute" -> {
                if (entry.getDuration() > 0) {
                    commands = AsureCore.inst().getConfig().getStringList("Compatibility.LiteBans.CommandsOnTempMute");
                }
                else
                    commands = AsureCore.inst().getConfig().getStringList("Compatibility.LiteBans.CommandsOnMute");
                executeEntryCommands(entry,commands);
            }
        }
    }

    @Override
    public void entryRemoved(Entry entry) {
        if (!AsureCore.inst().getConfig().getBoolean("Compatibility.LiteBans.Enabled")) return;
        List<String> commands;
        switch (entry.getType()) {
            case "ban" -> {
                commands = AsureCore.inst().getConfig().getStringList("Compatibility.LiteBans.CommandsOnUnban");
                executeEntryCommands(entry,commands);
            }
            case "mute" -> {
                commands = AsureCore.inst().getConfig().getStringList("Compatibility.LiteBans.CommandsOnUnmute");
                executeEntryCommands(entry,commands);
            }
        }
    }

    private void executeEntryCommands(Entry entry, List<String> commands) {
        Bukkit.getScheduler().runTask(AsureCore.inst(), () -> {
            for (String command : commands) {
                String parsedCommand = command.replace("%player_name%",entry.getUuid()).replace("%time%",Long.toString(entry.getDuration()));
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),parsedCommand);
            }
        });
    }
}
