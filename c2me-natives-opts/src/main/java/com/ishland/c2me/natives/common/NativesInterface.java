package com.ishland.c2me.natives.common;

import io.netty.util.internal.PlatformDependent;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.SymbolLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.CLinker.C_DOUBLE;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;

public class NativesInterface {

    private static final CLinker LINKER = CLinker.getInstance();
    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    private static final MethodHandle SAMPLE;
    private static final MethodHandle GENERATE_PERMUTATIONS;
    private static final MethodHandle CREATE_OCTAVE_SAMPLER_DATA;
    private static final MethodHandle OCTAVE_SAMPLE;
    private static final MethodHandle CREATE_INTERPOLATED_SAMPLER_DATA;
    private static final MethodHandle INTERPOLATED_SAMPLE;
    private static final MethodHandle DOUBLE_SAMPLE;

    static {

        SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_sample").get(),
                MethodType.methodType(double.class, long.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE)
        );

        GENERATE_PERMUTATIONS = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_generatePermutations").get(),
                MethodType.methodType(long.class),
                FunctionDescriptor.of(C_LONG_LONG));

        // octave_sampler_data *c2me_natives_create_octave_sampler_data(
        //    double lacunarity, double persistence, size_t size, bool *notNull, __uint8_t *sampler_permutations,
        //    double *sampler_originX, double *sampler_originY, double *sampler_originZ, double *amplitudes)

        CREATE_OCTAVE_SAMPLER_DATA = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_create_octave_sampler_data").get(),
                MethodType.methodType(long.class, double.class, double.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class),
                FunctionDescriptor.of(C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG)
        );

        // double c2me_natives_octave_sample(octave_sampler_data *data, double x, double y, double z)

        OCTAVE_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_octave_sample").get(),
                MethodType.methodType(double.class, long.class, double.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE)
        );

        // interpolated_sampler_data *c2me_natives_create_interpolated_sampler_data(
        //    octave_sampler_data *lowerInterpolatedNoise, octave_sampler_data *upperInterpolatedNoise, octave_sampler_data *interpolationNoise,
        //    double xzScale, double yScale, double xzMainScale, double yMainScale, int cellWidth, int cellHeight)

        CREATE_INTERPOLATED_SAMPLER_DATA = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_create_interpolated_sampler_data").get(),
                MethodType.methodType(long.class, long.class, long.class, long.class, double.class, double.class, double.class, double.class, int.class, int.class),
                FunctionDescriptor.of(C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_INT, C_INT)
        );

        // double c2me_natives_interpolated_sample(interpolated_sampler_data *data, int x, int y, int z)

        INTERPOLATED_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_interpolated_sample").get(),
                MethodType.methodType(double.class, long.class, int.class, int.class, int.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_INT, C_INT, C_INT)
        );

        // double c2me_natives_double_sample(
        //    octave_sampler_data *firstSampler, octave_sampler_data *secondSampler,
        //    double x, double y, double z, double amplitude)

        DOUBLE_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_double_sample").get(),
                MethodType.methodType(double.class, long.class, long.class, double.class, double.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE)
        );
    }

    public static double sample(long permutations, double originX, double originY, double originZ, double x, double y, double z, double yScale, double yMax) {
        if (permutations == 0) throw new NullPointerException();
        try {
            return (double) SAMPLE.invoke(permutations, originX, originY, originZ, x, y, z, yScale, yMax);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long generatePermutations() {
        try {
            return (long) GENERATE_PERMUTATIONS.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long createOctaveSamplerData(double lacunarity, double persistence, long size, long ptr_notNull, long ptr_sampler_permutations,
                                               long ptr_sampler_originX, long ptr_sampler_originY, long ptr_sampler_originZ, long ptr_amplitudes) {
        if (ptr_notNull == 0) throw new NullPointerException();
        if (ptr_sampler_permutations == 0) throw new NullPointerException();
        if (ptr_sampler_originX == 0) throw new NullPointerException();
        if (ptr_sampler_originY == 0) throw new NullPointerException();
        if (ptr_sampler_originZ == 0) throw new NullPointerException();
        if (ptr_amplitudes == 0) throw new NullPointerException();
        try {
            return (long) CREATE_OCTAVE_SAMPLER_DATA.invoke(lacunarity, persistence, size, ptr_notNull, ptr_sampler_permutations, ptr_sampler_originX, ptr_sampler_originY, ptr_sampler_originZ, ptr_amplitudes);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double sampleOctave(long ptr_octaveSamplerData, double x, double y, double z) {
        if (ptr_octaveSamplerData == 0) throw new NullPointerException();
        try {
            return (double) OCTAVE_SAMPLE.invoke(ptr_octaveSamplerData, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long createInterpolatedSamplerData(long ptr_lowerInterpolatedNoise, long ptr_upperInterpolatedNoise, long ptr_interpolationNoise,
                                                     double xzScale, double yScale, double xzMainScale, double yMainScale, int cellWidth, int cellHeight) {
        if (ptr_interpolationNoise == 0) throw new NullPointerException();
        if (ptr_lowerInterpolatedNoise == 0) throw new NullPointerException();
        if (ptr_upperInterpolatedNoise == 0) throw new NullPointerException();
        try {
            return (long) CREATE_INTERPOLATED_SAMPLER_DATA.invoke(ptr_lowerInterpolatedNoise, ptr_upperInterpolatedNoise, ptr_interpolationNoise, xzScale, yScale, xzMainScale, yMainScale, cellWidth, cellHeight);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double sampleInterpolated(long ptr_interpolatedSamplerData, int x, int y, int z) {
        if (ptr_interpolatedSamplerData == 0) throw new NullPointerException();
        try {
            return (double) INTERPOLATED_SAMPLE.invoke(ptr_interpolatedSamplerData, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double sampleDouble(long ptr_firstSampler, long ptr_secondSampler, double x, double y, double z, double amplitude) {
        if (ptr_firstSampler == 0) throw new NullPointerException();
        if (ptr_secondSampler == 0) throw new NullPointerException();
        try {
            return (double) DOUBLE_SAMPLE.invoke(ptr_firstSampler, ptr_secondSampler, x, y, z, amplitude);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void testSample() {
        final long memoryAddress = generatePermutations();

        long startTime = System.nanoTime();
        final int count = 1 << 20;
        for (int i = 0; i < count; i++) {
            sample(memoryAddress, 0, 0, 0, 40, 140, 20, 1.5, 40);
        }
        long endTime = System.nanoTime();
        System.out.println("%.2fns/op".formatted((endTime - startTime) / (double) count));

        PlatformDependent.freeMemory(memoryAddress);
    }

    public static void init() {
//        testSample();
//        testSample();
//        testSample();
//        testSample();
//        testSample();
//        testSample();
//        testSample();
//        testSample();
    }

}
