package com.stemcraft;

import com.stemcraft.listener.PlayerDropItemListener;
import com.stemcraft.util.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class STEMCraftLib extends JavaPlugin {
    @Getter
    private static STEMCraftLib instance;

    private static String messagePrefix = "";
    private static String infoPrefix = "";
    private static String warningPrefix = "";
    private static String errorPrefix = "";
    private static String successPrefix = "";

    public static final int DEFAULT_WIDTH = 6; // Default width for unknown characters
    public static Set<Character> WIDTH_2 = new HashSet<>(Set.of('i', '!', ';', ':', '\'', ',', '.', '|')); // 2 px
    public static Set<Character> WIDTH_3 = new HashSet<>(Set.of('l', '`'));       // 3 px
    public static Set<Character> WIDTH_4 = new HashSet<>(Set.of('t', '*', '(', ')', '[', ']', '{', '}', '"', 'I', ' '));      // 4 px
    public static Set<Character> WIDTH_5 = new HashSet<>(Set.of('f', 'k', '<', '>'));                 // 5 px
    public static Set<Character> WIDTH_6 = new HashSet<>(Set.of('a', 'b', 'c', 'd', 'e', 'g', 'h', 'j',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 'u',
            'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', '#', '$', '%', '^', '&',
            '-', '_', '=', '+', '/', '?', '\\')); // 6 px
    public static Set<Character> WIDTH_7 = new HashSet<>(Set.of('~', '@')); // 7 px


    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        File configFile = new File(instance.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            // Load prefixes
            messagePrefix = config.getString("prefix.message", "");
            infoPrefix = config.getString("prefix.info", "");
            warningPrefix = config.getString("prefix.warning", "");
            errorPrefix = config.getString("prefix.error", "");
            successPrefix = config.getString("prefix.success", "");

            // Load character pixel widths
            WIDTH_2 = loadWidthSet(config, "widths.2", WIDTH_2);
            WIDTH_3 = loadWidthSet(config, "widths.3", WIDTH_3);
            WIDTH_4 = loadWidthSet(config, "widths.4", WIDTH_4);
            WIDTH_5 = loadWidthSet(config, "widths.5", WIDTH_5);
            WIDTH_6 = loadWidthSet(config, "widths.6", WIDTH_6);

            // Load worlds
            ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
            if (worldsSection != null) {
                for (String worldName : worldsSection.getKeys(false)) {
                    if(SCWorld.exists(worldName)) {
                        boolean load = worldsSection.getBoolean(worldName + ".load", false);
                        if (load) {
                            SCWorld.load(worldName);
                            log("Loaded world {name}", "name", worldName);
                        }
                    } else {
                        warning("Configuration contains the world {name} however it does not exist", "name", worldName);
                    }
                }
            }
        }

        extractFile("prices.yml");
        SCItem.loadPricesFromConfig(new File(instance.getDataFolder(), "prices.yml"));

        SCHologram.loadFromConfig(new File(instance.getDataFolder(), "holograms.yml"));

        SCTabCompletion.register("player", () -> Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .toList()
        );

        SCTabCompletion.register("world", () -> Bukkit.getServer().getWorlds().stream()
                .map(World::getName)
                .toList()
        );

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
     * @param e The throwable object
     */
    public static void log(Level level, String message, Throwable e) {
        instance.getLogger().log(level, message, e);
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

    /**
     * Generate the stack trace of the throwable
     * @param t The throwable
     * @return The stack trace
     */
    public static List<String> getStackTrace(Throwable t) {
        List<String> lines = new ArrayList<>();

        do {
            // Write the error header
            if(t == null) {
                lines.add("Unknown error");
                break;
            } else {
                String v = t.getMessage();
                if(v == null || v.isBlank()) {
                    v = t.getLocalizedMessage();
                    if(v == null || v.isBlank()) {
                        v = "(Unknown cause)";
                    }
                }

                lines.add(t.getClass().getSimpleName() + " " + v);
            }

            int count = 0;

            for (final StackTraceElement el : t.getStackTrace()) {
                count++;

                final String trace = el.toString();

                if (trace.contains("sun.reflect"))
                    continue;

                if (count > 6 && trace.startsWith("net.minecraft.server"))
                    break;

                lines.add("\t at " + el);
            }
        } while ((t = t.getCause()) != null);

        return lines;
    }

    /**
     * Print the stack trace of the throwable
     * @param t The throwable
     */
    public static void printStackTrace(Throwable t) {

    }


        private static Set<Character> loadWidthSet(YamlConfiguration config, String path, Set<Character> defaultSet) {
        Set<Character> result = new HashSet<>();
        String values = config.getString(path, ""); // Default to empty string
        if (!values.isEmpty()) {
            for (char c : values.toCharArray()) {
                result.add(c);
            }
        } else {
            result = defaultSet; // Use defaults if config is missing
        }
        return result;
    }

    public static void extractFile(String fileName) {
        extractFile("", fileName);
    }

    public static void extractFile(String path, String fileName) {
        // Ensure the plugin's data folder exists
        File dataFolder = instance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // Create data folder if it doesn't exist
        }

        // Resolve paths
        String resourcePath = (path.isEmpty() ? "" : path + "/") + fileName; // Resource path inside the JAR
        File targetFile = new File(dataFolder, (path.isEmpty() ? "" : path + "/") + fileName); // Path in data folder

        // Create parent directories for target file
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // Create subdirectories if needed
        }

        // Check if the target file already exists
        if (!targetFile.exists()) {
            // Attempt to load the resource from the jar
            try (InputStream input = instance.getResource(resourcePath)) { // Load from resources
                if (input == null) {
                    log(Level.WARNING, "Could not extract '" + resourcePath + "' as it was not found in resources.");
                    return; // File doesn't exist in resources
                }

                // Copy the resource file into the data folder
                Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log(Level.INFO, "Extracted '" + resourcePath + "' to '" + targetFile.getPath() + "'.");
            } catch (IOException e) {
                log(Level.SEVERE, "An error occurred extracting '" + resourcePath + "' to '" + targetFile.getPath() + "'.", e);
            }
        }
    }
}
