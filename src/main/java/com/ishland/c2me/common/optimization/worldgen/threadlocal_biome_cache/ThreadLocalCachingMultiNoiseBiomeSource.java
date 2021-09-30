package com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.Optional;

public class ThreadLocalCachingMultiNoiseBiomeSource extends MultiNoiseBiomeSource {

    private final BiomeSourceCachingDelegate biomeSourceCachingDelegate = new BiomeSourceCachingDelegate(super::getBiome);

    public ThreadLocalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<Biome> entries) {
        super(entries);
    }

    public ThreadLocalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<Biome> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(entries, optional);
    }

    @Override
    public Biome getBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        return this.biomeSourceCachingDelegate.getBiome(biomeX, biomeY, biomeZ, multiNoiseSampler);
    }
}
