package com.ishland.c2me.mixin.optimization.worldgen.vanilla_optimization.the_end_biome_cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TheEndBiomeSource.class)
public abstract class MixinTheEndBiomeSource {

    @Shadow @Final private Biome centerBiome;

    @Shadow
    public static float getNoiseAt(SimplexNoiseSampler simplexNoiseSampler, int i, int j) {
        return 0;
    }

    @Shadow @Final private SimplexNoiseSampler noise;

    @Shadow @Final private Biome highlandsBiome;

    @Shadow @Final private Biome midlandsBiome;

    @Shadow @Final private Biome smallIslandsBiome;

    @Shadow @Final private Biome barrensBiome;

    private Biome getBiomeForNoiseGenVanilla(int biomeX, int biomeY, int biomeZ) {
        // TODO [VanillaCopy]
        int i = biomeX >> 2;
        int j = biomeZ >> 2;
        if ((long)i * (long)i + (long)j * (long)j <= 4096L) {
            return this.centerBiome;
        } else {
            float f = getNoiseAt(this.noise, i * 2 + 1, j * 2 + 1);
            if (f > 40.0F) {
                return this.highlandsBiome;
            } else if (f >= 0.0F) {
                return this.midlandsBiome;
            } else {
                return f < -20.0F ? this.smallIslandsBiome : this.barrensBiome;
            }
        }
    }

    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Biome>> cache = ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    private final int cacheCapacity = 1024;

    /**
     * @author ishland
     * @reason the end biome cache
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        final long key = ChunkPos.toLong(biomeX, biomeZ);
        final Long2ObjectLinkedOpenHashMap<Biome> cacheThreadLocal = cache.get();
        final Biome biome = cacheThreadLocal.get(key);
        if (biome != null) {
            return biome;
        } else {
            final Biome gennedBiome = getBiomeForNoiseGenVanilla(biomeX, biomeY, biomeZ);
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
