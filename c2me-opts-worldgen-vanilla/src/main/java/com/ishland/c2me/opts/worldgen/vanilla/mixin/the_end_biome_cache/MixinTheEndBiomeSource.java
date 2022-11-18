package com.ishland.c2me.opts.worldgen.vanilla.mixin.the_end_biome_cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TheEndBiomeSource.class)
public abstract class MixinTheEndBiomeSource {

    @Shadow @Final private RegistryEntry<Biome> highlandsBiome;

    @Shadow @Final private RegistryEntry<Biome> midlandsBiome;

    @Shadow @Final private RegistryEntry<Biome> smallIslandsBiome;

    @Shadow @Final private RegistryEntry<Biome> barrensBiome;

    @Shadow @Final private RegistryEntry<Biome> centerBiome;

    private RegistryEntry<Biome> getBiomeForNoiseGenVanilla(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        // TODO [VanillaCopy]
        int i = BiomeCoords.toBlock(x);
        int j = BiomeCoords.toBlock(y);
        int k = BiomeCoords.toBlock(z);
        int l = ChunkSectionPos.getSectionCoord(i);
        int m = ChunkSectionPos.getSectionCoord(k);
        if ((long)l * (long)l + (long)m * (long)m <= 4096L) {
            return this.centerBiome;
        } else {
            int n = (ChunkSectionPos.getSectionCoord(i) * 2 + 1) * 8;
            int o = (ChunkSectionPos.getSectionCoord(k) * 2 + 1) * 8;
            double d = noise.erosion().sample(new DensityFunction.UnblendedNoisePos(n, j, o));
            if (d > 0.25D) {
                return this.highlandsBiome;
            } else if (d >= -0.0625D) {
                return this.midlandsBiome;
            } else {
                return d < -0.21875D ? this.smallIslandsBiome : this.barrensBiome;
            }
        }
    }

    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<RegistryEntry<Biome>>> cache = ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    private final int cacheCapacity = 1024;

    /**
     * @author ishland
     * @reason the end biome cache
     */
    @Overwrite
    public RegistryEntry<Biome> getBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        final long key = ChunkPos.toLong(biomeX, biomeZ);
        final Long2ObjectLinkedOpenHashMap<RegistryEntry<Biome>> cacheThreadLocal = cache.get();
        final RegistryEntry<Biome> biome = cacheThreadLocal.get(key);
        if (biome != null) {
            return biome;
        } else {
            final RegistryEntry<Biome> gennedBiome = getBiomeForNoiseGenVanilla(biomeX, biomeY, biomeZ, multiNoiseSampler);
            cacheThreadLocal.put(key, gennedBiome);
            if (cacheThreadLocal.size() > cacheCapacity) {
                for (int i = 0; i < cacheCapacity / 16; i ++) {
                    cacheThreadLocal.removeFirst();
                }
            }
            return gennedBiome;
        }
    }

}
