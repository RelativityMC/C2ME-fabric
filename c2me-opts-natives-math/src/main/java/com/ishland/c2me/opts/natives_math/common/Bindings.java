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

package com.ishland.c2me.opts.natives_math.common;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public class Bindings {

    private static MethodHandle bind(MethodHandle template, String prefix) {
        return template.bindTo(NativeLoader.lookup.find(prefix + NativeLoader.currentMachineTarget.getSuffix()).get());
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_double = bind(BindingsTemplate.c2me_natives_noise_perlin_double, "c2me_natives_noise_perlin_double");
    private static final MethodHandle MH_c2me_natives_noise_perlin_double_ptr = bind(BindingsTemplate.c2me_natives_noise_perlin_double_ptr, "c2me_natives_noise_perlin_double");

    public static double c2me_natives_noise_perlin_double(MemorySegment data, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_perlin_double.invokeExact(data, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static double c2me_natives_noise_perlin_double(long data_ptr, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_perlin_double_ptr.invokeExact(data_ptr, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_double_batch = bind(BindingsTemplate.c2me_natives_noise_perlin_double_batch, "c2me_natives_noise_perlin_double_batch");
    private static final MethodHandle MH_c2me_natives_noise_perlin_double_batch_partial_ptr = bind(BindingsTemplate.c2me_natives_noise_perlin_double_batch_ptr, "c2me_natives_noise_perlin_double_batch");

    public static void c2me_natives_noise_perlin_double_batch(MemorySegment data, MemorySegment res, MemorySegment x, MemorySegment y, MemorySegment z, int length) {
        try {
            MH_c2me_natives_noise_perlin_double_batch.invokeExact(data, res, x, y, z, length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void c2me_natives_noise_perlin_double_batch(long data_ptr, MemorySegment res, MemorySegment x, MemorySegment y, MemorySegment z, int length) {
        try {
            MH_c2me_natives_noise_perlin_double_batch_partial_ptr.invokeExact(data_ptr, res, x, y, z, length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle MH_c2me_natives_noise_interpolated = bind(BindingsTemplate.c2me_natives_noise_interpolated, "c2me_natives_noise_interpolated");
    private static final MethodHandle MH_c2me_natives_noise_interpolated_ptr = bind(BindingsTemplate.c2me_natives_noise_interpolated_ptr, "c2me_natives_noise_interpolated");

    public static double c2me_natives_noise_interpolated(MemorySegment data, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_interpolated.invokeExact(data, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static double c2me_natives_noise_interpolated(long data_ptr, double x, double y, double z) {
        try {
            return (double) MH_c2me_natives_noise_interpolated_ptr.invokeExact(data_ptr, x, y, z);
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

    private static final MethodHandle MH_c2me_natives_biome_access_sample = bind(BindingsTemplate.c2me_natives_biome_access_sample, "c2me_natives_biome_access_sample");

    public static int c2me_natives_biome_access_sample(long seed, int x, int y, int z) {
        try {
            return (int) MH_c2me_natives_biome_access_sample.invokeExact(seed, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle MH_c2me_natives_aquifer_refreshDistPosIdx = bind(BindingsTemplate.c2me_natives_aquifer_refreshDistPosIdx, "c2me_natives_aquifer_refreshDistPosIdx");
    private static final MethodHandle MH_c2me_natives_aquifer_refreshDistPosIdx_ptr = bind(BindingsTemplate.c2me_natives_aquifer_refreshDistPosIdx_ptr, "c2me_natives_aquifer_refreshDistPosIdx");

    public static void c2me_natives_aquifer_refreshDistPosIdx(MemorySegment packedBlockPositions, MemorySegment res, MemorySegment aquiferData, int x, int y, int z) {
        try {
            MH_c2me_natives_aquifer_refreshDistPosIdx.invokeExact(packedBlockPositions, res, aquiferData, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void c2me_natives_aquifer_refreshDistPosIdx(long packedBlockPositions_ptr, long res_ptr, long aquiferData_ptr, int x, int y, int z) {
        try {
            MH_c2me_natives_aquifer_refreshDistPosIdx_ptr.invokeExact(packedBlockPositions_ptr, res_ptr, aquiferData_ptr, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle MH_c2me_natives_biome_search_tree_calc_args = bind(BindingsTemplate.c2me_natives_biome_search_tree_calc_args, "c2me_natives_biome_search_tree_calc_args");
    private static final MethodHandle MH_c2me_natives_biome_search_tree_calc_args_ptr = bind(BindingsTemplate.c2me_natives_biome_search_tree_calc_args_ptr, "c2me_natives_biome_search_tree_calc_args");

    public static int c2me_natives_biome_search_tree_calc_args(MemorySegment nodes, int nodes_c, int tree_depth, short p0, short p1, short p2, short p3, short p4, short p5, short p6) {
        try {
            return (int) MH_c2me_natives_biome_search_tree_calc_args.invokeExact(nodes, nodes_c, tree_depth, p0, p1, p2, p3, p4, p5, p6);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int c2me_natives_biome_search_tree_calc_args(long nodes_ptr, int nodes_c, int tree_depth, short p0, short p1, short p2, short p3, short p4, short p5, short p6) {
        try {
            return (int) MH_c2me_natives_biome_search_tree_calc_args_ptr.invokeExact(nodes_ptr, nodes_c, tree_depth, p0, p1, p2, p3, p4, p5, p6);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
