package com.ishland.c2me.opts.worldgen.vanilla.mixin.the_end_biome_cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TheEndBiomeSource.class)
public abstract class MixinTheEndBiomeSource {

    @Shadow @Final private SimplexNoiseSampler noise;

    @Shadow @Final private RegistryEntry<Biome> highlandsBiome;

    @Shadow @Final private RegistryEntry<Biome> midlandsBiome;

    @Shadow @Final private RegistryEntry<Biome> smallIslandsBiome;

    @Shadow @Final private RegistryEntry<Biome> barrensBiome;

    @Shadow @Final private RegistryEntry<Biome> centerBiome;

    private RegistryEntry<Biome> getBiomeForNoiseGenVanilla(int biomeX, int biomeY, int biomeZ) {
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

    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<RegistryEntry<Biome>>> cacheInstanced = ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    private final int cacheCapacity = 1024;

    /**
     * @author ishland
     * @reason the end biome cache
     */
    @Overwrite
    public RegistryEntry<Biome> getBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        final long key = ChunkPos.toLong(biomeX, biomeZ);
        final Long2ObjectLinkedOpenHashMap<RegistryEntry<Biome>> cacheThreadLocal = cacheInstanced.get();
        final RegistryEntry<Biome> biome = cacheThreadLocal.get(key);
        if (biome != null) {
            return biome;
        } else {
            final RegistryEntry<Biome> gennedBiome = getBiomeForNoiseGenVanilla(biomeX, biomeY, biomeZ);
            cacheThreadLocal.put(key, gennedBiome);
            if (cacheThreadLocal.size() > cacheCapacity) {
                for (int i = 0; i < cacheCapacity / 16; i ++) {
                    cacheThreadLocal.removeFirst();
                }
            }
            return gennedBiome;
        }
    }

    /**
     * @author ishland
     * @reason caching
     */
    @Overwrite
    public static float getNoiseAt(SimplexNoiseSampler simplexNoiseSampler, int i, int j) {
        int k = i / 2;
        int l = j / 2;
        int m = i % 2;
        int n = j % 2;
        float f = 100.0F - MathHelper.sqrt((float)(i * i + j * j)) * 8.0F;
        f = MathHelper.clamp(f, -100.0F, 80.0F);

        for(int o = -12; o <= 12; ++o) {
            for(int p = -12; p <= 12; ++p) {
                long q = (long)(k + o);
                long r = (long)(l + p);
                if (q * q + r * r > 4096L && simplexNoiseSampler.sample((double)q, (double)r) < -0.9F) {
                    float g = (MathHelper.abs((float)q) * 3439.0F + MathHelper.abs((float)r) * 147.0F) % 13.0F + 9.0F;
                    float h = (float)(m - o * 2);
                    float s = (float)(n - p * 2);
                    float t = 100.0F - MathHelper.sqrt(h * h + s * s) * g;
                    t = MathHelper.clamp(t, -100.0F, 80.0F);
                    f = Math.max(f, t);
                }
            }
        }

        return f;
    }

}
