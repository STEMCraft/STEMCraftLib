package com.stemcraft.util;

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
}
