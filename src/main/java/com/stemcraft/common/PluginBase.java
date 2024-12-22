package com.stemcraft.common;

import com.stemcraft.STEMCraftLib;
import com.stemcraft.variable.VariableManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public abstract class PluginBase extends JavaPlugin {
    /**
     * Logs a message to the server console
     *
     * @param level of the message
     * @param message to log
     */
    public void log(Level level, String message) {
        this.getLogger().log(level, message);
    }

    /**
     * Gets the version of this Plugin
     *
     * @return Plugin version
     */
    public final String getVersion() {
        return this.getDescription().getVersion();
    }
}
