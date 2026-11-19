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

package com.ishland.c2me.opts.natives_math.mixin.df;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.ducks.INativePointer;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;

@Mixin(DensityFunctionTypes.ShiftB.class)
public abstract class MixinDFTShiftB implements DensityFunctionTypes.Offset {

    @Shadow @Final private Noise offsetNoise;

    @Override
    public void fill(double[] densities, EachApplier applier) {
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
            NoisePos pos = applier.at(i);
            x[i] = pos.blockZ() * 0.25;
            y[i] = pos.blockX() * 0.25;
            z[i] = 0.0;
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
