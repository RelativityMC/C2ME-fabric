package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.class_6880;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public interface IGlobalBiomeCache {

    class_6880<Biome>[][][] preloadBiomes(ChunkSectionPos pos, class_6880<Biome>[][][] def, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler);

    class_6880<Biome> getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler);
}
