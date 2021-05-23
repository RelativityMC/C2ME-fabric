package org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.source.BiomeArray;

public interface IVanillaLayeredBiomeSource {

    BiomeArray preloadBiomes(ChunkPos pos, BiomeArray def);

    BiomeArray getBiomes(ChunkPos pos);

}
