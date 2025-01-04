package com.stemcraft.listener;

import com.stemcraft.util.SCWorld;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        GameMode gameMode = SCWorld.gameMode(player.getWorld(), null);
        if (gameMode != null) {
            player.setGameMode(gameMode);
        }
    }
}
