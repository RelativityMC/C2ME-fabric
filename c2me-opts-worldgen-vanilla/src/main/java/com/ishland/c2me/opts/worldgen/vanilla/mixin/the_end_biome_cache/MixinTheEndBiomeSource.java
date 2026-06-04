/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
