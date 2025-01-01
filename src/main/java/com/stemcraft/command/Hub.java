package com.stemcraft.command;

import com.stemcraft.STEMCraftCommand;
import com.stemcraft.util.SCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Hub extends STEMCraftCommand {
    @Override
    public void execute(CommandSender sender, String command, List<String> args) {
        if (!sender.hasPermission("stemcraft.hub")) {
            message(sender, "You do not have permission to use this command");
            return;
        }

        org.bukkit.World world = Bukkit.getWorlds().getFirst();
        if(world == null) {
            error(sender, "A server error occurred getting information about the world {name}", "name", "hub");
            return;
        }

        Location spawn = world.getSpawnLocation();
        SCPlayer.teleport((Player)sender, spawn);
    }
}
