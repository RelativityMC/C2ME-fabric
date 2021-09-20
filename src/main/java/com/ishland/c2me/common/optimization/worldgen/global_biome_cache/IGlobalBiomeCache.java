package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public interface IGlobalBiomeCache {

    Biome[][][] preloadBiomes(ChunkSectionPos pos, Biome[][][] def, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler);

    Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler);
}
