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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

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

    public static float invokeMathHelperLerp(float delta, float start, float end) {
        return MathHelper.lerp(delta, start, end);
    }

    public static int invokeFloor(double value) {
        return MathHelper.floor(value);
    }

}
