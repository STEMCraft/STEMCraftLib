package com.stemcraft.util;

import com.stemcraft.STEMCraftLib;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class SCItem {
    private static final Map<String, Double> prices = new HashMap<>();
    private static File pricesConfigFile = null;
    private static YamlConfiguration pricesConfig = null;

    /**
     * Adds an attribute to the ItemStack with the given key and value.
     *
     * @param item  The ItemStack to modify.
     * @param key   The key for the attribute.
     * @param value The value for the attribute.
     */
    public static <T, Z> void addAttrib(ItemStack item, String key, T value) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraftLib.getInstance(), key);
            PersistentDataType<Z, T> type = getPersistentDataType(value);
            if (type != null) {
                meta.getPersistentDataContainer().set(namespacedKey, type, value);
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Checks if the ItemStack has an attribute with the given key.
     *
     * @param item The ItemStack to check.
     * @param key  The key for the attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public static boolean hasAttrib(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraftLib.getInstance(), key);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            return container.has(namespacedKey, PersistentDataType.STRING);
        }
        return false;
    }

    /**
     * Removes an attribute from the ItemStack with the given key.
     *
     * @param item The ItemStack to modify.
     * @param key  The key for the attribute to remove.
     */
    public static void removeAttrib(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraftLib.getInstance(), key);
            meta.getPersistentDataContainer().remove(namespacedKey);
            item.setItemMeta(meta);
        }
    }

    /**
     * Retrieves an attribute from the ItemStack with the given key or returns a default value if not found.
     *
     * @param item         The ItemStack to check.
     * @param key          The key for the attribute.
     * @param typeClass    The class of the type you're expecting (String.class, Byte.class, etc.).
     * @param defaultValue The default value to return if the attribute is not found or there's an issue.
     * @return The value of the attribute or the default value.
     */
    public static <T, Z> T getAttrib(ItemStack item, String key, Class<T> typeClass, T defaultValue) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraftLib.getInstance(), key);
            PersistentDataType<Z, T> type = getPersistentDataType(typeClass);
            if (type != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(namespacedKey, type)) {
                    T value = container.get(namespacedKey, type);
                    if (value != null) {
                        return value;
                    }
                }
            }
        }

        return defaultValue;
    }

    public static <T, Z> T getAttrib(ItemStack item, String key, Class<T> typeClass) {
        return getAttrib(item, key, typeClass, null);
    }

    /**
     * Determines the PersistentDataType based on the object provided (class or value).
     *
     * @param object The object for which to determine the PersistentDataType (Class<?> or instance of a type).
     * @return The corresponding PersistentDataType, or null if the type is unsupported.
     */
    @SuppressWarnings("unchecked")
    private static <T, Z> PersistentDataType<Z, T> getPersistentDataType(Object object) {
        if (object instanceof Class<?> typeClass) {
            if (typeClass == String.class) {
                return (PersistentDataType<Z, T>) PersistentDataType.STRING;
            } else if (typeClass == Byte.class) {
                return (PersistentDataType<Z, T>) PersistentDataType.BYTE;
            } else if (typeClass == Integer.class) {
                return (PersistentDataType<Z, T>) PersistentDataType.INTEGER;
            } else if (typeClass == Double.class) {
                return (PersistentDataType<Z, T>) PersistentDataType.DOUBLE;
            } else if (typeClass == Float.class) {
                return (PersistentDataType<Z, T>) PersistentDataType.FLOAT;
            }
        } else {
            if (object instanceof String) {
                return (PersistentDataType<Z, T>) PersistentDataType.STRING;
            } else if (object instanceof Byte) {
                return (PersistentDataType<Z, T>) PersistentDataType.BYTE;
            } else if (object instanceof Integer) {
                return (PersistentDataType<Z, T>) PersistentDataType.INTEGER;
            } else if (object instanceof Double) {
                return (PersistentDataType<Z, T>) PersistentDataType.DOUBLE;
            } else if (object instanceof Float) {
                return (PersistentDataType<Z, T>) PersistentDataType.FLOAT;
            }
        }
        // Add more types if needed
        return null;
    }

    /**
     * Load the material prices from the supplied configuration file
     * @param configFile The configuration file
     */
    public static void loadPricesFromConfig(File configFile) {
        if (!configFile.exists()) {
            return; // Exit if the config file doesn't exist
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Clear existing prices to avoid duplicates on reload
        prices.clear();

        // Read the 'prices' section from the config
        if (config.contains("prices")) {
            Map<String, Object> rawPrices = Objects.requireNonNull(config.getConfigurationSection("prices")).getValues(false);

            for (Map.Entry<String, Object> entry : rawPrices.entrySet()) {
                String key = entry.getKey().toUpperCase(); // Ensure key is lowercase
                Object value = entry.getValue();

                double price;

                // Try parsing the value as float
                if (value instanceof Number) {
                    price = ((Number) value).doubleValue(); // Accept int or float
                } else {
                    try {
                        price = Double.parseDouble(value.toString()); // Attempt string-to-float conversion
                    } catch (NumberFormatException e) {
                        price = 0; // Default to 0 for invalid numbers
                    }
                }

                // Store the validated price
                prices.put(key, price);
            }
        }

        pricesConfigFile = configFile;
        pricesConfig = config;
    }

    /**
     * Get the price of a material
     * @param material The material to lookup
     * @param defaultPrice The default price if it doesn't exist
     * @return The price
     */
    public static double getPrice(String material, double defaultPrice) {
        return prices.getOrDefault(material.toUpperCase(), defaultPrice);
    }

    /**
     * Get the price of a material
     * @param material The material to lookup
     * @return The price
     */
    public static double getPrice(String material) {
        return getPrice(material, 0);
    }

    /**
     * Set the price of a material item
     * @param material The material to lookup
     * @param price The price to set the material to
     */
    public static void setPrice(String material, double price) {
        String materialUC = material.toUpperCase();

        prices.put(materialUC, price);
        pricesConfig.set("prices." + materialUC, price);

        try {
            // Save the file to disk
            pricesConfig.save(pricesConfigFile);
        } catch (IOException e) {
            STEMCraftLib.log(Level.SEVERE, "An error occurred saving the prices config file", e);
        }
    }
}
