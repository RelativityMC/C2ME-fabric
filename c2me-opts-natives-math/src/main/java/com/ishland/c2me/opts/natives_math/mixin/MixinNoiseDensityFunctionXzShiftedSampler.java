/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.base.mixin.access.ISampleBuffer;
import com.ishland.c2me.opts.natives_math.common.ducks.SampleBufferExtension;
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

@Mixin(NoiseDensityFunction.XzShiftedSampler.class)
@Implements({@Interface(iface = Sampler.class, prefix = "c2me$i$")})
public abstract class MixinNoiseDensityFunctionXzShiftedSampler implements Sampler {

    @Shadow
    @Final
    private Sampler shiftX;

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
                 final PooledSampleBuffer shiftZBuffer = context.allocateBufferFor(region)) {
                this.shiftX.fill(context, shiftXBuffer, region);
                this.shiftZ.fill(context, shiftZBuffer, region);

                output.fill(0.0F);

//                SampleBuffer vanillaBuffer = SampleBuffer.withCount(output.count());
//                this.fill(context, vanillaBuffer, region);

                shiftedBatchingCapableNoise.c2me$fillShifted(
                        output, region,
                        this.xzScale, this.yScale,
                        shiftXBuffer, null, shiftZBuffer,
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
