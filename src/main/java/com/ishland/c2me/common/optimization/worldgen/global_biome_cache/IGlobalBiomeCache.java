package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;

public interface IGlobalBiomeCache {

    BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def);

    Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ);
}
