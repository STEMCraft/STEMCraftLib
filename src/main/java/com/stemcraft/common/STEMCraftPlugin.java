package com.stemcraft.common;

import com.stemcraft.STEMCraftLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
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
        return lib.supports(attribute);
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
     * Display a message to the console or player
     * @param sender The player to send the message to
     * @param message The message string. Supports placeholders
     * @param args The placeholders to replace in the message in a "find", "replace" format
     */
    public void message(CommandSender sender, String message, String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Args must be in pairs of placeholder and value");
        }

        for (int i = 0; i < args.length; i += 2) {
            String placeholder = "{" + args[i] + "}";
            String value = args[i + 1];
            message = message.replace(placeholder, value);
        }

        String miniMessageString = LegacyComponentSerializer.legacyAmpersand().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(message)
        );

        Component component = MiniMessage.miniMessage().deserialize(miniMessageString);
        sender.sendMessage(component);
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
     */
    public void registerCommand(STEMCraftCommand executor, String command) {
        CommandMap commandMap = getCommandMap();
        PluginCommand pluginCommand = null;
        executor.setSTEMCraftPlugin(instance);

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
            if (!executor.getAliasList().isEmpty()) {
                pluginCommand.setAliases(executor.getAliasList());
            }

            pluginCommand.setTabCompleter(executor);
            pluginCommand.setExecutor(executor);
            commandMap.register(command, "stemcraft", pluginCommand);
        } else {
            log(Level.SEVERE, "Could not create a new instance of the command '" + command + "'");
        }
    }

    public void registerCommand(STEMCraftCommand executor) {
        String command = executor.getClass().getSimpleName().toLowerCase();
        registerCommand(executor, command);
    }

    /**
     * Register a new tab completion key for commands
     * @param key The tab completion key
     * @param args An array of valid values for the key
     */
    public void registerTabCompletion(String key, String ...args) {
        lib.getTabCompletionManager().register(key, () -> {
            return Arrays.asList(args);
        });
    }

    /**
     * Register a new tab completion key for commands
     * @param key The tab completion key
     * @param method A supplier that returns valid values for the key
     */
    public void registerTabCompletion(String key, Supplier<List<String>> method) {
        lib.getTabCompletionManager().register(key, method);
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
