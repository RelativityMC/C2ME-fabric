package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache.BiomeSourceCachingDelegate;
import com.mojang.datafixers.util.Function4;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threadly.concurrent.UnfairExecutor;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MultiBiomeCache {

    public static final UnfairExecutor EXECUTOR = new UnfairExecutor(2, new ThreadFactoryBuilder().setNameFormat("C2ME biomes #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build());
    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Biome Cache");

    private final IndexedIterable<Biome> registry;

    private final Function4<Integer, Integer, Integer, MultiNoiseUtil.MultiNoiseSampler, Biome> delegate;
    private final BiomeSourceCachingDelegate biomeSourceCachingDelegate;

    public MultiBiomeCache(Function4<Integer, Integer, Integer, MultiNoiseUtil.MultiNoiseSampler, Biome> delegate, IndexedIterable<Biome> registry) {
        this.registry = registry;
        this.delegate = delegate;
        this.biomeSourceCachingDelegate = new BiomeSourceCachingDelegate(this.delegate);
    }

    private final ConcurrentHashMap<MultiNoiseUtil.MultiNoiseSampler, LoadingCache<ChunkSectionPos, Biome[][][]>> biomeCaches = new ConcurrentHashMap<>();

    private final ThreadLocal<WeakHashMap<MultiNoiseUtil.MultiNoiseSampler, WeakHashMap<ChunkSectionPos, Biome[][][]>>> threadLocalCache = ThreadLocal.withInitial(WeakHashMap::new);

    private LoadingCache<ChunkSectionPos, Biome[][][]> createCache(MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        Preconditions.checkNotNull(multiNoiseSampler);
        return CacheBuilder.newBuilder()
                .softValues()
                .maximumSize(8192)
                .build(new CacheLoader<>() {
                    @Override
                    public Biome[][][] load(ChunkSectionPos key) {
                        int startX = BiomeCoords.fromBlock(key.getMinX());
                        int startY = BiomeCoords.fromBlock(key.getMinY());
                        int startZ = BiomeCoords.fromBlock(key.getMinZ());
                        final Biome[][][] result = new Biome[4][4][4];
                        for (int x = startX; x < startX + 4; x++) {
                            for (int y = startY; y < startY + 4; y++) {
                                for (int z = startZ; z < startZ + 4; z++) {
                                    result[x - startX][y - startY][z - startZ] = delegate.apply(x, y, z, multiNoiseSampler);
                                    Preconditions.checkNotNull(result[x - startX][y - startY][z - startZ]);
                                }
                            }
                        }
                        return result;
                    }
                });
    }

    private LoadingCache<ChunkSectionPos, Biome[][][]> getCache(MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        return this.biomeCaches.computeIfAbsent(multiNoiseSampler, this::createCache);
    }

    private Biome[][][] getCachedBiome(ChunkSectionPos chunkSectionPos, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        return this.threadLocalCache.get()
                .computeIfAbsent(multiNoiseSampler, unused -> new WeakHashMap<>())
                .computeIfAbsent(chunkSectionPos, getCache(multiNoiseSampler));
    }

    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler, boolean fast) {
        final ChunkSectionPos chunkPos = ChunkSectionPos.from(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeY), BiomeCoords.toChunk(biomeZ));
        final int offsetX = biomeX - BiomeCoords.fromBlock(chunkPos.getMinX());
        final int offsetY = biomeY - BiomeCoords.fromBlock(chunkPos.getMinY());
        final int offsetZ = biomeZ - BiomeCoords.fromBlock(chunkPos.getMinZ());
        return getCachedBiome(chunkPos, multiNoiseSampler)[offsetX][offsetY][offsetZ];
    }

    public Biome[][][] preloadBiomes(ChunkSectionPos pos, Biome[][][] def, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (def != null) {
            getCache(multiNoiseSampler).put(pos, def);
            return def;
        } else {
            return getCachedBiome(pos, multiNoiseSampler);
        }
    }

    public interface BiomeProvider {
        Biome sample(Registry<Biome> biomeRegistry, int x, int y, int z);
    }

}
