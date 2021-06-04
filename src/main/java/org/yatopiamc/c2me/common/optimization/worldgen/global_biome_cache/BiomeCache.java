package org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import org.threadly.concurrent.UnfairExecutor;
import org.yatopiamc.c2me.common.util.IBiomeArray;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class BiomeCache {

    public static final UnfairExecutor EXECUTOR = new UnfairExecutor(2, new ThreadFactoryBuilder().setNameFormat("C2ME biomes #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build());

    private final Registry<Biome> registry;
    private final List<Biome> biomes;

    private final UncachedBiomeSource uncachedBiomeSource;

    public BiomeCache(ThreadLocal<BiomeLayerSampler> sampler, Registry<Biome> registry, List<Biome> biomes) {
        this.registry = registry;
        this.biomes = biomes;
        this.uncachedBiomeSource = new UncachedBiomeSource(biomes, sampler, registry);
    }

    private final LoadingCache<ChunkSectionPos, BiomeArray> biomeCache = CacheBuilder.newBuilder()
            .weakKeys()
            .softValues()
            .maximumSize(8192)
            .build(new CacheLoader<>() {
                @Override
                public BiomeArray load(ChunkSectionPos key) {
                    return new BiomeArray(registry, new HeightViewFromSectionPos(key), key.toChunkPos(), uncachedBiomeSource);
                }
            });

    private final ThreadLocal<WeakHashMap<ChunkSectionPos, BiomeArray>> threadLocalCache = ThreadLocal.withInitial(WeakHashMap::new);

    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        final ChunkSectionPos chunkPos = ChunkSectionPos.from(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeY), BiomeCoords.toChunk(biomeZ));
        final int startX = BiomeCoords.fromBlock(chunkPos.getMinX());
        final int startZ = BiomeCoords.fromBlock(chunkPos.getMinZ());
        return threadLocalCache.get().computeIfAbsent(chunkPos, biomeCache).getBiomeForNoiseGen(biomeX - startX, biomeY, biomeZ - startZ);
    }

    public BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def) {
        Preconditions.checkNotNull(view);
        Map<Integer, BiomeArray> subArrays = new Object2ObjectOpenHashMap<>();
        for (int y = view.getBottomSectionCoord(); y <= view.getTopSectionCoord(); y ++) {
            final ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(pos, y);
            if (def != null && ((IBiomeArray) def).isWithinRange(BiomeCoords.fromChunk(y))) {
                biomeCache.put(chunkSectionPos, def);
                subArrays.put(y, def);
            } else {
                subArrays.put(y, biomeCache.getUnchecked(chunkSectionPos));
            }
        }
        return new BiomeArray(registry, view, new ChunkPos(0, 0), new BiomeSource(biomes) {
            @Override
            protected Codec<? extends BiomeSource> getCodec() {
                return VanillaLayeredBiomeSource.CODEC;
            }

            @Override
            public BiomeSource withSeed(long seed) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
                return subArrays.get(BiomeCoords.toChunk(biomeY)).getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
            }
        });
    }

    private record HeightViewFromSectionPos(ChunkSectionPos pos) implements HeightLimitView {

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public int getBottomY() {
            return pos().getMinY();
        }

        @Override
        public int hashCode() {
            return pos().hashCode();
        }
    }

}
