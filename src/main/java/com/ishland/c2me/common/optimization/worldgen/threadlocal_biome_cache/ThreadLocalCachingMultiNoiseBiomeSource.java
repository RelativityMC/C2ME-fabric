package com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache;

import net.minecraft.class_6880;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.Optional;

public class ThreadLocalCachingMultiNoiseBiomeSource extends MultiNoiseBiomeSource {

    private final BiomeSourceCachingDelegate biomeSourceCachingDelegate = new BiomeSourceCachingDelegate(super::getBiome);

    public ThreadLocalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<class_6880<Biome>> entries) {
        super(entries);
    }

    public ThreadLocalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<class_6880<Biome>> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<MultiNoiseBiomeSource.Instance> optional) {
        super(entries, optional);
    }

    @Override
    public class_6880<Biome> getBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        return this.biomeSourceCachingDelegate.getBiome(biomeX, biomeY, biomeZ, multiNoiseSampler);
    }
}
