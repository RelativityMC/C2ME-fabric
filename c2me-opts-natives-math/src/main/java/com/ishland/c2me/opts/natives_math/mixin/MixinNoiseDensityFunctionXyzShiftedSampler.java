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

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.base.mixin.access.ISampleBuffer;
import com.ishland.c2me.opts.natives_math.common.ducks.ShiftedBatchingCapableNoise;
import com.ishland.flowsched.util.Assertions;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.world.gen.densityfunction.NoiseDensityFunction;
import net.minecraft.world.gen.sampler.PooledSampleBuffer;
import net.minecraft.world.gen.sampler.SampleBuffer;
import net.minecraft.world.gen.sampler.Sampler;
import net.minecraft.world.gen.sampler.SamplingContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(NoiseDensityFunction.XyzShiftedSampler.class)
@Implements({@Interface(iface = Sampler.class, prefix = "c2me$i$")})
public abstract class MixinNoiseDensityFunctionXyzShiftedSampler implements Sampler {

    @Shadow
    @Final
    private Sampler shiftX;

    @Shadow
    @Final
    private Sampler shiftY;

    @Shadow
    @Final
    private Sampler shiftZ;

    @Shadow
    @Final
    private double xzScale;

    @Shadow
    @Final
    private double yScale;

    @Shadow
    @Final
    private NoiseSampler noise;

    @Shadow
    public abstract float sample(SamplingContext context, int x, int y, int z);

    @Intrinsic(displace = true)
    public void c2me$i$fill(final SamplingContext context, final SampleBuffer output, final SamplingRegion region) {
        Assertions.assertTrue(output.count() == region.sizeX() * region.sizeY() * region.sizeZ(), "Invalid buf for region");

        if (output.count() == 1) {
            output.set(
                    0,
                    this.sample(
                            context,
                            region.translateX(0),
                            region.translateY(0),
                            region.translateZ(0)
                    )
            );
            return;
        }
        if (output.count() >= 8 && this.noise instanceof ShiftedBatchingCapableNoise shiftedBatchingCapableNoise) {
            try (final PooledSampleBuffer shiftXBuffer = context.allocateBufferFor(region);
                 final PooledSampleBuffer shiftYBuffer = context.allocateBufferFor(region);
                 final PooledSampleBuffer shiftZBuffer = context.allocateBufferFor(region)) {
                this.shiftX.fill(context, shiftXBuffer, region);
                this.shiftY.fill(context, shiftYBuffer, region);
                this.shiftZ.fill(context, shiftZBuffer, region);

                output.fill(0.0F);

//                SampleBuffer vanillaBuffer = SampleBuffer.withCount(output.count());
//                this.fill(context, vanillaBuffer, region);

                shiftedBatchingCapableNoise.c2me$fillShifted(
                        output, region,
                        this.xzScale, this.yScale,
                        shiftXBuffer, shiftYBuffer, shiftZBuffer,
                        1.0, 1.0, 1.0F
                );

//                if (!Arrays.equals(
//                        ((ISampleBuffer) vanillaBuffer).getElems(), 0, output.count(),
//                        ((ISampleBuffer) output).getElems(), 0, output.count()
//                )) {
//                    throw new AssertionError();
//                }
            }
        } else {
            this.fill(context, output, region);
        }
    }

}
