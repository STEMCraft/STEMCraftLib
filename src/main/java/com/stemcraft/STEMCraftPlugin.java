package com.stemcraft;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

/**
 * STEMCraft Plugin Base
 */
public class STEMCraftPlugin extends JavaPlugin {
    @SuppressWarnings("FieldCanBeLocal")
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
        lib.registerCommand(executor, command, aliases, tabCompletions);
    }

    public void registerCommand(STEMCraftCommand executor, String command, String aliases, String tabCompletions) {
        lib.registerCommand(executor, command, aliases, tabCompletions);
    }

    public void registerCommand(STEMCraftCommand executor, String command) {
        lib.registerCommand(executor, command);
    }

    public void registerCommand(STEMCraftCommand executor) {
        lib.registerCommand(executor);
    }

    /**
     * Write a debug message to the console and operator players.
     * @param message The debug message
     */
    public void debug(String message) {
        STEMCraftLib.debug(message);
    }
}
