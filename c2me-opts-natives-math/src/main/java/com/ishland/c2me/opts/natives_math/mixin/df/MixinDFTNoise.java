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

@Mixin(DensityFunctionTypes.Noise.class)
public abstract class MixinDFTNoise {

    @Shadow @Final private DensityFunction.Noise noise;

    @Shadow @Deprecated public abstract double xzScale();

    @Shadow public abstract double yScale();

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        DoublePerlinNoiseSampler noise = this.noise.noise();
        if (noise == null) {
            Arrays.fill(densities, 0.0);
            return;
        }
        long ptr = ((INativePointer) noise).c2me$getPointer();
        if (ptr == 0L) {
            applier.fill(densities, (DensityFunction) this);
            return;
        }
        double[] x = new double[densities.length];
        double[] y = new double[densities.length];
        double[] z = new double[densities.length];
        for (int i = 0; i < densities.length; i++) {
            DensityFunction.NoisePos pos = applier.at(i);
            x[i] = pos.blockX() * this.xzScale();
            y[i] = pos.blockY() * this.yScale();
            z[i] = pos.blockZ() * this.xzScale();
        }
        Bindings.c2me_natives_noise_perlin_double_batch(
                ptr,
                MemorySegment.ofArray(densities),
                MemorySegment.ofArray(x),
                MemorySegment.ofArray(y),
                MemorySegment.ofArray(z),
                densities.length
        );
    }

}
