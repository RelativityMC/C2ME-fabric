package com.ishland.c2me.opts.worldgen.vanilla.mixin.the_end_biome_cache;

import it.unimi.dsi.fastutil.longs.Long2DoubleLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.EndIslands.class)
public class MixinDensityFunctionTypesEndIslands {

    @Shadow
    @Final
    private SimplexNoiseSampler field_36554;

    private final ThreadLocal<Long2DoubleLinkedOpenHashMap> cacheInstanced = ThreadLocal.withInitial(() -> {
        final Long2DoubleLinkedOpenHashMap map = new Long2DoubleLinkedOpenHashMap();
        map.defaultReturnValue(Double.NaN);
        return map;
    });
    private final int cacheCapacity = 1024;

    private double sampleVanilla(int x, int z) {
        return ((double) TheEndBiomeSource.getNoiseAt(this.field_36554, x, z) - 8.0) / 128.0;
    }

    /**
     * @author ishland
     * @reason caching
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        int x = pos.blockX() / 8;
        int z = pos.blockZ() / 8;

        final long key = ChunkPos.toLong(x, z);
        final Long2DoubleLinkedOpenHashMap cacheThreadLocal = cacheInstanced.get();
        final double cached = cacheThreadLocal.get(key);
        if (!Double.isNaN(cached)) {
            return cached;
        } else {
            final double uncached = sampleVanilla(x, z);
            cacheThreadLocal.put(key, uncached);
            if (cacheThreadLocal.size() > cacheCapacity) {
                for (int i = 0; i < cacheCapacity / 16; i ++) {
                    cacheThreadLocal.removeFirstDouble();
                }
            }
            return uncached;
        }
    }

}
