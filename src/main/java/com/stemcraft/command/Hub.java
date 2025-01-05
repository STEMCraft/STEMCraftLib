package com.stemcraft.command;

import com.stemcraft.STEMCraftCommand;
import com.stemcraft.util.SCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Hub extends STEMCraftCommand {
    @Override
    public void execute(CommandSender sender, String command, List<String> args) {
        Player targetPlayer = null;

        if (!sender.hasPermission("stemcraft.hub")) {
            message(sender, "You do not have permission to use this command");
            return;
        }

        if (sender instanceof Player player) {
            targetPlayer = player;
        } else {
            if (args.isEmpty()) {
                message(sender, "A player is required when using this command from the console.");
                return;
            }
        }

        if(!args.isEmpty()) {
            if (!sender.hasPermission("stemcraft.hub.other")) {
                message(sender, "You do not have permission to use this command");
                return;
            }

            targetPlayer = Bukkit.getPlayer(args.getFirst());
            if (targetPlayer == null) {
                error(sender, "Player {name} not found.", "name", args.getFirst());
                return;
            }
        }

        World world = Bukkit.getWorlds().getFirst();
        if(world == null) {
            error(sender, "A server error occurred getting information about the world {name}", "name", "hub");
            return;
        }

        Location spawn = world.getSpawnLocation();
        SCPlayer.teleport(targetPlayer, spawn);

        if(targetPlayer != sender) {
            message(sender, "Teleported {name} to the hub.", "name", targetPlayer.getName());
        }

        message(targetPlayer, "You have been teleported to the hub.");
    }
}
