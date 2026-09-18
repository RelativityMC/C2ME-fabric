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

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.ducks.LatticedNoiseSamplerExtension;
import com.ishland.c2me.opts.natives_math.common.ducks.SampleBufferExtension;
import com.ishland.flowsched.util.Assertions;
import net.minecraft.util.math.noise.LegacyPerlinNoiseSampler;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.foreign.MemorySegment;

@SuppressWarnings("deprecation")
@Mixin(LegacyPerlinNoiseSampler.class)
@Implements({@Interface(iface = NoiseSampler.class, prefix = "c2me$i$")})
public abstract class MixinLegacyPerlinNoiseSampler extends PerlinNoiseSampler implements LatticedNoiseSamplerExtension {

    @Shadow
    @Final
    private double offsetScale;

    public MixinLegacyPerlinNoiseSampler(Random random) {
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

        Bindings.c2me_natives_noise_perlin_sample_legacy_area(
                this.c2me$getPackedPermutationsMemorySegment(),
                this.originX,
                this.originY,
                this.originZ,
                this.offsetScale,
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
                MemorySegment.NULL,
                MemorySegment.NULL,
                MemorySegment.NULL,
                scaleXz,
                scaleY,
                outputScale
        );
    }

}
