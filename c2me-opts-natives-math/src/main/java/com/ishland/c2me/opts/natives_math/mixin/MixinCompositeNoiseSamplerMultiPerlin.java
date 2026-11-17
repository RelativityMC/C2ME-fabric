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

import com.ishland.c2me.opts.natives_math.common.ducks.ShiftedBatchingCapableNoise;
import net.minecraft.util.math.noise.CompositeNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CompositeNoiseSampler.MultiPerlin.class)
public abstract class MixinCompositeNoiseSamplerMultiPerlin extends CompositeNoiseSampler implements ShiftedBatchingCapableNoise {

    public MixinCompositeNoiseSamplerMultiPerlin(Component[] components) {
        super(components);
    }

    @Override
    public void c2me$fillShifted(SampleBuffer buf, SamplingRegion region, double preScaleXz, double preScaleY, SampleBuffer shiftX, SampleBuffer shiftY, SampleBuffer shiftZ, double scaleXz, double scaleY, float outputScale) {
        for (CompositeNoiseSampler.Component layer : this.components) {
            double frequency = layer.frequency();
            ((ShiftedBatchingCapableNoise) layer.noise()).c2me$fillShifted(buf, region, preScaleXz, preScaleY, shiftX, shiftY, shiftZ, scaleXz * frequency, scaleY * frequency, outputScale * layer.amplitude());
        }
    }

}
