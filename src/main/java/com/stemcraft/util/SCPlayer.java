package com.stemcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.geyser.api.GeyserApi;

public class SCPlayer {
    private static Boolean isGeyserInstalled = null;
    private static GeyserApi geyserApi = null;

    /**
     * Test if a player is a BedRock player
     *
     * @param player The player to test.
     * @return If the player is a geyser
     */
    public static boolean isBedrock(Player player) {
        if(isGeyserInstalled == null) {
            if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
                isGeyserInstalled = true;
                geyserApi = GeyserApi.api();
            }

            return false;
        }

        if(!isGeyserInstalled) {
            return false;
        }

        return geyserApi.isBedrockPlayer(player.getUniqueId());
    }
}
