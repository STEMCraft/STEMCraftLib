package com.stemcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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

    /**
     * Create a players head item stack based on a player.
     *
     * @param player The player to base the head on.
     * @return The item stack containing the players head.
     */
    public ItemStack getHead(Player player) {
        if(player == null) {
            return null;
        }

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        playerHead.setItemMeta(skullMeta);

        return playerHead;
    }
}
