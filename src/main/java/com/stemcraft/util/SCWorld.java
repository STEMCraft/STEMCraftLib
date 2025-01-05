package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import com.stemcraft.event.WorldDeleteEvent;
import com.stemcraft.exception.MainWorldDeletionException;
import com.stemcraft.exception.MainWorldUnloadException;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SCWorld {
    private static final List<String> unloadingList = new ArrayList<>();
    private static File configFile;
    private static YamlConfiguration config;

    public enum WorldStatus {
        UNLOADING_WORLD,
        UNLOADED_WORLD,
        WORLD_NOT_LOADED,
        DELETING_WORLD,
        DELETED_WORLD
    }

    public interface WorldStatusCallback {
        void onStatusUpdate(WorldStatus status); // Called with status updates
    }

    public static void init() {
        configFile = new File(STEMCraftLib.getInstance().getDataFolder(), "worlds.yml");
        if (!configFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();
            } catch (IOException e) {
                STEMCraftLib.log(Level.SEVERE, "Could not create the worlds.yml configuration file", e);
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // load worlds that are set to autoload
        ConfigurationSection worlds = config.getConfigurationSection("worlds");
        if(worlds != null) {
            for (String worldName : worlds.getKeys(false)) {
                if (config.getBoolean("worlds." + worldName + ".autoload", false)) {
                    load(worldName);
                }
            }
        }
    }

    /**
     * Save the world configuration files.
     */
    public static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            STEMCraftLib.log(Level.SEVERE, "Failed to save world configuration files", e);
        }
    }

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
     * Is a world currently loaded
     *
     * @param name The world name
     * @return If the world is loaded
     */
    public static boolean isLoaded(String name) {
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equals(name)) {
                return true;
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
    @SuppressWarnings("UnusedReturnValue")
    public static World load(String name) {
        if(exists(name)) {
            World world = Bukkit.getWorld(name);
            if(world != null) {
                STEMCraftLib.log("world {name} found in bukkit list", "name", name);
                return world;
            }

            if(!isUnloading(name)) {
                STEMCraftLib.log("using worldcreator for world {name}", "name", name);
                WorldCreator creator = new WorldCreator(name);
                return Bukkit.createWorld(creator);
            } else {
                STEMCraftLib.log("could not load world {name} as its unloading", "name", name);
                return null;
            }
        }

        STEMCraftLib.log("could not load world {name}", "name", name);
        return null;
    }

    /**
     * Get/Set the autoload setting for a world.
     *
     * @param name The world name
     * @return The world or NULL
     */
    public static boolean autoLoad(String name, Boolean autoLoad) {
        if(autoLoad != null) {
            config.set("worlds." + name + ".autoload", autoLoad);
            saveConfig();
        }

        return config.getBoolean("worlds." + name + ".autoload", false);
    }

    /**
     * Get/Set the autoload setting for a world.
     *
     * @param world The world to check
     * @param autoLoad The value to set
     * @return The world or NULL
     */
    public static boolean autoLoad(World world, Boolean autoLoad) {
        return autoLoad(world.getName(), autoLoad);
    }

    public static boolean autoLoad(String name) {
        return autoLoad(name, null);
    }

    public static boolean autoLoad(World world) {
        return autoLoad(world.getName(), null);
    }

    /**
     * Create a new world
     * @param name The name of the world
     * @param chunkGenerator The chunk generator class
     * @param settings The generator settings
     * @param seed The generator seed
     * @return  The created world
     */
    public static World create(String name, ChunkGenerator chunkGenerator, String settings, Long seed) {
        if(!exists(name)) {
            World world;

            WorldCreator c = new WorldCreator(name);
            c.generatorSettings("{}");
            c.generator(chunkGenerator);
            if(settings != null) c.generatorSettings(settings);
            if(seed != null) c.seed(seed);
            world = c.createWorld();

            return world;
        }

        return null;
    }

    /**
     * Create a new world
     * @param name The name of the world
     * @param generatorName The chunk generator name
     * @param settings The generator settings
     * @param seed The generator seed
     * @return  The created world
     */
    public static World create(String name, String generatorName, String settings, Long seed) {
        ChunkGenerator chunkGenerator = null;

        if (generatorName != null) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                try {
                    // Attempt to fetch a generator from each plugin
                    ChunkGenerator generator = plugin.getDefaultWorldGenerator(name, generatorName);
                    if (generator != null) {
                        chunkGenerator = generator;
                        break; // Stop once we find a valid generator
                    }
                } catch (Exception ignored) {
                    // Ignore any exceptions from plugins that don't support generators
                }
            }

            if (chunkGenerator == null) {
                throw new IllegalArgumentException("No valid generator found for name: " + generatorName);
            }
        }

        return create(name, chunkGenerator, settings, seed);
    }

    /**
     * Returns if the server is unloading the world
     * @param worldName The world to check
     * @return If the server is unloading the world
     */
    public static boolean isUnloading(String worldName) {
        return unloadingList.contains(worldName);
    }

    /**
     * Safely unload a world, teleporting the players back to the default world
     *
     * @param world The world to unload
     * @param save Save the world on unload
     * @param statusCallback Callback on progress
     */
    public static void unload(World world, Boolean save, WorldStatusCallback statusCallback) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if(!Bukkit.getWorlds().contains(world)) {
                    this.cancel();

                    Bukkit.getScheduler().runTaskLater(STEMCraftLib.getInstance(), () -> {
                        STEMCraftLib.log("Unloaded world {name}", "name", world.getName());
                        unloadingList.remove(world.getName());
                    }, 20L);

                    if(statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.UNLOADED_WORLD);
                }
            }
        };

        if(world != null) {
            if(!unloadingList.contains(world.getName())) {
                if (Bukkit.getWorlds().getFirst() != world) {
                    if(statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.UNLOADING_WORLD);

                    String name = world.getName();
                    unloadingList.add(name);

                    STEMCraftLib.log("Unloading world {name}", "name", name);

                    CompletableFuture<Void> teleportTasks = CompletableFuture.allOf(
                            world.getPlayers().stream().map(player -> {
                                STEMCraftLib.warning(player, "World '{name}' is being unloaded, teleporting to main world", "name", name);
                                return SCPlayer.teleport(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
                            }).toArray(CompletableFuture[]::new)
                    );

                    teleportTasks.thenRun(() -> Bukkit.getScheduler().runTaskLater(STEMCraftLib.getInstance(), () -> {
                        Bukkit.unloadWorld(world, save);

                        task.runTaskTimer(STEMCraftLib.getInstance(), 10L, 10L);
                    }, 20L));
                } else {
                    throw new MainWorldUnloadException();
                }
            } else {
                if(statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.UNLOADING_WORLD);
            }

            task.runTaskTimer(STEMCraftLib.getInstance(), 10L, 10L);
        } else {
            if(statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.WORLD_NOT_LOADED);
        }
    }

    /**
     * Delete a world from the server.
     *
     * @param worldName The world to remove
     * @param statusCallback Callback on progress
     */
    public static void delete(String worldName, WorldStatusCallback statusCallback) {
        World world = Bukkit.getWorld(worldName);
        if(world != null) {
            delete(world, statusCallback);
        } else {
            File worldFolder = getWorldFolder(worldName);
            if (worldFolder.exists()) {
                deleteFolder(worldFolder);
            }

            STEMCraftLib.log("Deleted world {name}", "name", worldName);
            Bukkit.getPluginManager().callEvent(new WorldDeleteEvent(worldName));

            if (statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.DELETED_WORLD);
        }
    }

    /**
     * Delete a world from the server.
     *
     * @param world The world to remove
     * @param statusCallback Callback on progress
     */
    public static void delete(World world, WorldStatusCallback statusCallback) {
        if(world != null) {
            if(Bukkit.getWorlds().getFirst() != world) {
                String name = world.getName();

                config.set("worlds." + world.getName(), null);

                unload(world, false, status -> {
                    if(statusCallback != null) statusCallback.onStatusUpdate(status);

                    if(status == WorldStatus.UNLOADED_WORLD) {
                        Bukkit.getScheduler().runTaskLater(STEMCraftLib.getInstance(), () -> {
                            if (statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.DELETING_WORLD);
                            File worldFolder = getWorldFolder(name);
                            if (worldFolder.exists()) {
                                deleteFolder(worldFolder);
                            }

                            STEMCraftLib.log("Deleted world {name}", "name", name);
                            Bukkit.getPluginManager().callEvent(new WorldDeleteEvent(name));

                            if (statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.DELETED_WORLD);
                        }, 20L);
                    }
                });
            } else {
                throw new MainWorldDeletionException();
            }
        } else {
            if (statusCallback != null) statusCallback.onStatusUpdate(WorldStatus.DELETED_WORLD);
        }
    }

    /**
     * Get the last location of a player within a world.
     *
     * @param world The world to lookup
     * @param player The player to lookup
     * @return The last location or spawn
     */
    public static Location getLastLocation(World world, Player player) {
        String lastLocation = config.getString("worlds." + world.getName() + ".players." + player.getUniqueId() + ".last-location");
        if(lastLocation != null) {
            return SCString.stringToLocation(lastLocation, world);
        }

        return world.getSpawnLocation();
    }

    /**
     * Update the last location of a player within a world.
     *
     * @param player The player to update
     * @param location The last location to update
     */
    public static void updateLastLocation(Player player, Location location) {
        String lastLocation = SCString.locationToString(location, false, true);
        config.set("worlds." + location.getWorld().getName() + ".players." + player.getUniqueId() + ".last-location", lastLocation);
    }

    /**
     * Delete a world from the server
     * @param world The world to remove
     */
    public static void delete(World world) {
        delete(world, null);
    }

    /**
     * Delete a world from the server
     * @param worldName The world name to remove
     */
    public static void delete(String worldName) {
        if(worldName != null) {
            if(!Bukkit.getWorlds().getFirst().getName().equalsIgnoreCase(worldName)) {
                World world = Bukkit.getWorld(worldName);
                if(world != null) {
                    delete(world);
                } else {
                    File worldFolder = getWorldFolder(worldName);
                    if(worldFolder.exists()) {
                        deleteFolder(worldFolder);
                    }
                }
            }
        }
    }

    /**
     * Duplicate a world.
     *
     * @param sourceWorldName The source world name
     * @param targetWorldName The destination world name
     */
    public static void duplicate(String sourceWorldName, String targetWorldName) {
        World sourceWorld = Bukkit.getWorld(sourceWorldName);
        if (sourceWorld == null || exists(targetWorldName)) return;

        File sourceFolder = getWorldFolder(sourceWorldName);
        File targetFolder = getWorldFolder(targetWorldName);

        try {
            copyFolder(sourceFolder, targetFolder);
            WorldCreator creator = new WorldCreator(targetWorldName);
            Bukkit.createWorld(creator);
        } catch (IOException e) {
            STEMCraftLib.log(Level.SEVERE, "Failed to duplicate world", e);
        }
    }

    /**
     * Get/Set the bed respawn setting for a world.
     * @param world The world to check
     * @param value The value to set
     * @return  The bed respawn setting
     */
    public static boolean bedRespawn(World world, Boolean value) {
        if(value != null) {
            config.set("worlds." + world.getName() + ".bedRespawn", value);
            saveConfig();
        }

        return config.getBoolean("worlds." + world.getName() + ".bedRespawn", true);
    }

    public static boolean bedRespawn(World world) {
        return bedRespawn(world, null);
    }

    /**
     * Get/Set the world game mode.
     * @param world The world to check
     * @param value The value to set
     * @return  The world game mode setting
     */
    public static GameMode gameMode(World world, GameMode value) {
        if(value != null) {
            config.set("worlds." + world.getName() + ".game-mode", value.toString());
            saveConfig();
        }

        String gameModeString = config.getString("worlds." + world.getName() + ".game-mode");
        if(gameModeString != null) {
            return GameMode.valueOf(gameModeString);
        }

        return null;
    }

    public static GameMode gameMode(World world) {
        return gameMode(world, null);
    }

    private static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) { // Check if the folder is not empty
                for (File file : files) {
                    deleteFolder(file); // Recursive call
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        folder.delete(); // Delete the folder or file
    }

    private static void copyFolder(File source, File target) throws IOException {
        if (!target.exists()) //noinspection ResultOfMethodCallIgnored
            target.mkdirs();
        for (File file : Objects.requireNonNull(source.listFiles())) {
            File targetFile = new File(target, file.getName());
            if (file.isDirectory()) {
                copyFolder(file, targetFile);
            } else {
                java.nio.file.Files.copy(file.toPath(), targetFile.toPath());
            }
        }
    }
}