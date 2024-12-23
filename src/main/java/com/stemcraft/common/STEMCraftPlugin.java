package com.stemcraft.common;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class STEMCraftPlugin extends JavaPlugin {

    public void log(Level level, String string) {
        getLogger().log(level, string);
    }

    public void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void registerCommand(CommandExecutor executor, String command) {
        Objects.requireNonNull(getCommand(command)).setExecutor(executor);
    }
}
