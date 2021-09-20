package com.ishland.c2me.common.util;

import com.google.common.base.Preconditions;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.PalettedContainer;

public class PalettedContainerUtil {

    private PalettedContainerUtil() {
    }

    public static Biome[][][] toArray(PalettedContainer<Biome> palettedContainer, int xSize, int ySize, int zSize) {
        if (palettedContainer == null) return null;
        final Biome[][][] biomes = new Biome[xSize][ySize][zSize];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                for (int z = 0; z < zSize; z++) {
                    biomes[x][y][z] = palettedContainer.get(x, y, z);
                }
            }
        }
        return biomes;
    }

    public static void writeArray(PalettedContainer<Biome> palettedContainer, Biome[][][] array) {
        Preconditions.checkNotNull(palettedContainer);
        Preconditions.checkNotNull(array);
        palettedContainer.lock();
        try {
            for (int x = 0, arrayLength = array.length; x < arrayLength; x++) {
                Biome[][] biomes1 = array[x];
                for (int y = 0, biomesLength = biomes1.length; y < biomesLength; y++) {
                    Biome[] biomes2 = biomes1[y];
                    for (int z = 0, biomeLength = biomes2.length; z < biomeLength; z++) {
                        Biome biome = biomes2[z];
                        palettedContainer.set(x, y, z, biome);
                    }
                }
            }
        } finally {
            palettedContainer.unlock();
        }
    }

}
