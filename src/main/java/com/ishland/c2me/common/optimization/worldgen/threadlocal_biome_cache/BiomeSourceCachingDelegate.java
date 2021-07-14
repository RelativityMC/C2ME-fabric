package com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;

import java.util.List;

public class BiomeSourceCachingDelegate extends BiomeSource {

    private static final int CACHE_CAPACITY = 4096;

    private final Function3<Integer, Integer, Integer, Biome> delegate;
    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Biome>> cache = ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    
    public BiomeSourceCachingDelegate(Function3<Integer, Integer, Integer, Biome> delegate) {
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
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        final Long2ObjectLinkedOpenHashMap<Biome> cache = this.cache.get();
        final long key = BlockPos.asLong(biomeX, biomeY, biomeZ);
        final Biome cachedBiome = cache.get(key);
        if (cachedBiome != null) return cachedBiome;
        final Biome uncachedBiome = delegate.apply(biomeX, biomeY, biomeZ);
        ensureCapacityLimit();
        cache.put(key, uncachedBiome);
        return uncachedBiome;
    }

    private void ensureCapacityLimit() {
        final Long2ObjectLinkedOpenHashMap<Biome> cache = this.cache.get();
        if (cache.size() > CACHE_CAPACITY) {
            for(int k = 0; k < CACHE_CAPACITY / 16; ++k) {
                cache.removeFirst();
            }
        }
    }
}
