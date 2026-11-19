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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.ishland.c2me.base.common.integration.lithostitched.FNLBindings;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;

/**
 * All called from generated code
 */
public class InvocationShim {

    public static double invokeDensityFunctionSample(DensityFunction densityFunction, DensityFunction.NoisePos pos) {
        return densityFunction.sample(pos);
    }

    public static void invokeDensityFunctionFill(DensityFunction densityFunction, double[] densities, DensityFunction.EachApplier applier) {
        densityFunction.fill(densities, applier);
    }

    public static double invokeMathHelperClampedMap(double value, double oldStart, double oldEnd, double newStart, double newEnd) {
        return MathHelper.clampedMap(value, oldStart, oldEnd, newStart, newEnd);
    }

    public static double invokeDensityFunctionNoiseSample(DensityFunction.Noise noise, double x, double y, double z) {
        return noise.sample(x, y, z);
    }

    public static double invokeFastNoiseConfigSample(Object config, double x, double y, double z) {
        return FNLBindings.call_FastNoiseConfig$sample(config, x, y, z);
    }

    public static float invokeMathHelperLerp(float delta, float start, float end) {
        return MathHelper.lerp(delta, start, end);
    }

    public static int invokeFloor(double value) {
        return MathHelper.floor(value);
    }

}
