package com.stemcraft.command;

import com.stemcraft.STEMCraftCommand;
import com.stemcraft.util.SCHologram;
import com.stemcraft.util.SCPlayer;
import com.stemcraft.util.SCWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class World extends STEMCraftCommand {
    @Override
    public void execute(CommandSender sender, String command, List<String> args) {
        if (!sender.hasPermission("stemcraft.world")) {
            message(sender, "You do not have permission to use this command");
            return;
        }

        switch (args.getFirst()) {
            case "spawn":
                executeSpawn((Player)sender, args);
                break;
            default:
                messageUsage(sender);
                break;
        }
    }

    public void messageUsage(CommandSender sender) {
        String usage = "Usage: /oneblock <join|leave|delete|hologram <create|delete>>";
        message(sender, usage);
    }

    public void executeSpawn(Player player, List<String> args) {
        if(args.size() < 2) {
            message(player, "Usage: /world teleport <name>");
            return;
        }

        String worldName = args.get(1);
        if(!SCWorld.exists(worldName)) {
            error(player, "The world {name} does not exist", "name", worldName);
            return;
        }

        if(!SCWorld.isLoaded(worldName)) {
            error(player, "The world {name} is not loaded", "name", worldName);
            return;
        }

        org.bukkit.World world = Bukkit.getWorld(worldName);
        if(world == null) {
            error(player, "A server error occurred getting information about the world {name}", "name", worldName);
            return;
        }

        Location spawn = world.getSpawnLocation();
        SCPlayer.teleport(player, spawn);
    }
}
