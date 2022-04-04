package com.ishland.c2me.natives.common;

import io.netty.util.internal.PlatformDependent;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.SymbolLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.CLinker.C_DOUBLE;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;

public class NativesInterface {

    private static final CLinker LINKER = CLinker.getInstance();
    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    private static final MethodHandle SAMPLE;
    private static final MethodHandle GENERATE_PERMUTATIONS;

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

    }

    public static double sample(long permutations, double originX, double originY, double originZ, double x, double y, double z, double yScale, double yMax) {
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

    private static void testSample() {
        final long memoryAddress = generatePermutations();

        long startTime = System.nanoTime();
        final int count = 1 << 25;
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
