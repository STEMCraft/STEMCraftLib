package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.util.Arrays;

public class SCString {
    /**
     * Parse a string replacing placeholders with associated values
     *
     * @param message The message to parse
     * @param args    The placeholders in a key/value pair
     * @return The parsed message
     */
    public static String placeholders(String message, String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Args must be in pairs of placeholder and value");
        }

        for (int i = 0; i < args.length; i += 2) {
            String placeholder = "{" + args[i] + "}";
            String value = args[i + 1];
            message = message.replace(placeholder, value);
        }

        return message;
    }

    /**
     * Helper method to return the text length of a component
     *
     * @param text The text to calculate
     * @return The text length
     */
    public static int componentLength(Component text) {
        String t = LegacyComponentSerializer.legacySection().serialize(text);
        return t.replaceAll("§[0-9a-fk-or]", "").length();
    }

    /**
     * Calculate the pixel width of a string based on the default minecraft font
     *
     * @param text The text to calculate
     * @return The pixel width
     */
    public static int calculatePixelWidth(Component text) {
        return calculatePixelWidth(LegacyComponentSerializer.legacySection().serialize(text));
    }

    /**
     * Calculate the pixel width of a string based on the default minecraft font
     *
     * @param text The text to calculate
     * @return The pixel width
     */
    public static int calculatePixelWidth(String text) {
        // Remove Minecraft color and formatting codes (e.g., §b, §l, etc.)
        text = text.replaceAll("§[0-9a-fk-or]", "");

        int width = 0;
        for (char c : text.toCharArray()) {
            if (STEMCraftLib.WIDTH_2.contains(c)) width += 2;
            else if (STEMCraftLib.WIDTH_3.contains(c)) width += 3;
            else if (STEMCraftLib.WIDTH_4.contains(c)) width += 4;
            else if (STEMCraftLib.WIDTH_5.contains(c)) width += 5;
            else if (STEMCraftLib.WIDTH_6.contains(c)) width += 6;
            else if (STEMCraftLib.WIDTH_7.contains(c)) width += 7;
            else width += STEMCraftLib.DEFAULT_WIDTH; // Fallback for unsupported characters
        }
        return width;
    }

    /**
     * Parse a string to a location
     *
     * @param s     The string to parse
     * @param world Optional world to use if no world found
     * @return A location
     */
    public static Location stringToLocation(String s, World world) {
        String[] data = s.split(",");

        // Check if the first item is a valid world name
        World parsedWorld = Bukkit.getWorld(data[0]);
        if (parsedWorld != null) {
            world = parsedWorld;
            data = Arrays.copyOfRange(data, 1, data.length); // Remove world name from data
        }

        // Use default world if no world provided
        if (world == null) {
            world = Bukkit.getWorlds().getFirst(); // Default to the first server world
        }

        // Parse coordinates
        double x = Double.parseDouble(data[0]);
        double y = Double.parseDouble(data[1]);
        double z = Double.parseDouble(data[2]);

        // Parse optional yaw and pitch
        float yaw = (data.length > 3) ? Float.parseFloat(data[3]) : 0.0f;
        float pitch = (data.length > 4) ? Float.parseFloat(data[4]) : 0.0f;

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Parse a string to a location
     *
     * @param s The string to parse
     * @return A location
     */
    public static Location stringToLocation(String s) {
        return stringToLocation(s, null);
    }

    /**
     * Convert a location to a formatted string
     * @param loc The location to convert
     * @param includeWorld Include the world name in the string
     * @param includeYawPitch Include the yaw and pitch in the string
     * @return The converted string
     */
    public static String locationToString(Location loc, boolean includeWorld, boolean includeYawPitch) {
        StringBuilder sb = new StringBuilder();

        if (includeWorld) {
            sb.append(loc.getWorld().getName()).append(",");
        }

        sb.append(String.format("%.2f,%.2f,%.2f", loc.getX(), loc.getY(), loc.getZ()));

        if (includeYawPitch) {
            sb.append(String.format(",%.2f,%.2f", loc.getYaw(), loc.getPitch()));
        }

        return sb.toString();
    }
}
