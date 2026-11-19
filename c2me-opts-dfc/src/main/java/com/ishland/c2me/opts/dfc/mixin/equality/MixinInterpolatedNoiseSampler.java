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
