package com.stemcraft.variable;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.BiFunction;

public class VariableManager {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final Map<String, BiFunction<Player, String, String>> variables = new HashMap<>();

    static {
        // Initialize default global variables
        register("player", (player, variable) -> player.getName());
        register("world", (player, variable) -> player.getWorld().getName());
    }

    /**
     * Registers a new variable method.
     *
     * @param name   The name of the variable (without braces).
     * @param method The method to handle the variable replacement.
     */
    public static void register(String name, BiFunction<Player, String, String> method) {
        variables.put(name.toLowerCase(), method);
    }

    /**
     * Replaces all variables in the given text with their corresponding values.
     *
     * @param player The player for whom to process the variables.
     * @param text   The text containing variables to replace.
     * @return The text with all variables replaced.
     */
    public static String replace(Player player, String text) {
        String result = text;
        boolean containsVariables;

        do {
            containsVariables = false;
            Matcher matcher = VARIABLE_PATTERN.matcher(result);
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                containsVariables = true;
                String variableName = matcher.group(1).toLowerCase();
                BiFunction<Player, String, String> method = variables.get(variableName);

                if (method != null) {
                    String replacement = method.apply(player, variableName);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                } else {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        } while (containsVariables);

        return result;
    }

    /**
     * Checks if a variable with the given name is registered.
     *
     * @param name The name of the variable to check.
     * @return true if the variable is registered, false otherwise.
     */
    public static boolean exists(String name) {
        return variables.containsKey(name.toLowerCase());
    }

    /**
     * Removes a registered variable method.
     *
     * @param name The name of the variable to remove.
     */
    public static void unregister(String name) {
        variables.remove(name.toLowerCase());
    }
}
