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

package com.ishland.c2me.opts.dfc.mixin.equality;

import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(InterpolatedNoiseSampler.class)
public class MixinInterpolatedNoiseSampler {

    @Shadow @Final private OctavePerlinNoiseSampler lowerInterpolatedNoise;

    @Shadow @Final private OctavePerlinNoiseSampler upperInterpolatedNoise;

    @Shadow @Final private OctavePerlinNoiseSampler interpolationNoise;

    @Shadow @Final private double scaledXzScale;

    @Shadow @Final private double scaledYScale;

    @Shadow @Final private double xzFactor;

    @Shadow @Final private double yFactor;

    @Shadow @Final private double smearScaleMultiplier;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinInterpolatedNoiseSampler that = (MixinInterpolatedNoiseSampler) object;
        return Double.compare(scaledXzScale, that.scaledXzScale) == 0 && Double.compare(scaledYScale, that.scaledYScale) == 0 && Double.compare(xzFactor, that.xzFactor) == 0 && Double.compare(yFactor, that.yFactor) == 0 && Double.compare(smearScaleMultiplier, that.smearScaleMultiplier) == 0 && Objects.equals(lowerInterpolatedNoise, that.lowerInterpolatedNoise) && Objects.equals(upperInterpolatedNoise, that.upperInterpolatedNoise) && Objects.equals(interpolationNoise, that.interpolationNoise);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(lowerInterpolatedNoise);
        result = 31 * result + Objects.hashCode(upperInterpolatedNoise);
        result = 31 * result + Objects.hashCode(interpolationNoise);
        result = 31 * result + Double.hashCode(scaledXzScale);
        result = 31 * result + Double.hashCode(scaledYScale);
        result = 31 * result + Double.hashCode(xzFactor);
        result = 31 * result + Double.hashCode(yFactor);
        result = 31 * result + Double.hashCode(smearScaleMultiplier);
        return result;
    }
}
