package com.stemcraft.command;

// TODO Add in permission checks

import com.stemcraft.STEMCraftCommand;
import com.stemcraft.exception.InvalidWorldGeneratorException;
import com.stemcraft.exception.MainWorldDeletionException;
import com.stemcraft.util.SCChatMenu;
import com.stemcraft.util.SCPlayer;
import com.stemcraft.util.SCWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class World extends STEMCraftCommand {
    @Override
    public void execute(CommandSender sender, String command, List<String> args) {
        if (!sender.hasPermission("stemcraft.world")) {
            message(sender, "You do not have permission to use this command.");
            return;
        }

        if (args.isEmpty()) {
            messageUsage(sender);
            return;
        }

        switch (args.getFirst().toLowerCase()) {
            case "create":
                executeCreate(sender, args);
                break;
            case "delete":
                executeDelete(sender, args);
                break;
            case "load":
                executeLoad(sender, args);
                break;
            case "unload":
                executeUnload(sender, args);
                break;
            case "list":
                executeList(sender, args);
                break;
            case "teleport":
                executeTeleport(sender, args);
                break;
            case "spawn":
                executeSpawn(sender, args);
                break;
            case "setspawn":
                executeSetSpawn(sender, args);
                break;
            case "copy":
                executeCopy(sender, args);
                break;
            case "autosave":
                executeAutosave(sender, args);
                break;
            case "save":
                executeSave(sender, args);
                break;
            case "bedrespawn":
                executeBedRespawn(sender, args);
                break;
            case "gamemode":
                executeGameMode(sender, args);
                break;
            case "listgenerators":
                executeListGenerators(sender, args);
                break;
            default:
                messageUsage(sender);
                break;
        }
    }

    public void messageUsage(CommandSender sender) {
        String usage = "Usage: /world <create|delete|load|unload|list|spawn> [args]";
        message(sender, usage);
    }

    /**
     * Create a new world with specific arguments
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeCreate(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            message(sender, "Usage: /world create <name>:[generator]:[arguments] [seed]");
            return;
        }

        String[] nameParts = args.get(1).split(":");
        String worldName = nameParts[0];
        String generator = nameParts.length > 1 ? nameParts[1] : null;
        String settings = nameParts.length > 2 ? nameParts[2] : "";

        Long seed = null;
        if (args.size() > 2) {
            try {
                seed = Long.parseLong(args.get(2));
            } catch (NumberFormatException e) {
                error(sender, "The seed must be a number.");
                return;
            }
        }

        if (SCWorld.exists(worldName)) {
            error(sender, "The world {name} already exists.", "name", worldName);
            return;
        }

        try {
            org.bukkit.World world = SCWorld.create(worldName, generator, settings, seed);
            if (world != null) {
                message(sender, "World {name} has been created successfully.", "name", worldName);
            } else {
                error(sender, "Failed to create world {name}.", "name", worldName);
            }
        } catch(InvalidWorldGeneratorException generatorException) {
            error(sender, "Failed to create world {name} as the generator {generator} is not installed", "name", worldName, "generator", generator);
        } catch (RuntimeException e) {
            error(sender, "Failed to create world {name}. " + e.getMessage(), "name", worldName);
        }
    }

    /**
     * Delete a world
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeDelete(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            message(sender, "Usage: /world delete <name>");
            return;
        }

        String worldName = args.get(1);
        if (!SCWorld.exists(worldName)) {
            error(sender, "The world {name} does not exist.", "name", worldName);
            return;
        }

        try {
            SCWorld.delete(worldName, status -> {
                if (Objects.requireNonNull(status) == SCWorld.WorldStatus.DELETED_WORLD) {
                    message(sender, "World {name} has been deleted successfully.", "name", worldName);
                } else {
                    message(sender, "World {name} is being processed.", "name", worldName);
                }
            });
        } catch(MainWorldDeletionException exception) {
            error(sender, "You cannot delete the default world of server.");
        } catch(RuntimeException exception) {
            error(sender, "Failed to delete world {name}. " + exception.getMessage(), "name", worldName);
        }
    }

    /**
     * Load a world into the server
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeLoad(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            message(sender, "Usage: /world load <name>");
            return;
        }

        String worldName = args.get(1);
        if (SCWorld.isLoaded(worldName)) {
            message(sender, "The world {name} is already loaded.", "name", worldName);
            return;
        }

        org.bukkit.World world = SCWorld.load(worldName);
        if (world != null) {
            message(sender, "World {name} has been loaded successfully.", "name", worldName);
        } else {
            error(sender, "Failed to load world {name}.", "name", worldName);
        }
    }

    public void executeUnload(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            message(sender, "Usage: /world unload <name>");
            return;
        }

        String worldName = args.get(1);
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            error(sender, "The world {name} is not loaded.", "name", worldName);
            return;
        }

        SCWorld.unload(world, false, status -> {
            if (Objects.requireNonNull(status) == SCWorld.WorldStatus.UNLOADED_WORLD) {
                message(sender, "World {name} has been unloaded successfully.", "name", worldName);
            } else {
                message(sender, "World {name} is being processed.", "name", worldName);
            }
        });
    }

    public void executeList(CommandSender sender, List<String> args) {
        String title = "Worlds";
        String command = "world list";
        int page = SCChatMenu.getPageFromArgs(args);

        Collection<String> worldList = SCWorld.list();

        SCChatMenu.render(
                sender,
                title,
                command,
                page,
                worldList.size(),
                (Integer start, Integer count) -> {
                    List<Component> list = new ArrayList<>();
                    List<String> worlds = worldList.stream().toList(); // Cache the list once

                    int end = Math.min(start + count, worldList.size()); // Ensure bounds
                    for (int i = start; i < end; i++) {
                        list.add(listItemRow(worlds.get(i), i)); // Add rows directly
                    }

                    return list;
                },
                "No worlds where found"
        );
    }


    /**
     * Teleport player to their last location in a world
     * @param sender The command
     * @param args Command arguments
     */
    public void executeTeleport(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            message(sender, "Usage: /world teleport <world> [player]");
            return;
        }

        String worldName = args.get(1);
        if (!SCWorld.exists(worldName)) {
            error(sender, "The world {name} does not exist.", "name", worldName);
            return;
        }

        if (!SCWorld.isLoaded(worldName)) {
            error(sender, "The world {name} is not loaded.", "name", worldName);
            return;
        }

        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        Player targetPlayer;

        if (sender instanceof Player player) {
            targetPlayer = player;
        } else {
            if (args.size() < 3) {
                message(sender, "Usage: /world teleport <world> <player>");
                return;
            }
            targetPlayer = Bukkit.getPlayer(args.get(2));
            if (targetPlayer == null) {
                error(sender, "Player {name} not found.", "name", args.get(2));
                return;
            }
        }

        Location spawn = SCWorld.getLastLocation(world, targetPlayer);
        SCPlayer.teleport(targetPlayer, spawn);
        message(sender, "Teleported {player} to spawn of world {name}.", "player", targetPlayer.getName(), "name", worldName);
    }

    /**
     * Teleport player to the spawn location of a world
     * @param sender The command
     * @param args Command arguments
     */
    public void executeSpawn(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            message(sender, "Usage: /world spawn <world> [player]");
            return;
        }

        String worldName = args.get(1);
        if (!SCWorld.exists(worldName)) {
            error(sender, "The world {name} does not exist.", "name", worldName);
            return;
        }

        if (!SCWorld.isLoaded(worldName)) {
            error(sender, "The world {name} is not loaded.", "name", worldName);
            return;
        }

        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        Location spawn = world.getSpawnLocation();
        Player targetPlayer;

        if (sender instanceof Player player) {
            targetPlayer = player;
        } else {
            if (args.size() < 3) {
                message(sender, "Usage: /world spawn <world> <player>");
                return;
            }
            targetPlayer = Bukkit.getPlayer(args.get(2));
            if (targetPlayer == null) {
                error(sender, "Player {name} not found.", "name", args.get(2));
                return;
            }
        }

        SCPlayer.teleport(targetPlayer, spawn);
        message(sender, "Teleported {player} to spawn of world {name}.", "player", targetPlayer.getName(), "name", worldName);
    }

    /**
     * Set the spawn location of a world
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeSetSpawn(CommandSender sender, List<String> args) {
        String worldName = null;
        Player targetPlayer = null;

        if (sender instanceof Player player) {
            targetPlayer = player;
            worldName = player.getWorld().getName();
        } else {
            if (args.size() < 2) {
                message(sender, "Usage: /world setspawn [world] [player]");
                return;
            }
        }

        if (!args.isEmpty()) {
            worldName = args.getFirst();
        }

        if (args.size() > 1) {
            targetPlayer = Bukkit.getPlayer(args.get(1));
            if (targetPlayer == null) {
                error(sender, "Player {name} not found.", "name", args.get(1));
                return;
            }
        }

        if (worldName == null) {
            message(sender, "Usage: /world setspawn (world) (player)");
            return;
        }

        if (!SCWorld.exists(worldName)) {
            error(sender, "The world {name} does not exist.", "name", worldName);
            return;
        }

        if (!SCWorld.isLoaded(worldName)) {
            error(sender, "The world {name} is not loaded.", "name", worldName);
            return;
        }

        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        Location location = targetPlayer != null ? targetPlayer.getLocation() : world.getSpawnLocation();
        world.setSpawnLocation(location);
        message(sender, "Set spawn location for world {name} to {location}.", "name", worldName, "location", location.toString());
    }

    /**
     * Duplicate a world
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeCopy(CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            message(sender, "Usage: /world duplicate <source> <target>");
            return;
        }

        String sourceWorldName = args.get(1);
        String targetWorldName = args.get(2);

        if (!SCWorld.exists(sourceWorldName)) {
            error(sender, "The source world {name} does not exist.", "name", sourceWorldName);
            return;
        }

        if (SCWorld.exists(targetWorldName)) {
            error(sender, "The target world {name} already exists.", "name", targetWorldName);
            return;
        }

        SCWorld.duplicate(sourceWorldName, targetWorldName);
        message(sender, "World {source} has been duplicated to {target}.", "source", sourceWorldName, "target", targetWorldName);
    }

    /**
     * Get/Set the autosave status of a world
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeAutosave(CommandSender sender, List<String> args) {
        // TODO Add support for the world name option to be optional
        String status = null;
        String worldName = null;
        org.bukkit.World world = null;

        if (args.size() > 2) {
            String arg = args.get(1);
            if (arg.equalsIgnoreCase("enabled") || arg.equalsIgnoreCase("disabled")) {
                status = arg;
                if (args.size() > 3) {
                    worldName = args.get(2);
                }
            } else {
                worldName = arg;
            }
        }

        if(worldName != null) {
            if(SCWorld.exists(worldName)) {
                if(SCWorld.isLoaded(worldName)) {
                    world = Bukkit.getWorld(worldName);
                } else {
                    error(sender, "The world {name} is not loaded.", "name", worldName);
                    return;
                }
            } else {
                error(sender, "The world {name} does not exist.", "name", worldName);
                return;
            }

        }

        if (sender instanceof Player player) {
            if (worldName == null) {
                world = player.getWorld();
            }
        } else {
            if (worldName == null) {
                message(sender, "A world name is required when using this command from console.");
                return;
            }
        }

        if(world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        if (status == null) {
            boolean autosave = world.isAutoSave();
            message(sender, "Autosave for world {name} is currently {status}.", "name", worldName, "status", autosave ? "enabled" : "disabled");
        } else {
            boolean autosave = status.equalsIgnoreCase("enabled");
            world.setAutoSave(autosave);
            message(sender, "Autosave for world {name} has been {status}.", "name", worldName, "status", status);
        }
    }

    /**
     * Save the world
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeSave(CommandSender sender, List<String> args) {
        String worldName = null;
        org.bukkit.World world = null;

        if (args.size() > 2) {
            worldName = args.get(2);
        }

        if(worldName != null) {
            if(SCWorld.exists(worldName)) {
                if(SCWorld.isLoaded(worldName)) {
                    world = Bukkit.getWorld(worldName);
                } else {
                    error(sender, "The world {name} is not loaded.", "name", worldName);
                    return;
                }
            } else {
                error(sender, "The world {name} does not exist.", "name", worldName);
                return;
            }

        }

        if (sender instanceof Player player) {
            if (worldName == null) {
                world = player.getWorld();
            }
        } else {
            if (worldName == null) {
                message(sender, "A world name is required when using this command from console.");
                return;
            }
        }

        if(world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        message(sender, "Saving the world {name}.", "name", worldName);
        world.save();
    }

    /**
     * Get, Set the bed respawn location of a world
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeBedRespawn(CommandSender sender, List<String> args) {
        // TODO disable bed sleeping when enabled
        String status = null;
        String worldName = null;
        org.bukkit.World world = null;

        if (args.size() > 2) {
            String arg = args.get(1);
            if (arg.equalsIgnoreCase("enabled") || arg.equalsIgnoreCase("disabled")) {
                status = arg;
                if (args.size() > 3) {
                    worldName = args.get(2);
                }
            } else {
                worldName = arg;
            }
        }

        if(worldName != null) {
            if(SCWorld.exists(worldName)) {
                if(SCWorld.isLoaded(worldName)) {
                    world = Bukkit.getWorld(worldName);
                } else {
                    error(sender, "The world {name} is not loaded.", "name", worldName);
                    return;
                }
            } else {
                error(sender, "The world {name} does not exist.", "name", worldName);
                return;
            }

        }

        if (sender instanceof Player player) {
            if (worldName == null) {
                world = player.getWorld();
            }
        } else {
            if (worldName == null) {
                message(sender, "A world name is required when using this command from console.");
                return;
            }
        }

        if(world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        if (status == null) {
            boolean bedRespawn = SCWorld.bedRespawn(world, null);
            message(sender, "Bed respawn for world {name} is currently {status}.", "name", worldName, "status", bedRespawn ? "enabled" : "disabled");
        } else {
            boolean bedRespawn = SCWorld.bedRespawn(world, status.equalsIgnoreCase("enabled"));
            message(sender, "Bed respawn for world {name} has been {status}.", "name", worldName, "status", status);
        }
    }

    /**
     * Get, Set the world game mode
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeGameMode(CommandSender sender, List<String> args) {
        // TODO show usage on bad params. Add reset param.
        String gameMode = null;
        String worldName = null;
        org.bukkit.World world = null;

        if (args.size() > 1) {
            String arg = args.get(1);
            if (arg.equalsIgnoreCase("survival") || arg.equalsIgnoreCase("creative") ||
                arg.equalsIgnoreCase("adventure") || arg.equalsIgnoreCase("spectator")) {
                gameMode = arg;
                if (args.size() > 2) {
                    worldName = args.get(2);
                }
            } else {
                worldName = arg;
            }
        }

        if (worldName != null) {
            if (SCWorld.exists(worldName)) {
                if (SCWorld.isLoaded(worldName)) {
                    world = Bukkit.getWorld(worldName);
                } else {
                    error(sender, "The world {name} is not loaded.", "name", worldName);
                    return;
                }
            } else {
                error(sender, "The world {name} does not exist.", "name", worldName);
                return;
            }
        }

        if (sender instanceof Player player) {
            if (worldName == null) {
                world = player.getWorld();
            }
        } else {
            if (worldName == null) {
                message(sender, "A world name is required when using this command from console.");
                return;
            }
        }

        if (world == null) {
            error(sender, "Failed to find world {name}.", "name", worldName);
            return;
        }

        if (gameMode == null) {
            message(sender, "Usage: /world gamemode <survival|creative|adventure|spectator> [world]");
            return;
        }

        org.bukkit.GameMode mode;
        switch (gameMode.toLowerCase()) {
            case "survival":
                mode = org.bukkit.GameMode.SURVIVAL;
                break;
            case "creative":
                mode = org.bukkit.GameMode.CREATIVE;
                break;
            case "adventure":
                mode = org.bukkit.GameMode.ADVENTURE;
                break;
            case "spectator":
                mode = org.bukkit.GameMode.SPECTATOR;
                break;
            default:
                message(sender, "Invalid game mode: {mode}", "mode", gameMode);
                return;
        }

        for (Player player : world.getPlayers()) {
            player.setGameMode(mode);
        }

        message(sender, "Game mode for world {name} has been set to {mode}.", "name", worldName, "mode", gameMode);
    }

    /**
     * List the available world generators
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    public void executeListGenerators(CommandSender sender, List<String> args) {
        ArrayList<String> generators = new ArrayList<>();
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            try {
                //noinspection deprecation
                String mainclass = plugin.getDescription().getMain();
                Class<?> cmain = plugin.getClass();
                if (!cmain.getName().equals(mainclass)) {
                    cmain = Class.forName(mainclass);
                }
                if (cmain.getMethod("getDefaultWorldGenerator", String.class, String.class).getDeclaringClass() != JavaPlugin.class) {
                    //noinspection deprecation
                    generators.add(plugin.getDescription().getName());
                }
            } catch (Throwable t) {
                // empty
            }
        }

        String title = "Generators";
        String command = "world listgenerators";
        int page = SCChatMenu.getPageFromArgs(args);

        SCChatMenu.render(
                sender,
                title,
                command,
                page,
                generators.size(),
                (Integer start, Integer count) -> {
                    List<Component> list = new ArrayList<>();

                    int end = Math.min(start + count, generators.size()); // Ensure bounds
                    for (int i = start; i < end; i++) {
                        Component component = Component.text((i + 1) + ". ", NamedTextColor.LIGHT_PURPLE)
                                .append(Component.text(generators.get(i) + " ", NamedTextColor.GOLD));

                        list.add(component);
                    }

                    return list;
                },
                "No generators where found"
        );
    }

    /**
     * Generate the list item row for the world list.
     *
     * @param worldName The world name
     * @param number The row index number
     * @return A component representing the list item row
     */
    private Component listItemRow(String worldName, int number) {
        boolean isLoaded = SCWorld.isLoaded(worldName);
        int numPlayers = 0;

        if(isLoaded) {
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                numPlayers = world.getPlayerCount();
            }
        }

        org.bukkit.World firstWorld = Bukkit.getWorlds().getFirst();
        String firstWorldName = firstWorld.getName();

        boolean isDefaultWorld = worldName.equals(firstWorldName) ||                     // Default world
                worldName.equals(firstWorldName + "_nether") ||         // Nether dimension
                worldName.equals(firstWorldName + "_the_end");          // The end dimension


        Component component = Component.text((number + 1) + ". ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(worldName + " ", NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.text("World is " + (isLoaded ? "loaded, " + numPlayers + " players" : "unloaded") ))));

        if(!isDefaultWorld) {
            if(isLoaded) {
                component = component.append(Component.text("[Unload]", NamedTextColor.GRAY)
                        .hoverEvent(HoverEvent.showText(Component.text("Unloaded the world")))
                        .clickEvent(ClickEvent.runCommand("/world unload " + worldName)));
            } else {
                component = component.append(Component.text("[Load]", NamedTextColor.GRAY)
                        .hoverEvent(HoverEvent.showText(Component.text("Load the world")))
                        .clickEvent(ClickEvent.runCommand("/world load " + worldName)));
            }

            component = component.append(Component.text(" "))
                    .append(Component.text("[Del]", NamedTextColor.RED)
                            .hoverEvent(HoverEvent.showText(Component.text("Deletes the world")))
                            .clickEvent(ClickEvent.runCommand("/world delete " + worldName)));
        }

        return component;
    }
}
