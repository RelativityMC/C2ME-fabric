package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeCoords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadly.concurrent.UnfairExecutor;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class BiomeCache {

    public static final UnfairExecutor EXECUTOR = new UnfairExecutor(2, new ThreadFactoryBuilder().setNameFormat("C2ME biomes #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build());
    private static final Logger LOGGER = LogManager.getLogger("C2ME Biome Cache");

    private final Registry<Biome> registry;

    private final UncachedBiomeSource uncachedBiomeSource;

    private final AtomicReference<HeightLimitView> finalHeightLimitView = new AtomicReference<>(null);

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
                    if (finalHeightLimitView.get() == null) throw new IllegalStateException(String.format("Cannot populate non-configured biome cache %s", BiomeCache.this));
                    return new BiomeArray(registry, finalHeightLimitView.get(), key, uncachedBiomeSource);
                }
            });

    private final ThreadLocal<WeakHashMap<ChunkPos, BiomeArray>> threadLocalCache = ThreadLocal.withInitial(WeakHashMap::new);

    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ, boolean fast) {
        if (finalHeightLimitView.get() == null) {
            return uncachedBiomeSource.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        }
        final ChunkPos chunkPos = new ChunkPos(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeZ));
        final int startX = BiomeCoords.fromBlock(chunkPos.getStartX());
        final int startZ = BiomeCoords.fromBlock(chunkPos.getStartZ());
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

    public BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def) {
        Preconditions.checkNotNull(view);
        if (!finalHeightLimitView.compareAndSet(null, view)) {
            if (view.getBottomY() != finalHeightLimitView.get().getBottomY()
                    || view.getTopY() != finalHeightLimitView.get().getTopY())
                throw new IllegalArgumentException(String.format("Cannot modify %s height value : expected %d ~ %d but got %d ~ %d",
                        this, finalHeightLimitView.get().getBottomY(), finalHeightLimitView.get().getTopY(), view.getBottomY(), view.getTopY()));
        } else {
            LOGGER.info("Successfully setup {} with height: {} ~ {}", this, view.getBottomY(), view.getTopY());
        }
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
