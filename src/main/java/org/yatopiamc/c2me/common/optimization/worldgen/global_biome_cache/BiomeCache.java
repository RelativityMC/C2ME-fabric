package org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import org.threadly.concurrent.UnfairExecutor;

import java.util.List;
import java.util.WeakHashMap;

public class BiomeCache {

    public static final UnfairExecutor EXECUTOR = new UnfairExecutor(2, new ThreadFactoryBuilder().setNameFormat("C2ME biomes #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build());

    private final Registry<Biome> registry;

    private final UncachedBiomeSource uncachedBiomeSource;

    public BiomeCache(BiomeLayerSampler sampler, Registry<Biome> registry, List<Biome> biomes) {
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

    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        final ChunkPos chunkPos = new ChunkPos(biomeX >> 2, biomeZ >> 2);
        final int startX = chunkPos.getStartX() >> 2;
        final int startZ = chunkPos.getStartZ() >> 2;
        return threadLocalCache.get().computeIfAbsent(chunkPos, biomeCache).getBiomeForNoiseGen(biomeX - startX, biomeY, biomeZ - startZ);
    }

    public BiomeArray preloadBiomes(ChunkPos pos, BiomeArray def) {
        if (def != null) {
            biomeCache.put(pos, def);
            return def;
        } else {
            return biomeCache.getUnchecked(pos);
        }
    }

}
