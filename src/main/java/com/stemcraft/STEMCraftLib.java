package com.stemcraft;

import com.stemcraft.listener.PlayerDropItemListener;
import com.stemcraft.util.SCString;
import com.stemcraft.util.SCTabCompletion;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class STEMCraftLib extends JavaPlugin {
    @Getter
    private static STEMCraftLib instance;

    private static String messagePrefix = "";
    private static String infoPrefix = "";
    private static String warningPrefix = "";
    private static String errorPrefix = "";
    private static String successPrefix = "";

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        SCTabCompletion.register("player", () -> Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .toList()
        );

        SCTabCompletion.register("world", () -> Bukkit.getServer().getWorlds().stream()
                .map(World::getName)
                .toList()
        );

        File configFile = new File(instance.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            messagePrefix = config.getString("prefix.message", "");
            infoPrefix = config.getString("prefix.info", "");
            warningPrefix = config.getString("prefix.warning", "");
            errorPrefix = config.getString("prefix.error", "");
            successPrefix = config.getString("prefix.success", "");
        }


        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);

        getLogger().log(Level.INFO, "STEMCraftLib Loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static boolean supports(String attribute) {
        String[] supportedAttributes = {
            "plugin_base",
            "register_event",
            "register_command",
            "tab_completion",
            "message",
            "log",
            "bedrock",
            "item_attrib",
            "player_heads"
        };

        for (String attr : supportedAttributes) {
            if (attr.equalsIgnoreCase(attribute)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Write a message to the server log
     * @param level The log level
     * @param message The message to write
     */
    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    /**
     * Write a message to the server log
     * @param level The log level
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void log(Level level, String message, String... args) {
        log(level, SCString.placeholders(message, args));
    }

    /**
     * Write a message to the server log
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void log(String message, String... args) {
        log(Level.INFO, SCString.placeholders(message, args));
    }

    /**
     * Write a message to the server log
     * @param message The message to write
     */
    public static void log(String message) {
        log(Level.INFO, message);
    }

    /**
     * Write a message to the server log
     * @param message The message to write
     */
    public static void message(String message) {
        log(Level.INFO, message);
    }

    /**
     * Write a message to the server log
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void message(String message, String... args) {
        log(Level.INFO, message, args);
    }

    /**
     * Send a message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     */
    public static void message(CommandSender sender, String message) {
        String fullMessage = ((sender instanceof Player) ? messagePrefix : "") + message;
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMessage);
        sender.sendMessage(component);
    }

    /**
     * Send a message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void message(CommandSender sender, String message, String... args) {
        message(sender, SCString.placeholders(message, args));
    }
    /**
     * Write a info message to the server log
     * @param message The message to write
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Write a info message to the server log
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void info(String message, String... args) {
        log(Level.INFO, message, args);
    }

    /**
     * Send a info message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     */
    public static void info(CommandSender sender, String message) {
        String fullMessage = ((sender instanceof Player) ? infoPrefix : "") + message;
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMessage);
        sender.sendMessage(component);
    }

    /**
     * Send a info to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void info(CommandSender sender, String message, String... args) {
        info(sender, SCString.placeholders(message, args));
    }
    /**
     * Write a warning message to the server log
     * @param message The message to write
     */
    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Write a message to the server log
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void warning(String message, String... args) {
        log(Level.WARNING, message, args);
    }

    /**
     * Send a warning message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     */
    public static void warning(CommandSender sender, String message) {
        String fullMessage = ((sender instanceof Player) ? warningPrefix : "") + message;
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMessage);
        sender.sendMessage(component);
    }

    /**
     * Send a warning message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void warning(CommandSender sender, String message, String... args) {
        warning(sender, SCString.placeholders(message, args));
    }
    /**
     * Write a error message to the server log
     * @param message The message to write
     */
    public static void error(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Write a error message to the server log
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void error(String message, String... args) {
        log(Level.SEVERE, message, args);
    }

    /**
     * Send a error message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     */
    public static void error(CommandSender sender, String message) {
        String fullMessage = ((sender instanceof Player) ? errorPrefix : "") + message;
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMessage);
        sender.sendMessage(component);
    }

    /**
     * Send a error message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void error(CommandSender sender, String message, String... args) {
        error(sender, SCString.placeholders(message, args));
    }
    /**
     * Write a success message to the server log
     * @param message The message to write
     */
    public static void success(String message) {
        log(Level.INFO, message);
    }

    /**
     * Write a success message to the server log
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void success(String message, String... args) {
        log(Level.INFO, message, args);
    }

    /**
     * Send a success message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     */
    public static void success(CommandSender sender, String message) {
        String fullMessage = ((sender instanceof Player) ? successPrefix : "") + message;
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMessage);
        sender.sendMessage(component);
    }

    /**
     * Send a success message to the server or player
     * @param sender The recipient of the message
     * @param message The message to write
     * @param args The placeholders to replace in the message
     */
    public static void success(CommandSender sender, String message, String... args) {
        success(sender, SCString.placeholders(message, args));
    }
}
