package com.stemcraft;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.util.*;
import java.util.function.Supplier;

@Getter
public class TabCompletionManager {
    private final Map<String, Supplier<List<String>>> completions = new HashMap<>();

    public TabCompletionManager() {
        register("player", () -> Bukkit.getServer().getOnlinePlayers().stream()
            .map(Player::getName)
            .toList()
        );

        register("world", () -> Bukkit.getServer().getWorlds().stream()
            .map(World::getName)
            .toList()
        );
    }

    /**
     * Registers a new completions method.
     *
     * @param name   The name of the variable (without braces).
     * @param method The method to handle the variable replacement.
     */
    public void register(String name, Supplier<List<String>> method) {
        completions.put(name.toLowerCase(), method);
    }

    /**
     * Checks if a completions with the given name is registered.
     *
     * @param name The name of the completion to check.
     * @return true if the completion is registered, false otherwise.
     */
    public boolean exists(String name) {
        return completions.containsKey(name.toLowerCase());
    }

    /**
     * Removes a registered completions method.
     *
     * @param name The name of the completion to remove.
     */
    public void unregister(String name) {
        completions.remove(name.toLowerCase());
    }

    public Set<String> keys() {
        return completions.keySet();
    }

    public List<String> list(String key) {
        if(completions.containsKey(key)) {
            return completions.get(key).get();
        }

        return new ArrayList<>();
    }
}
