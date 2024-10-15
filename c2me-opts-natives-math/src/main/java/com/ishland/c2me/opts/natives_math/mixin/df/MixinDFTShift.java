package com.ishland.c2me.opts.natives_math.mixin.df;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.ducks.INativePointer;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;

@Mixin(DensityFunctionTypes.Shift.class)
public abstract class MixinDFTShift implements DensityFunctionTypes.Offset {

    @Shadow @Final private DensityFunction.Noise offsetNoise;

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        DoublePerlinNoiseSampler noise = this.offsetNoise.noise();
        if (noise == null) {
            Arrays.fill(densities, 0.0);
            return;
        }
        long ptr = ((INativePointer) noise).c2me$getPointer();
        if (ptr == 0L) {
            applier.fill(densities, this);
            return;
        }
        double[] x = new double[densities.length];
        double[] y = new double[densities.length];
        double[] z = new double[densities.length];
        for (int i = 0; i < densities.length; i++) {
            DensityFunction.NoisePos pos = applier.at(i);
            x[i] = pos.blockX() * 0.25;
            y[i] = pos.blockY() * 0.25;
            z[i] = pos.blockZ() * 0.25;
        }
        Bindings.c2me_natives_noise_perlin_double_batch(
                ptr,
                MemorySegment.ofArray(densities),
                MemorySegment.ofArray(x),
                MemorySegment.ofArray(y),
                MemorySegment.ofArray(z),
                densities.length
        );
        for (int i = 0; i < densities.length; i++) {
            densities[i] *= 4.0;
        }
    }

}
