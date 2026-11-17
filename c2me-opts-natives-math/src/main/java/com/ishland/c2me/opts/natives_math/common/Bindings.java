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

package com.ishland.c2me.opts.natives_math.common;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public class Bindings {

    private static MethodHandle bind(MethodHandle template, String prefix) {
        return template.bindTo(NativeLoader.lookup.find(prefix + NativeLoader.currentMachineTarget.getSuffix()).get());
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_sample_legacy_area = bind(BindingsTemplate.c2me_natives_noise_perlin_sample_legacy_area, "c2me_natives_noise_perlin_sample_legacy_area");

    public static void c2me_natives_noise_perlin_sample_legacy_area(final MemorySegment fusedPermutationTable,
                                                                    final double originX, final double originY, final double originZ,
                                                                    final double yScale, final MemorySegment output,
                                                                    final int sizeX, final int sizeY, final int sizeZ,
                                                                    final int minBlockX, final int minBlockY, final int minBlockZ,
                                                                    final int stepBlockX, final int stepBlockY, final int stepBlockZ,
                                                                    final double scaleXz, final double scaleY, final float outputScale) {
        try {
            MH_c2me_natives_noise_perlin_sample_legacy_area.invokeExact(fusedPermutationTable, originX, originY, originZ, yScale, output, sizeX,
                    sizeY, sizeZ, minBlockX, minBlockY, minBlockZ, stepBlockX, stepBlockY, stepBlockZ, scaleXz, scaleY, outputScale);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_sample_base_area = bind(BindingsTemplate.c2me_natives_noise_perlin_sample_base_area, "c2me_natives_noise_perlin_sample_base_area");

    public static void c2me_natives_noise_perlin_sample_base_area(final MemorySegment fusedPermutationTable,
                                                                  final double originX, final double originY, final double originZ,
                                                                  final MemorySegment output,
                                                                  final int sizeX, final int sizeY, final int sizeZ,
                                                                  final int minBlockX, final int minBlockY, final int minBlockZ,
                                                                  final int stepBlockX, final int stepBlockY, final int stepBlockZ,
                                                                  final double scaleXz, final double scaleY, final float outputScale) {
        try {
            MH_c2me_natives_noise_perlin_sample_base_area.invokeExact(fusedPermutationTable, originX, originY, originZ, output, sizeX,
                    sizeY, sizeZ, minBlockX, minBlockY, minBlockZ, stepBlockX, stepBlockY, stepBlockZ, scaleXz, scaleY, outputScale);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_sample_shifted_legacy_area = bind(BindingsTemplate.c2me_natives_noise_perlin_sample_shifted_legacy_area, "c2me_natives_noise_perlin_sample_shifted_legacy_area");

    public static void c2me_natives_noise_perlin_sample_shifted_legacy_area(final MemorySegment fusedPermutationTable,
                                                                            final double originX, final double originY, final double originZ,
                                                                            final double yScale, final MemorySegment output,
                                                                            final int sizeX, final int sizeY, final int sizeZ,
                                                                            final int minBlockX, final int minBlockY, final int minBlockZ,
                                                                            final int stepBlockX, final int stepBlockY, final int stepBlockZ,
                                                                            final double preScaleXz, final double preScaleY,
                                                                            final MemorySegment shiftX, final MemorySegment shiftY, final MemorySegment shiftZ,
                                                                            final double scaleXz, final double scaleY, final float outputScale) {
        try {
            MH_c2me_natives_noise_perlin_sample_shifted_legacy_area.invokeExact(fusedPermutationTable, originX, originY, originZ, yScale, output, sizeX,
                    sizeY, sizeZ, minBlockX, minBlockY, minBlockZ, stepBlockX, stepBlockY, stepBlockZ, preScaleXz, preScaleY, shiftX, shiftY, shiftZ, scaleXz, scaleY, outputScale);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static final MethodHandle MH_c2me_natives_noise_perlin_sample_shifted_base_area = bind(BindingsTemplate.c2me_natives_noise_perlin_sample_shifted_base_area, "c2me_natives_noise_perlin_sample_shifted_base_area");

    public static void c2me_natives_noise_perlin_sample_shifted_base_area(final MemorySegment fusedPermutationTable,
                                                                          final double originX, final double originY, final double originZ,
                                                                          final MemorySegment output,
                                                                          final int sizeX, final int sizeY, final int sizeZ,
                                                                          final int minBlockX, final int minBlockY, final int minBlockZ,
                                                                          final int stepBlockX, final int stepBlockY, final int stepBlockZ,
                                                                          final double preScaleXz, final double preScaleY,
                                                                          final MemorySegment shiftX, final MemorySegment shiftY, final MemorySegment shiftZ,
                                                                          final double scaleXz, final double scaleY, final float outputScale) {
        try {
            MH_c2me_natives_noise_perlin_sample_shifted_base_area.invokeExact(fusedPermutationTable, originX, originY, originZ, output, sizeX,
                    sizeY, sizeZ, minBlockX, minBlockY, minBlockZ, stepBlockX, stepBlockY, stepBlockZ, preScaleXz, preScaleY, shiftX, shiftY, shiftZ, scaleXz, scaleY, outputScale);
        } catch (Throwable t) {
            throw new RuntimeException(t);
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

//    private static final MethodHandle MH_c2me_natives_fnlGetNoise3D = bind(BindingsTemplate.c2me_natives_fnlGetNoise3D, "c2me_natives_fnlGetNoise3D");
//    private static final MethodHandle MH_c2me_natives_fnlGetNoise3D_ptr = bind(BindingsTemplate.c2me_natives_fnlGetNoise3D_ptr, "c2me_natives_fnlGetNoise3D");

    public static float c2me_natives_fnlGetNoise3D(MemorySegment state, double x, double y, double z) {
        throw new AbstractMethodError();
//        try {
//            return (float) MH_c2me_natives_fnlGetNoise3D.invokeExact(state, x, y, z);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
    }

    public static float c2me_natives_fnlGetNoise3D(long state_ptr, double x, double y, double z) {
        throw new AbstractMethodError();
//        try {
//            return (float) MH_c2me_natives_fnlGetNoise3D_ptr.invokeExact(state_ptr, x, y, z);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
    }
}
