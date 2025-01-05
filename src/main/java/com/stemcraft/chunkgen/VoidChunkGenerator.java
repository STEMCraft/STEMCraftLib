package com.stemcraft.chunkgen;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A chunk generator that generates a void (empty) world.
 */
public class VoidChunkGenerator extends ChunkGenerator {

    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new BiomeProvider() {
            @NotNull
            @Override
            public Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                return Biome.THE_VOID;
            }

            @NotNull
            @Override
            public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                List<Biome> list = new ArrayList<>();

                list.add(Biome.THE_VOID);
                return list;
            }
        };
    }
}
