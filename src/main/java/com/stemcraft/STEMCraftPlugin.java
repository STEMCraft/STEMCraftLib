package com.stemcraft;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * STEMCraft Plugin Base
 */
public class STEMCraftPlugin extends JavaPlugin {
    private static STEMCraftPlugin instance;
    private static STEMCraftLib lib;

    @Override
    public void onEnable() {
        instance = this;
        lib = (STEMCraftLib)Bukkit.getServer().getPluginManager().getPlugin("STEMCraftLib");
    }

    /**
     * Get the instance of the STEMCraftLib
     *
     * @return The instance of the STEMCraftLib
     */
    public STEMCraftLib getSTEMCraftLib() {
        return lib;
    }

    /**
     * Does the STEMCraftLib support an attribute
     *
     * @param attribute The attribute to test
     * @return If the attribute is supported
     */
    public boolean supports(String attribute) {
        return STEMCraftLib.supports(attribute);
    }

    /**
     * Write a message to the server log
     * @param level The message level
     * @param string The message string
     * @param throwable The exception to log
     */
    public void log(Level level, String string, Throwable throwable) {
        getLogger().log(level, string, throwable);
    }

    public void log(Level level, String string) {
        getLogger().log(level, string);
    }

    public void log(String string) {
        getLogger().log(Level.INFO, string);
    }

    /**
     * Register events located in the listener class
     *
     * @param listener The class that contains the events
     */
    public void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    /**
     * Register a command on the server
     *
     * @param executor The class containing the command methods
     * @param command The optional command name. If not used, the command will be the lowercase class name of executor
     * @param aliases The optional aliases for the command
     * @param tabCompletions The tab completions for the command
     */
    public void registerCommand(STEMCraftCommand executor, String command, List<String> aliases, List<String[]> tabCompletions) {
        CommandMap commandMap = getCommandMap();
        PluginCommand pluginCommand = null;

        if(commandMap == null) {
            log(Level.SEVERE, "Could not get the server command map");
            return;
        }

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            pluginCommand = c.newInstance(command, this);
        } catch (Exception e) {
            log(Level.SEVERE, "An error occurred registering the command '" + command + "'", e);
        }

        if(pluginCommand != null) {
            if (aliases != null && !aliases.isEmpty()) {
                pluginCommand.setAliases(aliases);
            }

            if(tabCompletions != null && !tabCompletions.isEmpty()) {
                tabCompletions.forEach(executor::addTabCompletion);
                pluginCommand.setTabCompleter(executor);
            }

            pluginCommand.setExecutor(executor);
            commandMap.register(command, "stemcraft", pluginCommand);
        } else {
            log(Level.SEVERE, "Could not create a new instance of the command '" + command + "'");
        }
    }

    public void registerCommand(STEMCraftCommand executor, String command, String aliases, String tabCompletions) {
        List<String> aliasList;
        List<String[]> tabCompletionsList;

        if(aliases != null) {
            aliasList = Arrays.asList(aliases.split(" "));
        } else {
            aliasList = new ArrayList<>();
        }

        if(tabCompletions != null) {
            tabCompletionsList = Collections.singletonList(tabCompletions.split(" "));
        } else {
            tabCompletionsList = new ArrayList<>();
        }

        registerCommand(executor, command, aliasList, tabCompletionsList);
    }

    public void registerCommand(STEMCraftCommand executor, String command) {
        registerCommand(executor, command, (List<String>)null, null);
    }

    public void registerCommand(STEMCraftCommand executor) {
        String command = executor.getClass().getSimpleName().toLowerCase();
        registerCommand(executor, command, (List<String>) null, null);
    }

    /**
     * Return the command map structure for the server
     *
     * @return The command map for the server
     */
    private static CommandMap getCommandMap() {
        try {
            Server server = instance.getServer();
            final Field bukkitCommandMap = server.getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            return (CommandMap) bukkitCommandMap.get(server);
        } catch (Exception e) {
            instance.getLogger().log(Level.SEVERE, "An error occurred getting the command map of the server", e);
        }

        return null;
    }
}
