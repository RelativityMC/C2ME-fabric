package com.ishland.c2me.opts.natives_math.common;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public class Bindings {

    private static MethodHandle bind(MethodHandle template, String prefix) {
        return template.bindTo(NativeLoader.lookup.find(prefix + NativeLoader.currentMachineTarget.getSuffix()).get());
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_double_octave_sample = bind(BindingsTemplate.c2me_natives_noise_perlin_double_octave_sample, "c2me_natives_noise_perlin_double_octave_sample");
    private static final MethodHandle MH_c2me_natives_noise_perlin_double_octave_sample_ptr = bind(BindingsTemplate.c2me_natives_noise_perlin_double_octave_sample_ptr, "c2me_natives_noise_perlin_double_octave_sample");

    public static double c2me_natives_noise_perlin_double_octave_sample(MemorySegment data, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_perlin_double_octave_sample.invokeExact(data, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static double c2me_natives_noise_perlin_double_octave_sample(long data_ptr, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_perlin_double_octave_sample_ptr.invokeExact(data_ptr, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_interpolated_sample = bind(BindingsTemplate.c2me_natives_noise_perlin_interpolated_sample, "c2me_natives_noise_perlin_interpolated_sample");
    private static final MethodHandle MH_c2me_natives_noise_perlin_interpolated_sample_ptr = bind(BindingsTemplate.c2me_natives_noise_perlin_interpolated_sample_ptr, "c2me_natives_noise_perlin_interpolated_sample");

    public static double c2me_natives_noise_perlin_interpolated_sample(MemorySegment data, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_perlin_interpolated_sample.invokeExact(data, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static double c2me_natives_noise_perlin_interpolated_sample(long data_ptr, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_perlin_interpolated_sample_ptr.invokeExact(data_ptr, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle MH_c2me_natives_end_islands_sample = bind(BindingsTemplate.c2me_natives_end_islands_sample, "c2me_natives_end_islands_sample");
    private static final MethodHandle MH_c2me_natives_end_islands_sample_ptr = bind(BindingsTemplate.c2me_natives_end_islands_sample_ptr, "c2me_natives_end_islands_sample");

    public static float c2me_natives_end_islands_sample(MemorySegment data, int x, int z) {
        try {
            return (float) MH_c2me_natives_end_islands_sample.invokeExact(data, x, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float c2me_natives_end_islands_sample(long data_ptr, int x, int z) {
        try {
            return (float) MH_c2me_natives_end_islands_sample_ptr.invokeExact(data_ptr, x, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
