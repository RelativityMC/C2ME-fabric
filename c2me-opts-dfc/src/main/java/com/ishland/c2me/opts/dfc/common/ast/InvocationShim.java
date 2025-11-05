package com.ishland.c2me.opts.dfc.common.ast;

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
