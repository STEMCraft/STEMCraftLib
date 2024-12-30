package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class SCString {
    /**
     * Parse a string replacing placeholders with associated values
     * @param message The message to parse
     * @param args The placeholders in a key/value pair
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
     * @param text The text to calculate
     * @return The text length
     */
    public static int componentLength(Component text) {
        String t = LegacyComponentSerializer.legacySection().serialize(text);
        return t.replaceAll("§[0-9a-fk-or]", "").length();
    }

    /**
     * Calculate the pixel width of a string based on the default minecraft font
     * @param text The text to calculate
     * @return The pixel width
     */
    public static int calculatePixelWidth(Component text) {
        return calculatePixelWidth(LegacyComponentSerializer.legacySection().serialize(text));
    }

    /**
     * Calculate the pixel width of a string based on the default minecraft font
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
}
