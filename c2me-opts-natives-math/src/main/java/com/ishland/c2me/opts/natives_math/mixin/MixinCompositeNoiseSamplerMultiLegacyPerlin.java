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

import com.ishland.c2me.opts.natives_math.common.ducks.ShiftedBatchingCapableNoise;
import net.minecraft.util.math.noise.CompositeNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CompositeNoiseSampler.MultiLegacyPerlin.class)
public abstract class MixinCompositeNoiseSamplerMultiLegacyPerlin extends CompositeNoiseSampler implements ShiftedBatchingCapableNoise {

    public MixinCompositeNoiseSamplerMultiLegacyPerlin(Component[] components) {
        super(components);
    }

    @Override
    public void c2me$fillShifted(SampleBuffer buf, SamplingRegion region, double preScaleXz, double preScaleY, SampleBuffer shiftX, SampleBuffer shiftY, SampleBuffer shiftZ, double scaleXz, double scaleY, float outputScale) {
        for (Component layer : this.components) {
            double frequency = layer.frequency();
            ((ShiftedBatchingCapableNoise) layer.noise()).c2me$fillShifted(buf, region, preScaleXz, preScaleY, shiftX, shiftY, shiftZ, scaleXz * frequency, scaleY * frequency, outputScale * layer.amplitude());
        }
    }

}
