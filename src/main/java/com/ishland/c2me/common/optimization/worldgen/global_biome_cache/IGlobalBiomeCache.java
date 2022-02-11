package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public interface IGlobalBiomeCache {

    RegistryEntry<Biome>[][][] preloadBiomes(ChunkSectionPos pos, RegistryEntry<Biome>[][][] def, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler);

    RegistryEntry<Biome> getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler);
}
