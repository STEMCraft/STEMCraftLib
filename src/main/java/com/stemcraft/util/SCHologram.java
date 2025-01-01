package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Hologram helper class
 */
public class SCHologram {
    private static final Map<String, Supplier<List<String>>> types = new HashMap<>();
    private static final Map<UUID, HologramData> holograms = new HashMap<>();
    private static File configFile = null;
    private static YamlConfiguration config = null;
    private static BukkitRunnable saveTask;
    private static double LINE_SPACING = 0.25;

    @Getter
    static class HologramData {
        private final UUID id;
        private final String type;
        private final List<ArmorStand> stands;
        private final List<UUID> standIds;
        private List<String> text;
        private final Location location;
        private boolean dirty;
        private boolean deleting = false;

        public HologramData(UUID id, String type, Location location, List<UUID> stand, List<String> text) {
            this.stands = new ArrayList<>();
            this.id = id;
            this.type = (type == null || type.isEmpty()) ? null : type;
            this.location = location;
            this.standIds = (stand == null) ? new ArrayList<>() : stand;
            this.text = (text == null) ? new ArrayList<>() : text;
            this.dirty = false;

            if(this.type != null && this.text.isEmpty() && types.containsKey(this.type)) {
                this.text = types.get(this.type).get();
            }
        }

        /**
         * Save the data to the configuration file
         *
         * @param force Save the data to the configuration file even if it hasn't changed
         * @return If data was saved to the configuration file
         */
        public boolean save(boolean force) {
            if(deleting) {
                stands.forEach(entity -> {
                    if(entity != null && entity.isValid()) {
                        entity.remove();
                    }
                });

                stands.clear();
                config.set("holograms." + id, null);

                holograms.remove(id);

                this.dirty = true;
                return true;
            }

            if(dirty || force) {
                config.set("holograms." + id + ".type", type);
                config.set("holograms." + id + ".location", SCString.locationToString(location, true, false));
                config.set("holograms." + id + ".stands", standIds.stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList()));
                config.set("holograms." + id + ".text", text);

                dirty = false;
                return true;
            }

            return false;
        }

        /**
         * Save the data to the configuration file
         *
         * @return If data was saved to the configuration file
         */
        public boolean save() {
            return save(false);
        }

