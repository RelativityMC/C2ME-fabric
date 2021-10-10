package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.WeakHashMap;

public class BiomeCache {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Biome Cache");

    private final Registry<Biome> registry;

    private final UncachedBiomeSource uncachedBiomeSource;


    public BiomeCache(BiomeProvider sampler, Registry<Biome> registry, List<Biome> biomes) {
        this.registry = registry;
        this.uncachedBiomeSource = new UncachedBiomeSource(biomes, sampler, registry);
    }

    private final LoadingCache<ChunkPos, BiomeArray> biomeCache = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(8192)
            .build(new CacheLoader<>() {
                @Override
                public BiomeArray load(ChunkPos key) {
                    return new BiomeArray(registry, key, uncachedBiomeSource);
                }
            });

    private final ThreadLocal<WeakHashMap<ChunkPos, BiomeArray>> threadLocalCache = ThreadLocal.withInitial(WeakHashMap::new);

    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ, boolean fast) {
        final ChunkPos chunkPos = new ChunkPos(biomeX >> 2, biomeZ >> 2);
        final int startX = chunkPos.getStartX() >> 2;
        final int startZ = chunkPos.getStartZ() >> 2;
        if (fast) {
            final WeakHashMap<ChunkPos, BiomeArray> localCache = threadLocalCache.get();
            final BiomeArray biomeArray1 = localCache.get(chunkPos);
            if (biomeArray1 != null) return biomeArray1.getBiomeForNoiseGen(biomeX - startX, biomeY, biomeZ - startZ);
            final BiomeArray biomeArray2 = this.biomeCache.asMap().get(chunkPos);
            if (biomeArray2 != null) {
                localCache.put(chunkPos, biomeArray2);
                return biomeArray2.getBiomeForNoiseGen(biomeX - startX, biomeY, biomeZ - startZ);
            }
            return uncachedBiomeSource.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        } else {
            return threadLocalCache.get().computeIfAbsent(chunkPos, biomeCache).getBiomeForNoiseGen(biomeX - startX, biomeY, biomeZ - startZ);
        }
    }

    public BiomeArray preloadBiomes(ChunkPos pos, BiomeArray def) {
        if (def != null) {
            this.biomeCache.put(pos, def);
            return def;
        } else {
            return this.biomeCache.getUnchecked(pos);
        }
    }

    public interface BiomeProvider {
        Biome sample(Registry<Biome> biomeRegistry, int x, int y, int z);
    }

}
