package com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.class_6880;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;

public class BiomeSourceCachingDelegate extends BiomeSource {

    private static final int CACHE_CAPACITY = 4096;

    private final Function4<Integer, Integer, Integer, MultiNoiseUtil.MultiNoiseSampler, class_6880<Biome>> delegate;
    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<class_6880<Biome>>> cache = ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    
    public BiomeSourceCachingDelegate(Function4<Integer, Integer, Integer, MultiNoiseUtil.MultiNoiseSampler, class_6880<Biome>> delegate) {
        super(List.of());
        this.delegate = delegate;
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BiomeSource withSeed(long seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public class_6880<Biome> getBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        final Long2ObjectLinkedOpenHashMap<class_6880<Biome>> cache = this.cache.get();
        long l = 0L;
        l |= ((long) biomeX & BlockPos.BITS_X) << BlockPos.BIT_SHIFT_X;
        l |= ((long) biomeY & BlockPos.BITS_Y) << 0;
        final long key = l | ((long) biomeZ & BlockPos.BITS_Z) << BlockPos.BIT_SHIFT_Z;
        final class_6880<Biome> cachedBiome = cache.get(key);
        if (cachedBiome != null) return cachedBiome;
        final class_6880<Biome> uncachedBiome = delegate.apply(biomeX, biomeY, biomeZ, multiNoiseSampler);
        ensureCapacityLimit();
        cache.put(key, uncachedBiome);
        return uncachedBiome;
    }

    private void ensureCapacityLimit() {
        final Long2ObjectLinkedOpenHashMap<class_6880<Biome>> cache = this.cache.get();
        if (cache.size() > CACHE_CAPACITY) {
            for(int k = 0; k < CACHE_CAPACITY / 16; ++k) {
                cache.removeFirst();
            }
        }
    }
}