        /**
         * Update the armor stands with the data
         */
        public void update() {
            // check if chunk is loaded
            if(location.getChunk().isLoaded()) {

                // check armor stands are valid
                // Safe removal via iterator
                stands.removeIf(entity -> entity == null || !entity.isValid());

                // check if too many stands
                while(stands.size() > text.size()) {
                    Entity entity = stands.getLast();
                    stands.remove(entity);
                    entity.remove();
                }

                // update the stands (create if required)
                double y = location.getY() + (LINE_SPACING * (text.size() + 1));
                Location standLoc = location;
                for (int i = 0; i < text.size(); i++) {
                    UUID standId;
                    String str = text.get(i);

                    if(str.isEmpty()) {
                        standLoc = standLoc.subtract(0, (LINE_SPACING / 2), 0);
                        continue;
                    }

                    standLoc = standLoc.subtract(0, LINE_SPACING, 0);

                    if(stands.size() <= i + 1) {
                        // create new stand
                        ArmorStand entity = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
                        entity.setVisible(false);
                        entity.setCustomNameVisible(true);
                        entity.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(str));
                        entity.setGravity(false);

                        standId = entity.getUniqueId();
                        stands.add(entity);
                    } else {
                        ArmorStand entity = stands.get(i);
                        entity.teleport(standLoc);
                        entity.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(str));

                        standId = entity.getUniqueId();
                    }

                    if(standIds.size() <= i + 1) {
                        standIds.add(standId);
                        this.dirty = true;
                    } else {
                        if(standIds.get(i) != standId) {
                            this.dirty = true;
                            standIds.set(i, standId);
                        }
                    }
                }
            }
        }

        /**
         * Update the text of the hologram
         * @param text The text to use
         */
        public void setText(List<String> text) {
            this.text = text;
            update();
        }

        /**
         * Delete the hologram and its data
         */
        public void delete() {
            this.deleting = true;

            save();
            SCHologram.saveAll();
        }
    }

    /**
     * Load the holograms from the configuration file
     */
    public static void init() {
        holograms.clear();

        configFile = new File(STEMCraftLib.getInstance().getDataFolder(), "holograms.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                STEMCraftLib.log(Level.SEVERE, "Could not create the holograms.yml configuration file", e);
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        if (config.contains("holograms")) {
            for (String id : Objects.requireNonNull(config.getConfigurationSection("holograms")).getKeys(false)) {
                String type = config.getString("holograms." + id + ".type");
                Location location = SCString.stringToLocation(config.getString("holograms." + id + ".location"));
                List<UUID> stands = config.getStringList("holograms." + id + ".stands")
                        .stream()
                        .map(UUID::fromString).collect(Collectors.toList());
                List<String> text = config.getStringList("holograms." + id + ".text");

                UUID uuid = UUID.fromString(id);
                HologramData hologram = new HologramData(uuid, type, location, stands, text);
                holograms.put(uuid, hologram);
            }
        }
    }

    /**
     * Register a new hologram type.
     *
     * @param type The hologram type.
     * @param supplier The supplier to update hologram type.
     */
    public static void registerType(String type, Supplier<List<String>> supplier) {
        types.put(type, supplier);
    }

    /**
     * Update all the holograms of this type.
     *
     * @param type The type to update.
     */
    public static void updateAll(String type) {
        if(type != null && !type.isEmpty() && types.containsKey(type)) {
            List<String> text = types.get(type).get();

            holograms.forEach((id, hologram) -> {
                if (hologram.getType().equals(type)) {
                    hologram.setText(text);
                }
            });
        }
    }

    /**
     * Create a new hologram
     * @param location The location of the hologram
     * @param type The type of hologram (blank for standard text)
     * @return The hologram ID
     */
    public static UUID create(Location location, String type) {
        return create(location, type, new ArrayList<>());
    }

    /**
     * Create a new hologram
     * @param location The location of the hologram
     * @param type The type of hologram (blank for standard text)
     * @param text The text of the hologram
     * @return The hologram ID
     */
    public static UUID create(Location location, String type, String text) {
        List<String> list = new ArrayList<>();
        list.add(text);

        return create(location, type, list);
    }

    /**
     * Create a new hologram
     * @param location The location of the hologram
     * @param type The type of hologram (blank for standard text)
     * @param text The text of the hologram
     * @return The hologram ID
     */
    public static UUID create(Location location, String type, List<String> text) {
        UUID id = UUID.randomUUID();

        HologramData data = new HologramData(id, type, location, null, text);
        holograms.put(id, data);

        data.update();
        SCHologram.saveAll();
        return data.getId();
    }

    /**
     * Save all the dirty holograms
     */
    private static synchronized void saveAll() {
        saveAll(false);
    }

    /**
     * Save all the dirty holograms
     *
     * @param now Save the data now, or wait a period before saving
     */
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

            saveTask.runTaskLater(STEMCraftLib.getInstance(), 10);
            return;
        }

        AtomicBoolean dirty = new AtomicBoolean(false);

        holograms.forEach((id, hologram) -> {
            if(hologram.save()) {
                dirty.set(true);
            }
        });

        if(dirty.get()) {
            try {
                config.save(configFile);
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
    public static void delete(UUID id) {
        if (holograms.containsKey(id)) {
            holograms.get(id).delete();
        }
    }

    /**
     * Return holograms within a range from a location
     * @param location The starting location to search from.
     * @param type The type of holograms to search for.
     * @param range The distance from the location to include.
     * @return A list of UUIDs found.
     */
    public static List<UUID> find(Location location, String type, int range) {
        int rangeSquared = range * range;

        return holograms.values().stream()
                .filter(hologramData -> {
                    Location targetLocation = hologramData.getLocation();

                    // Check if worlds match, distance is within range, and type matches
                    return targetLocation.getWorld().equals(location.getWorld()) &&
                            targetLocation.distanceSquared(location) <= rangeSquared &&
                            hologramData.getType().equals(type); // New type check
                })
                .map(HologramData::getId)
                .collect(Collectors.toList());
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
}
