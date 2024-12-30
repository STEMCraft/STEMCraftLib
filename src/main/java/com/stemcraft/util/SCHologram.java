package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class SCHologram {
    private static final Map<String, HologramItem> holograms = new HashMap<>();
    private static File hologramConfigFile = null;
    private static YamlConfiguration hologramConfig = null;
    private static BukkitRunnable saveTask;

    @Getter
    static class HologramItem {
        private ArmorStand stand = null;
        private Location location;
        private String text;
        private String id;
        private boolean dirty;

        public HologramItem(String id, Location location, String text) {
            this.id = id;
            this.location = location;
            this.text = text;
            this.dirty = false;
        }

        public void setLocation(Location location) {
            this.dirty = true;
            this.location = location;
            if(stand != null && stand.isValid()) {
                stand.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
            }
        }

        public void setText(String text) {
            this.dirty = true;
            this.text = text;
            if(stand != null && stand.isValid()) {
                stand.teleport(location);
            }
        }

        public boolean update() {
            if(stand == null || !stand.isValid()) {
                if (!location.isChunkLoaded()) {
                    return false;
                }

                Entity entity = location.getWorld().getEntity(UUID.fromString(id));
                if (entity instanceof ArmorStand && entity.isValid() && entity.getUniqueId().toString().equals(id)) {
                    stand = (ArmorStand) entity;
                } else {
                    for (Entity e : location.getChunk().getEntities()) {
                        if (e instanceof ArmorStand && e.isValid() && e.getUniqueId().toString().equals(id)) {
                            stand = (ArmorStand) e;
                            break;
                        }
                    }

                    if (stand == null || !stand.isValid()) {
                        stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                        stand.setVisible(false);
                        stand.setCustomNameVisible(true);
                        stand.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
                        stand.setGravity(false);

                        this.id = stand.getUniqueId().toString();
                        this.dirty = true;
                        return true;
                    }
                }
            }

            stand.teleport(location);
            stand.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(text));

            return false;
        }

        public void despawn() {
            if(stand != null && stand.isValid()) {
                stand.remove();
                stand = null;
            }
        }
    }

    /**
     * Load the holograms from the configuration file
     */
    public static void load() {
        holograms.clear();

        hologramConfigFile = new File(STEMCraftLib.getInstance().getDataFolder(), "holograms.yml");
        if(hologramConfigFile.exists()) {
            hologramConfig = YamlConfiguration.loadConfiguration(hologramConfigFile);

            if (hologramConfig.contains("holograms")) {
                for (String id : Objects.requireNonNull(hologramConfig.getConfigurationSection("holograms")).getKeys(false)) {
                    String worldName = hologramConfig.getString("holograms." + id + ".world");
                    double x = hologramConfig.getDouble("holograms." + id + ".x");
                    double y = hologramConfig.getDouble("holograms." + id + ".y");
                    double z = hologramConfig.getDouble("holograms." + id + ".z");
                    String text = hologramConfig.getString("holograms." + id + ".text");

                    if (worldName != null && SCWorld.isLoaded(worldName)) {
                        Location location = new Location(Bukkit.getWorld(worldName), x, y, z);

                        HologramItem hologram = new HologramItem(id, location, text);
                        hologram.update();
                        holograms.put(hologram.getId(), hologram);
                    } else {
                        STEMCraftLib.log(Level.WARNING, "World " + worldName + " for hologram " + id + " does not exist.");
                    }
                }
            }
        }
    }

    private synchronized void saveAll() {
        saveAll(false);
    }

    private static synchronized void saveAll(boolean now) {
        if(!now) {
            if (saveTask != null) {
                saveTask.cancel();
            }

            saveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    saveAll(true);
                }
            };

            saveTask.runTaskLater(STEMCraftLib.getInstance(), 100);
            return;
        }

        AtomicBoolean dirty = new AtomicBoolean(false);

        holograms.forEach((id, hologram) -> {
            if(hologram.isDirty()) {
                dirty.set(true);

                String newId = hologram.getId();
                if(!id.equals(newId)) {
                    holograms.put(newId, hologram);
                    holograms.remove(id);

                    hologramConfig.set("holograms." + id, null);

                    hologramConfig.set("holograms." + newId + ".world", hologram.getLocation().getWorld().getName());
                    hologramConfig.set("holograms." + newId + ".x", hologram.getLocation().getX());
                    hologramConfig.set("holograms." + newId + ".y", hologram.getLocation().getY());
                    hologramConfig.set("holograms." + newId + ".z", hologram.getLocation().getZ());
                    hologramConfig.set("holograms." + newId + ".text", hologram.getText());
                }
            }
        });

        if(dirty.get()) {
            try {
                hologramConfig.save(hologramConfigFile);
            } catch (IOException e) {
                STEMCraftLib.log(Level.SEVERE, "Failed to save the holograms configuration file", e);
            }
        }
    }

    /**
     * Deletes an existing hologram by its ID.
     *
     * @param id The unique ID of the hologram.
     */
    public static void delete(String id) {
        if (holograms.containsKey(id)) {
            holograms.get(id).despawn();
            holograms.remove(id);
        }

        if(hologramConfig.contains("holograms." + id)) {
            hologramConfig.set("holograms." + id, null);

            try {
                hologramConfig.save(hologramConfigFile);
            } catch (IOException e) {
                STEMCraftLib.log(Level.SEVERE, "Failed to save the holograms configuration file", e);
            }
        }
    }

    /**
     * Update the holograms in a specified chunk
     * @param chunk The chunk to update.
     */
    public static void updateChunk(Chunk chunk) {
        if(chunk.isLoaded()) {
            holograms.forEach((id, hologram) -> {
                if(hologram.location.getChunk().equals(chunk)) {
                    hologram.update();
                }
            });
        }
    }

    /**
     * Updates a hologram to function like a scoreboard with multiple lines.
     *
     * @param id       The unique ID of the hologram.
     * @param location The base location of the hologram.
     * @param lines    The lines to display, top to bottom.
     */
//    public void createOrUpdateScoreboardHologram(String id, Location location, String... lines) {
//        deleteHologram(id); // Remove existing hologram if it exists.
//
//        Location currentLocation = location.clone();
//        for (String line : lines) {
//            createOrUpdateHologram(id + "_" + UUID.randomUUID(), currentLocation, line);
//            currentLocation.subtract(0, 0.3, 0); // Adjust spacing for next line.
//        }
//    }
}
