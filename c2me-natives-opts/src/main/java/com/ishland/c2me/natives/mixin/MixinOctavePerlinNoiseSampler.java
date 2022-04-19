package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.base.mixin.access.IPerlinNoiseSampler;
import com.ishland.c2me.natives.common.Cleaners;
import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.NativesStruct;
import com.ishland.c2me.natives.common.UnsafeUtil;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OctavePerlinNoiseSampler.class, priority = 1200)
public class MixinOctavePerlinNoiseSampler implements NativesStruct {

    @Shadow @Final private double lacunarity;

    @Shadow @Final private double persistence;

    @Shadow @Final private PerlinNoiseSampler[] octaveSamplers;

    @Shadow @Final private DoubleList amplitudes;

    private long octaveSamplerDataPointer = 0L;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        final int size = this.octaveSamplers.length;
        final long ptr_indexes = UnsafeUtil.getInstance().allocateMemory(size * 8L);
        final long ptr_sampler_permutations = UnsafeUtil.getInstance().allocateMemory(size * 256L);
        final long ptr_sampler_originX = UnsafeUtil.getInstance().allocateMemory(size * 8L);
        final long ptr_sampler_originY = UnsafeUtil.getInstance().allocateMemory(size * 8L);
        final long ptr_sampler_originZ = UnsafeUtil.getInstance().allocateMemory(size * 8L);
        final long ptr_amplitudes = UnsafeUtil.getInstance().allocateMemory(size * 8L);
        int pos = 0;
        for (int i = 0; i < size; i ++) {
            final PerlinNoiseSampler sampler = this.octaveSamplers[i];
            if (sampler != null) {
                UnsafeUtil.getInstance().putLong(ptr_indexes + pos * 8L, i);
                PlatformDependent.copyMemory(((IPerlinNoiseSampler) sampler).getPermutations(), 0, ptr_sampler_permutations + 256L * pos, 256);
                UnsafeUtil.getInstance().putDouble(ptr_sampler_originX + pos * 8L, sampler.originX);
                //noinspection SuspiciousNameCombination
                UnsafeUtil.getInstance().putDouble(ptr_sampler_originY + pos * 8L, sampler.originY);
                UnsafeUtil.getInstance().putDouble(ptr_sampler_originZ + pos * 8L, sampler.originZ);
                UnsafeUtil.getInstance().putDouble(ptr_amplitudes + pos * 8L, this.amplitudes.getDouble(i));
                pos ++;
            }
        }
        this.octaveSamplerDataPointer = NativesInterface.createOctaveSamplerData(
                this.lacunarity,
                this.persistence,
                pos,
                ptr_indexes,
                ptr_sampler_permutations,
                ptr_sampler_originX,
                ptr_sampler_originY,
                ptr_sampler_originZ,
                ptr_amplitudes
        );
        Cleaners.register(this,
                this.octaveSamplerDataPointer,
                ptr_indexes,
                ptr_sampler_permutations,
                ptr_sampler_originX,
                ptr_sampler_originY,
                ptr_sampler_originZ,
                ptr_amplitudes);
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(double x, double y, double z) {
        return NativesInterface.sampleOctave(octaveSamplerDataPointer, x, y, z);
    }

    @Override
    public long getNativePointer() {
        return this.octaveSamplerDataPointer;
    }
}
