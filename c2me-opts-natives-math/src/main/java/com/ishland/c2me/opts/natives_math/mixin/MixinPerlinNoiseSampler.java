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

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.ducks.LatticedNoiseSamplerExtension;
import com.ishland.c2me.opts.natives_math.common.ducks.SampleBufferExtension;
import com.ishland.c2me.opts.natives_math.common.ducks.ShiftedBatchingCapableNoise;
import com.ishland.flowsched.util.Assertions;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.foreign.MemorySegment;

@Mixin(value = PerlinNoiseSampler.class, priority = 1100)
@Implements({@Interface(iface = NoiseSampler.class, prefix = "c2me$i$")})
public abstract class MixinPerlinNoiseSampler extends LatticedNoiseSampler implements LatticedNoiseSamplerExtension, ShiftedBatchingCapableNoise {

    @Shadow
    public abstract float sample(double x, double y, double z);

    protected MixinPerlinNoiseSampler(Random random) {
        super(random);
    }

    @Intrinsic(displace = true)
    public void c2me$i$fill(final SampleBuffer buf, final SamplingRegion region, final double scaleXz, final double scaleY, final float outputScale) {
        Assertions.assertTrue(buf.count() == region.sizeX() * region.sizeY() * region.sizeZ(), "Invalid buf for region");

        if (buf.count() == 1) {
            buf.add(
                    0,
                    this.sample(
                            region.translateX(0) * scaleXz,
                            region.translateY(0) * scaleY,
                            region.translateZ(0) * scaleXz
                    ) * outputScale
            );
            return;
        }
        if (buf.count() < 8) {
            this.fill(buf, region, scaleXz, scaleY, outputScale);
            return;
        }

        Bindings.c2me_natives_noise_perlin_sample_base_area(
                this.c2me$getFusedPermutationTableForNative(),
                this.originX,
                this.originY,
                this.originZ,
                ((SampleBufferExtension) buf).c2me$getHeapBackedMemorySegment(),
                region.sizeX(),
                region.sizeY(),
                region.sizeZ(),
                region.minBlockX(),
                region.minBlockY(),
                region.minBlockZ(),
                region.stepBlockX(),
                region.stepBlockY(),
                region.stepBlockZ(),
                scaleXz,
                scaleY,
                outputScale
        );
    }

    @Override
    public void c2me$fillShifted(SampleBuffer buf, SamplingRegion region, double preScaleXz, double preScaleY, SampleBuffer shiftX, SampleBuffer shiftY, SampleBuffer shiftZ, double scaleXz, double scaleY, float outputScale) {
        int volume = region.getVolume();
        Assertions.assertTrue(buf.count() == volume, "Invalid buf for region");
        Assertions.assertTrue(shiftX.count() == volume, "Invalid buf for shiftX");
        if (shiftY != null) {
            Assertions.assertTrue(shiftY.count() == volume, "Invalid buf for shiftY");
        }
        Assertions.assertTrue(shiftZ.count() == volume, "Invalid buf for shiftZ");

        if (buf.count() < 1) {
            return;
        }

        Bindings.c2me_natives_noise_perlin_sample_shifted_base_area(
                this.c2me$getFusedPermutationTableForNative(),
                this.originX,
                this.originY,
                this.originZ,
                ((SampleBufferExtension) buf).c2me$getHeapBackedMemorySegment(),
                region.sizeX(),
                region.sizeY(),
                region.sizeZ(),
                region.minBlockX(),
                region.minBlockY(),
                region.minBlockZ(),
                region.stepBlockX(),
                region.stepBlockY(),
                region.stepBlockZ(),
                preScaleXz,
                preScaleY,
                ((SampleBufferExtension) shiftX).c2me$getHeapBackedMemorySegment(),
                shiftY != null ? ((SampleBufferExtension) shiftY).c2me$getHeapBackedMemorySegment() : MemorySegment.NULL,
                ((SampleBufferExtension) shiftZ).c2me$getHeapBackedMemorySegment(),
                scaleXz,
                scaleY,
                outputScale
        );

    }
}
