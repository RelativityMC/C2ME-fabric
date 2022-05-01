package com.ishland.c2me.natives.common;

import io.netty.util.internal.PlatformDependent;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.SymbolLookup;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.CLinker.C_CHAR;
import static jdk.incubator.foreign.CLinker.C_DOUBLE;
import static jdk.incubator.foreign.CLinker.C_FLOAT;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;

public class NativeInterface {

    private static final CLinker LINKER = CLinker.getInstance();
    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    private static final MethodHandle INIT;
    private static final MethodHandle PERLIN_SAMPLE;
    private static final MethodHandle PERLIN_GENERATE_PERMUTATIONS;
    private static final MethodHandle PERLIN_CREATE_OCTAVE_SAMPLER_DATA;
    private static final MethodHandle PERLIN_OCTAVE_SAMPLE;
    private static final MethodHandle PERLIN_CREATE_INTERPOLATED_SAMPLER_DATA;
    private static final MethodHandle PERLIN_INTERPOLATED_SAMPLE;
    private static final MethodHandle PERLIN_DOUBLE_SAMPLE;
    private static final MethodHandle SIMPLEX_SAMPLE;
    private static final MethodHandle THE_END_SAMPLE;

    // ===== Density Functions Constructors =====

//    // density_function_multi_pos_args_data *
//    // c2me_natives_create_chunk_noise_sampler_data(int horizontalBlockSize, int verticalBlockSize, int baseX, int baseY,
//    //                                              int baseZ)
//    private static final MethodHandle DFA_create_chunk_noise_sampler_data = LINKER.downcallHandle(
//            LOOKUP.lookup("c2me_natives_create_chunk_noise_sampler_data").get(),
//            MethodType.methodType(long.class, int.class, int.class, int.class, int.class, int.class),
//            FunctionDescriptor.of(C_LONG_LONG, C_INT, C_INT, C_INT, C_INT, C_INT)
//    );
//
//    public static long createChunkNoiseSamplerData(int horizontalBlockSize, int verticalBlockSize, int baseX, int baseY, int baseZ) {
//        try {
//            return (long) DFA_create_chunk_noise_sampler_data.invoke(horizontalBlockSize, verticalBlockSize, baseX, baseY, baseZ);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//    }

    // density_function_multi_pos_args_data *c2me_natives_create_chunk_noise_sampler_data_empty()

