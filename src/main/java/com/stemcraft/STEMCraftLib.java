package com.stemcraft;

import com.stemcraft.variable.VariableManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class STEMCraftLib extends JavaPlugin {
    private static STEMCraftLib instance;
    private final VariableManager variableManager = new VariableManager();

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Gets the instance of the plugin.
     * @return The plugin instance.
     */
    public static STEMCraftLib getInstance() {
        return instance;
    }

    /**
     * Gets the VariableManager instance.
     * @return The VariableManager instance.
     */
    public VariableManager getVariableManager() {
        return variableManager;
    }
}
