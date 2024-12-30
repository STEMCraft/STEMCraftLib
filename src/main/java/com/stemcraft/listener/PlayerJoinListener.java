package com.stemcraft.listener;

import com.stemcraft.util.SCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location spawn = Objects.requireNonNull(Bukkit.getServer().getWorld("world")).getSpawnLocation();

        SCPlayer.teleport(player, spawn);
    }
}


