package com.stemcraft.listener;

import com.stemcraft.STEMCraftLib;
import com.stemcraft.util.SCWorld;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World targetWorld = event.getTo().getWorld();

        if (SCWorld.isUnloading(targetWorld)) {
            event.setCancelled(true); // Cancel teleport
            STEMCraftLib.error(event.getPlayer(), "You cannot teleport to this world right now as it is unloading.");
        }
    }
}