    private static final MethodHandle DFA_create_chunk_noise_sampler_data_empty = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_chunk_noise_sampler_data_empty").get(),
            MethodType.methodType(long.class),
            FunctionDescriptor.of(C_LONG_LONG)
    );

    public static long createChunkNoiseSamplerDataEmpty() {
        try {
            return (long) DFA_create_chunk_noise_sampler_data_empty.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // density_function_multi_pos_args_data *c2me_natives_create_chunk_noise_sampler_data_empty()

    private static final MethodHandle DFA_create_chunk_noise_sampler1_data_empty = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_chunk_noise_sampler1_data_empty").get(),
            MethodType.methodType(long.class),
            FunctionDescriptor.of(C_LONG_LONG)
    );

    public static long createChunkNoiseSampler1DataEmpty() {
        try {
            return (long) DFA_create_chunk_noise_sampler1_data_empty.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // density_function_impl_data *c2me_natives_create_dfi_constant(double constant)

    private static final MethodHandle DFA_create_dfi_constant = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_dfi_constant").get(),
            MethodType.methodType(long.class, double.class),
            FunctionDescriptor.of(C_LONG_LONG, C_DOUBLE)
    );

    public static long createDFIConstant(double constant) {
        try {
            return (long) DFA_create_dfi_constant.invoke(constant);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // density_function_impl_data *c2me_natives_create_dfi_noise_data(
    //        bool isNull, octave_sampler_data *firstSampler, octave_sampler_data *secondSampler, double amplitude,
    //        double xzScale, double yScale)
    private static final MethodHandle DFI_create_dfi_noise_data = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_dfi_noise_data").get(),
            MethodType.methodType(long.class, byte.class, long.class, long.class, double.class, double.class, double.class),
            FunctionDescriptor.of(C_LONG_LONG, C_CHAR, C_LONG_LONG, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE)
    );

    public static long createDFINoiseData(boolean isNull, long firstSampler, long secondSampler, double amplitude, double xzScale, double yScale) {
        if (!isNull) {
            if (firstSampler == 0) throw new NullPointerException();
            if (secondSampler == 0) throw new NullPointerException();
        }
        try {
            return (long) DFI_create_dfi_noise_data.invoke((byte) (isNull ? 1 : 0), firstSampler, secondSampler, amplitude, xzScale, yScale);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // density_function_impl_data *c2me_natives_create_dfi_end_islands(int* permutations)

    private static final MethodHandle DFI_create_dfi_end_islands = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_dfi_end_islands").get(),
            MethodType.methodType(long.class, long.class),
            FunctionDescriptor.of(C_LONG_LONG, C_LONG_LONG)
    );

    public static long createDFIEndIslands(long ptr_permutations) {
        if (ptr_permutations == 0) throw new NullPointerException();
        try {
            return (long) DFI_create_dfi_end_islands.invoke(ptr_permutations);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // density_function_impl_data *c2me_natives_create_dfi_shifted0_data(bool isNull,
    //                                                                   octave_sampler_data *firstSampler,
    //                                                                   octave_sampler_data *secondSampler,
    //                                                                   double amplitude)

    private static final MethodHandle DFI_create_dfi_shifted0_data = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_dfi_shifted0_data").get(),
            MethodType.methodType(long.class, byte.class, long.class, long.class, double.class),
            FunctionDescriptor.of(C_LONG_LONG, C_CHAR, C_LONG_LONG, C_LONG_LONG, C_DOUBLE)
    );

    public static long createDFIShifted0Data(boolean isNull, long firstSampler, long secondSampler, double amplitude) {
        if (!isNull) {
            if (firstSampler == 0) throw new NullPointerException();
            if (secondSampler == 0) throw new NullPointerException();
        }
        try {
            return (long) DFI_create_dfi_shifted0_data.invoke((byte) (isNull ? 1 : 0), firstSampler, secondSampler, amplitude);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // density_function_impl_data *c2me_natives_create_dfi_shiftedA_data(bool isNull,
    //                                                                   octave_sampler_data *firstSampler,
    //                                                                   octave_sampler_data *secondSampler,
    //                                                                   double amplitude)

    private static final MethodHandle DFI_create_dfi_shiftedA_data = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_dfi_shiftedA_data").get(),
            MethodType.methodType(long.class, byte.class, long.class, long.class, double.class),
            FunctionDescriptor.of(C_LONG_LONG, C_CHAR, C_LONG_LONG, C_LONG_LONG, C_DOUBLE)
    );

    public static long createDFIShiftedAData(boolean isNull, long firstSampler, long secondSampler, double amplitude) {
        if (!isNull) {
            if (firstSampler == 0) throw new NullPointerException();
            if (secondSampler == 0) throw new NullPointerException();
        }
        try {
            return (long) DFI_create_dfi_shiftedA_data.invoke((byte) (isNull ? 1 : 0), firstSampler, secondSampler, amplitude);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle DFI_create_dfi_shiftedB_data = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_create_dfi_shiftedB_data").get(),
            MethodType.methodType(long.class, byte.class, long.class, long.class, double.class),
            FunctionDescriptor.of(C_LONG_LONG, C_CHAR, C_LONG_LONG, C_LONG_LONG, C_DOUBLE)
    );

    public static long createDFIShiftedBData(boolean isNull, long firstSampler, long secondSampler, double amplitude) {
        if (!isNull) {
            if (firstSampler == 0) throw new NullPointerException();
            if (secondSampler == 0) throw new NullPointerException();
        }
        try {
            return (long) DFI_create_dfi_shiftedB_data.invoke((byte) (isNull ? 1 : 0), firstSampler, secondSampler, amplitude);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // ===== Density Function Bindings =====

    // double c2me_natives_dfi_bindings_single_op(density_function_impl_data *dfi, int blockX, int blockY, int blockZ)
    private static final MethodHandle DFI_bindings_single_op = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_dfi_bindings_single_op").get(),
            MethodType.methodType(double.class, long.class, int.class, int.class, int.class),
            FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_INT, C_INT, C_INT)
    );

    public static double dfiBindingsSingleOp(long ptr_dfi, int blockX, int blockY, int blockZ) {
        if (ptr_dfi == 0) throw new NullPointerException();
        try {
            return (double) DFI_bindings_single_op.invoke(ptr_dfi, blockX, blockY, blockZ);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // void c2me_natives_dfi_bindings_multi_op(density_function_impl_data *dfi, density_function_multi_pos_args_data *dfa,
    //                                          double *res, size_t length)
    private static final MethodHandle DFI_bindings_multi_op = LINKER.downcallHandle(
            LOOKUP.lookup("c2me_natives_dfi_bindings_multi_op").get(),
            MethodType.methodType(void.class, long.class, long.class, long.class, long.class),
            FunctionDescriptor.ofVoid(C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG)
    );

    public static void dfiBindingsMultiOp(long ptr_dfi, long ptr_dfa, long ptr_res, long length) {
        if (ptr_dfi == 0) throw new NullPointerException();
        if (ptr_dfa == 0) throw new NullPointerException();
        if (ptr_res == 0) throw new NullPointerException();
        try {
            DFI_bindings_multi_op.invoke(ptr_dfi, ptr_dfa, ptr_res, length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void dfiBindingsMultiOp(long ptr_dfi, long ptr_dfa, double[] res) {
        final int size = res.length * 8;
        final long ptr_res = NativeMemoryTracker.allocateMemoryWithoutCleaner(size);
        dfiBindingsMultiOp(ptr_dfi, ptr_dfa, ptr_res, res.length);
        byte[] tmp = new byte[size];
        PlatformDependent.copyMemory(ptr_res, tmp, 0, size);
        UnsafeUtil.getInstance().copyMemory(
                tmp,
                Unsafe.ARRAY_BYTE_BASE_OFFSET,
                res,
                Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
                size
        );
        NativeMemoryTracker.freeMemoryWithoutCleaner(ptr_res, size);
    }

    // ===== Struct sizeof()s =====
    public static final long SIZEOF_octave_sampler_data = sizeOf("octave_sampler_data");
    public static final long SIZEOF_interpolated_sampler_data = sizeOf("interpolated_sampler_data");
    public static final long SIZEOF_chunk_noise_sampler_data = sizeOf("chunk_noise_sampler_data");
    public static final long SIZEOF_dfi_noise_data = sizeOf("dfi_noise_data");
    public static final long SIZEOF_density_function_data = sizeOf("density_function_data");
    public static final long SIZEOF_density_function_multi_pos_args_data = sizeOf("density_function_multi_pos_args_data");
    public static final long SIZEOF_dfi_constant_data = sizeOf("dfi_constant_data");
    public static final long SIZEOF_dfi_end_islands_data = sizeOf("dfi_end_islands_data");
    public static final long SIZEOF_dfi_simple_shifted_noise_data = sizeOf("dfi_simple_shifted_noise_data");

    static {

        INIT = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_init").get(),
                MethodType.methodType(void.class),
                FunctionDescriptor.ofVoid()
        );

        PERLIN_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_sample").get(),
                MethodType.methodType(double.class, long.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE)
        );

        PERLIN_GENERATE_PERMUTATIONS = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_generatePermutations").get(),
                MethodType.methodType(long.class),
                FunctionDescriptor.of(C_LONG_LONG));

        // octave_sampler_data *c2me_natives_create_octave_sampler_data(
        //    double lacunarity, double persistence, size_t length, size_t *indexes, __uint8_t *sampler_permutations,
        //    double *sampler_originX, double *sampler_originY, double *sampler_originZ, double *amplitudes)

        PERLIN_CREATE_OCTAVE_SAMPLER_DATA = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_create_octave_sampler_data").get(),
                MethodType.methodType(long.class, double.class, double.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class),
                FunctionDescriptor.of(C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG)
        );

        // double c2me_natives_octave_sample(octave_sampler_data *data, double x, double y, double z)

        PERLIN_OCTAVE_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_octave_sample").get(),
                MethodType.methodType(double.class, long.class, double.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE)
        );

        // interpolated_sampler_data *c2me_natives_create_interpolated_sampler_data(
        //    octave_sampler_data *lowerInterpolatedNoise, octave_sampler_data *upperInterpolatedNoise, octave_sampler_data *interpolationNoise,
        //    double xzScale, double yScale, double xzMainScale, double yMainScale, int cellWidth, int cellHeight)

        PERLIN_CREATE_INTERPOLATED_SAMPLER_DATA = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_create_interpolated_sampler_data").get(),
                MethodType.methodType(long.class, long.class, long.class, long.class, double.class, double.class, double.class, double.class, int.class, int.class),
                FunctionDescriptor.of(C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_INT, C_INT)
        );

        // double c2me_natives_interpolated_sample(interpolated_sampler_data *data, int x, int y, int z)

        PERLIN_INTERPOLATED_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_interpolated_sample").get(),
                MethodType.methodType(double.class, long.class, int.class, int.class, int.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_INT, C_INT, C_INT)
        );

        // double c2me_natives_double_sample(
        //    octave_sampler_data *firstSampler, octave_sampler_data *secondSampler,
        //    double x, double y, double z, double amplitude)

        PERLIN_DOUBLE_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_perlin_double_sample").get(),
                MethodType.methodType(double.class, long.class, long.class, double.class, double.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_LONG_LONG, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE)
        );

        // double c2me_natives_simplex_sample(int *permutations, double x, double y)

        SIMPLEX_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_simplex_sample").get(),
                MethodType.methodType(double.class, long.class, double.class, double.class),
                FunctionDescriptor.of(C_DOUBLE, C_LONG_LONG, C_DOUBLE, C_DOUBLE)
        );

        // float c2me_natives_end_noise_sample(int *permutations, int i, int j)

        THE_END_SAMPLE = LINKER.downcallHandle(
                LOOKUP.lookup("c2me_natives_end_noise_sample").get(),
                MethodType.methodType(float.class, long.class, int.class, int.class),
                FunctionDescriptor.of(C_FLOAT, C_LONG_LONG, C_INT, C_INT)
        );

        initNatives();
    }

    private static void initNatives() {
        try {
            INIT.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double perlinSample(long permutations, double originX, double originY, double originZ, double x, double y, double z, double yScale, double yMax) {
        if (permutations == 0) throw new NullPointerException();
        try {
            return (double) PERLIN_SAMPLE.invoke(permutations, originX, originY, originZ, x, y, z, yScale, yMax);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long perlinGeneratePermutations() {
        try {
            return (long) PERLIN_GENERATE_PERMUTATIONS.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long createPerlinOctaveSamplerData(double lacunarity, double persistence, long length, long ptr_indexes, long ptr_sampler_permutations,
                                                     long ptr_sampler_originX, long ptr_sampler_originY, long ptr_sampler_originZ, long ptr_amplitudes) {
        if (ptr_indexes == 0) throw new NullPointerException();
        if (ptr_sampler_permutations == 0) throw new NullPointerException();
        if (ptr_sampler_originX == 0) throw new NullPointerException();
        if (ptr_sampler_originY == 0) throw new NullPointerException();
        if (ptr_sampler_originZ == 0) throw new NullPointerException();
        if (ptr_amplitudes == 0) throw new NullPointerException();
        try {
            return (long) PERLIN_CREATE_OCTAVE_SAMPLER_DATA.invoke(lacunarity, persistence, length, ptr_indexes, ptr_sampler_permutations, ptr_sampler_originX, ptr_sampler_originY, ptr_sampler_originZ, ptr_amplitudes);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double perlinSampleOctave(long ptr_octaveSamplerData, double x, double y, double z) {
        if (ptr_octaveSamplerData == 0) throw new NullPointerException();
        try {
            return (double) PERLIN_OCTAVE_SAMPLE.invoke(ptr_octaveSamplerData, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long createPerlinInterpolatedSamplerData(long ptr_lowerInterpolatedNoise, long ptr_upperInterpolatedNoise, long ptr_interpolationNoise,
                                                           double xzScale, double yScale, double xzMainScale, double yMainScale, int cellWidth, int cellHeight) {
        if (ptr_interpolationNoise == 0) throw new NullPointerException();
        if (ptr_lowerInterpolatedNoise == 0) throw new NullPointerException();
        if (ptr_upperInterpolatedNoise == 0) throw new NullPointerException();
        try {
            return (long) PERLIN_CREATE_INTERPOLATED_SAMPLER_DATA.invoke(ptr_lowerInterpolatedNoise, ptr_upperInterpolatedNoise, ptr_interpolationNoise, xzScale, yScale, xzMainScale, yMainScale, cellWidth, cellHeight);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double perlinSampleInterpolated(long ptr_interpolatedSamplerData, int x, int y, int z) {
        if (ptr_interpolatedSamplerData == 0) throw new NullPointerException();
        try {
            return (double) PERLIN_INTERPOLATED_SAMPLE.invoke(ptr_interpolatedSamplerData, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double perlinSampleDouble(long ptr_firstSampler, long ptr_secondSampler, double x, double y, double z, double amplitude) {
        if (ptr_firstSampler == 0) throw new NullPointerException();
        if (ptr_secondSampler == 0) throw new NullPointerException();
        try {
            return (double) PERLIN_DOUBLE_SAMPLE.invoke(ptr_firstSampler, ptr_secondSampler, x, y, z, amplitude);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static double simplexSample(long ptr_simplexNoise, double x, double y) {
        if (ptr_simplexNoise == 0) throw new NullPointerException();
        try {
            return (double) SIMPLEX_SAMPLE.invoke(ptr_simplexNoise, x, y);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static float theEndSample(long ptr_simplexNoise, int x, int y) {
        if (ptr_simplexNoise == 0) throw new NullPointerException();
        try {
            return (float) THE_END_SAMPLE.invoke(ptr_simplexNoise, x, y);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void testSample() {
        final long memoryAddress = perlinGeneratePermutations();

        long startTime = System.nanoTime();
        final int count = 1 << 20;
        for (int i = 0; i < count; i++) {
            perlinSample(memoryAddress, 0, 0, 0, 40, 140, 20, 1.5, 40);
        }
        long endTime = System.nanoTime();
        System.out.println("%.2fns/op".formatted((endTime - startTime) / (double) count));

        PlatformDependent.freeMemory(memoryAddress);
    }

    /**
     * Returns struct size
     *
     * <br/>
     * Note: this operation is expensive and a function must be declared to return the size of the struct
     *
     * @param structName name of struct
     * @return size of struct
     */
    static long sizeOf(String structName) {
        try {
            return (long) LINKER.downcallHandle(
                    LOOKUP.lookup(String.format("c2me_natives_sizeof_%s", structName)).get(),
                    MethodType.methodType(long.class),
                    FunctionDescriptor.of(C_LONG_LONG)
            ).invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
