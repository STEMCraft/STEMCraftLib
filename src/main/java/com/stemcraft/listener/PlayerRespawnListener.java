package com.stemcraft.listener;

import com.stemcraft.util.SCWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if(!SCWorld.bedRespawn(world)) {
            event.setRespawnLocation(world.getSpawnLocation());
        }
    }
}
