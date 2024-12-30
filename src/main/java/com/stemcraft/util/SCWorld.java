package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SCWorld {
    /**
     * Are worlds part of the same realm?
     *
     * @param worldA World A to test
     * @param worldB World B to test
     * @return If both world names are in the same realm
     */
    public static boolean sameRealm(World worldA, World worldB) {
        String worldAName = worldA.getName().toLowerCase();
        String worldBName = worldB.getName().toLowerCase();

        if (worldAName.equals(worldBName)) {
            return true;
        }

        return worldAName.replace("_nether", "").replace("_the_end", "").equals(
                worldBName.replace("_nether", "").replace("_the_end", "")
        );
    }

    /**
     * Get the world folder
     * @param worldName The world name
     * @return A File object to the world folder
     */
    private static File getWorldFolder(String worldName) {
        return new File(Bukkit.getWorldContainer(), worldName);
    }

    /**
     * Get the world region folder
     * @param worldName The world name
     * @return A File object to the world region folder
     */
    private static File getWorldRegionFolder(String worldName) {
        File mainFolder = getWorldFolder(worldName);

        // Overworld
        File tmp = new File(mainFolder, "region");
        if (tmp.exists()) {
            return tmp;
        }

        // Nether
        tmp = new File(mainFolder, "DIM-1" + File.separator + "region");
        if (tmp.exists()) {
            return tmp;
        }

        // The End
        tmp = new File(mainFolder, "DIM1" + File.separator + "region");
        if (tmp.exists()) {
            return tmp;
        }

        // Unknown???
        return null;
    }

    /**
     * Return if a world exists on the server (including unloaded worlds)
     * @param name The world name
     * @return If the world exists
     */
    public static boolean exists(String name) {
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equals(name)) {
                return true;
            }
        }

        return isLoadable(name);
    }

    /**
     * Return a list of worlds on the server
     * @return A list of world names
     */
    public static Collection<String> list() {
        String[] subDirs = Bukkit.getWorldContainer().list();
        assert subDirs != null;
        Collection<String> list = new ArrayList<>(subDirs.length);
        for (String name : subDirs) {
            if (isLoadable(name)) {
                list.add(name);
            }
        }
        return list;
    }

    /**
     * Check if a world is loadable (valid)
     * @param name The world name
     * @return If the world is loadable
     */
    private static boolean isLoadable(String name) {
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equals(name)) {
                return true;
            }
        }

        File worldFolder = getWorldFolder(name);
        if (!worldFolder.isDirectory()) {
            return false;
        }

        if (new File(worldFolder, "level.dat").exists()) {
            return true;
        }

        File regionFolder = getWorldRegionFolder(name);
        if (regionFolder != null) {
            for (String fileName : Objects.requireNonNull(regionFolder.list())) {
                if (fileName.toLowerCase(Locale.ENGLISH).endsWith(".mca")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Load a world from disk
     *
     * @param name The world name
     * @return The world or NULL
     */
    public static World load(String name) {
        if(exists(name)) {
            WorldCreator creator = new WorldCreator(name);
            return Bukkit.createWorld(creator);
        }

        return null;
    }

    /**
     * Create a new world
     * @param name The name of the world
     * @param chunkGenerator The chunk generator class
     * @return  The created world
     */
    public static World create(String name, ChunkGenerator chunkGenerator) {
        if(!exists(name)) {
            World world;

            WorldCreator c = new WorldCreator(name);
            c.generatorSettings("{}");
            c.generator(chunkGenerator);
            world = c.createWorld();

            return world;
        }

        return null;
    }

    /**
     * Safely unload a world, teleporting the players back to the default world
     *
     * @param world The world to unload
     * @param save Save the world on unload
     * @param callback Callback once the world is unloaded
     */
    public static void unload(World world, Boolean save, Runnable callback) {
        if(world != null) {
            if(Bukkit.getWorlds().getFirst() != world) {
                String name = world.getName();

                CompletableFuture<Void> teleportTasks = CompletableFuture.allOf(
                        world.getPlayers().stream().map(player -> {
                            STEMCraftLib.warning(player, "World '{name}' is being unloaded, teleporting to main world", "name", name);
                            return SCPlayer.teleport(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
                        }).toArray(CompletableFuture[]::new)
                );

                teleportTasks.thenRun(() -> Bukkit.getScheduler().runTaskLater(STEMCraftLib.getInstance(), () -> {
                    Bukkit.unloadWorld(world, save);
                    if(callback != null) {
                        callback.run();
                    }
                }, 1L));
            }
        }
    }

    /**
     * Remove a world from the server
     * @param world The world to remove
     * @param sender Send messages to sender. Use null for quiet
     */
    public void remove(World world, CommandSender sender, Runnable callback) {
        if(world != null) {
            if(Bukkit.getWorlds().getFirst() != world) {
                String name = world.getName();

                unload(world, false, () -> {
                    File worldFolder = getWorldFolder(name);
                    if(worldFolder.exists()) {
                        deleteFolder(worldFolder);
                    }

                    if(sender != null) {
                        STEMCraftLib.info(sender, "The world '{name}' has been removed", "name", name);
                    }
                });
            } else if(sender != null){
                STEMCraftLib.error(sender, "You cannot remove the main world");
            }
        }
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) { // Check if the folder is not empty
                for (File file : files) {
                    deleteFolder(file); // Recursive call
                }
            }
        }
        folder.delete(); // Delete the folder or file
    }
}