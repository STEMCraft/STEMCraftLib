package com.stemcraft.util;

import java.util.*;
import java.util.function.Supplier;

public class SCTabCompletion {
    private static final Map<String, Supplier<List<String>>> completions = new HashMap<>();

    /**
     * Registers a new completions method.
     *
     * @param name   The name of the variable (without braces).
     * @param method The method to handle the variable replacement.
     */
    public static void register(String name, Supplier<List<String>> method) {
        completions.put(name.toLowerCase(), method);
    }

    
    /**
     * Registers a new completions method.
     *
     * @param name   The name of the variable (without braces).
     * @param args   The list of strings to display.
     */
    public static void register(String name, List<String> args) {
        register(name, () -> args);
    }

    /**
     * Registers a new completions method.
     *
     * @param name   The name of the variable (without braces).
     * @param args   The list of strings to display.
     */
    public static void register(String name, String... args) {
        register(name, () -> Arrays.asList(args));
    }

    /**
     * Checks if a completions with the given name is registered.
     *
     * @param name The name of the completion to check.
     * @return true if the completion is registered, false otherwise.
     */
    public static boolean exists(String name) {
        return completions.containsKey(name.toLowerCase());
    }

    /**
     * Removes a registered completions method.
     *
     * @param name The name of the completion to remove.
     */
    public static void unregister(String name) {
        completions.remove(name.toLowerCase());
    }

    public static Set<String> keys() {
        return completions.keySet();
    }

    public static List<String> list(String key) {
        if(completions.containsKey(key)) {
            return completions.get(key).get();
        }

        return new ArrayList<>();
    }
}
