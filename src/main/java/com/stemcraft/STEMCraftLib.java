package com.stemcraft;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@Getter
public class STEMCraftLib extends JavaPlugin {
    private static STEMCraftLib instance;
    private final VariableManager variableManager = new VariableManager();
    private final TabCompletionManager tabCompletionManager = new TabCompletionManager();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().log(Level.INFO, "STEMCraftLib Loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean supports(String attribute) {
        String[] supportedAttributes = {
            "plugin_base",
            "register_event",
            "register_command",
            "variables",
            "tab_completion",
            "message",
            "log",
        };

        for (String attr : supportedAttributes) {
            if (attr.equalsIgnoreCase(attribute)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Are worlds part of the same realm?
     *
     * @param worldA World A to test
     * @param worldB World B to test
     * @return If both world names are in the same realm
     */
    public boolean worldsInSameRealm(World worldA, World worldB) {
        String worldAName = worldA.getName().toLowerCase();
        String worldBName = worldB.getName().toLowerCase();

        if (worldAName.equals(worldBName)) {
            return true;
        }

        return worldAName.replace("_nether", "").replace("_the_end", "").equals(
                worldBName.replace("_nether", "").replace("_the_end", "")
        );
    }

}
