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

import com.ishland.c2me.base.mixin.access.IInterpolatedNoiseSampler;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTree;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeBranchNode;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeLeafNode;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeNode;
import com.ishland.c2me.base.mixin.access.IOctavePerlinNoiseSampler;
import com.ishland.c2me.base.mixin.access.IPerlinNoiseSampler;
import com.ishland.c2me.base.common.util.MemoryUtil;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class BindingsTemplate {

    // double c2me_natives_noise_perlin_sample (const uint8_t *permutations, double originX, double originY, double originZ, double x, double y, double z, double yScale, double yMax)
    public static final MethodHandle c2me_natives_noise_perlin_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(true)
    );

    // c2me_natives_noise_perlin_double, double, (const double_octave_sampler_data_t *data, double x, double y, double z)
    public static final MethodHandle c2me_natives_noise_perlin_double = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );
    public static final MethodHandle c2me_natives_noise_perlin_double_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );

    // c2me_natives_noise_perlin_double_batch, void, (const double_octave_sampler_data_t *const data,
    //                                                                           double *const res, const double *const x,
    //                                                                           const double *const y, const double *const z,
    //                                                                           const uint32_t length)
    public static final MethodHandle c2me_natives_noise_perlin_double_batch = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(true)
    );
    public static final MethodHandle c2me_natives_noise_perlin_double_batch_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.JAVA_LONG,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(true)
    );


    public static final StructLayout double_octave_sampler_data = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("length"),
            ValueLayout.JAVA_DOUBLE.withName("amplitude"), // 16
            ValueLayout.JAVA_INT.withName("need_shift"),
            ValueLayout.JAVA_INT.withName("lacunarity_powd"),
            ValueLayout.JAVA_INT.withName("persistence_powd"),
            ValueLayout.JAVA_INT.withName("sampler_permutations"), // 32
            ValueLayout.JAVA_INT.withName("sampler_originX"),
            ValueLayout.JAVA_INT.withName("sampler_originY"),
            ValueLayout.JAVA_INT.withName("sampler_originZ"),
            ValueLayout.JAVA_INT.withName("amplitudes") // 48
    ).withByteAlignment(64).withName("double_double_octave_sampler_data");
    public static final VarHandle double_octave_sampler_data$length = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("length"));
    public static final VarHandle double_octave_sampler_data$amplitude = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("amplitude"));
    public static final VarHandle double_octave_sampler_data$need_shift = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("need_shift"));
    public static final VarHandle double_octave_sampler_data$lacunarity_powd = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("lacunarity_powd"));
    public static final VarHandle double_octave_sampler_data$persistence_powd = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("persistence_powd"));
    public static final VarHandle double_octave_sampler_data$sampler_permutations = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_permutations"));
    public static final VarHandle double_octave_sampler_data$sampler_originX = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_originX"));
    public static final VarHandle double_octave_sampler_data$sampler_originY = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_originY"));
    public static final VarHandle double_octave_sampler_data$sampler_originZ = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_originZ"));
    public static final VarHandle double_octave_sampler_data$amplitudes = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("amplitudes"));

    public static MemorySegment double_octave_sampler_data$create(Arena arena, OctavePerlinNoiseSampler firstSampler, OctavePerlinNoiseSampler secondSampler, double amplitude, boolean int8) {
        long nonNullSamplerCount = 0;
        for (PerlinNoiseSampler sampler : ((IOctavePerlinNoiseSampler) firstSampler).getOctaveSamplers()) {
            if (sampler != null) {
                nonNullSamplerCount++;
            }
        }
        for (PerlinNoiseSampler sampler : ((IOctavePerlinNoiseSampler) secondSampler).getOctaveSamplers()) {
            if (sampler != null) {
                nonNullSamplerCount++;
            }
        }
        final long need_shift_offset = double_octave_sampler_data.byteSize();
        final long lacunarity_powd_offset = MemoryUtil.roundUp(need_shift_offset + nonNullSamplerCount, 64);
        final long persistence_powd_offset = MemoryUtil.roundUp(lacunarity_powd_offset + nonNullSamplerCount * 8, 64);
        final long sampler_permutations_offset = MemoryUtil.roundUp(persistence_powd_offset + nonNullSamplerCount * 8, 64);
        final long sampler_originX_offset = MemoryUtil.roundUp(sampler_permutations_offset + nonNullSamplerCount * 256 * (int8 ? 1 : 4), 64);
        final long sampler_originY_offset = MemoryUtil.roundUp(sampler_originX_offset + nonNullSamplerCount * 8, 64);
        final long sampler_originZ_offset = MemoryUtil.roundUp(sampler_originY_offset + nonNullSamplerCount * 8, 64);
        final long amplitudes_offset = MemoryUtil.roundUp(sampler_originZ_offset + nonNullSamplerCount * 8, 64);
        final MemorySegment data = arena.allocate(MemoryUtil.roundUp(amplitudes_offset + nonNullSamplerCount * 8, 64), 64);
        final MemorySegment need_shift = data.asSlice(need_shift_offset, nonNullSamplerCount, 1);
        final MemorySegment lacunarity_powd = data.asSlice(lacunarity_powd_offset, nonNullSamplerCount * 8, 64);
        final MemorySegment persistence_powd = data.asSlice(persistence_powd_offset, nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_permutations = data.asSlice(sampler_permutations_offset, nonNullSamplerCount * 256 * (int8 ? 1 : 4), 64);
        final MemorySegment sampler_originX = data.asSlice(sampler_originX_offset, nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_originY = data.asSlice(sampler_originY_offset, nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_originZ = data.asSlice(sampler_originZ_offset, nonNullSamplerCount * 8, 64);
        final MemorySegment amplitudes = data.asSlice(amplitudes_offset, nonNullSamplerCount * 8, 64);
        double_octave_sampler_data$length.set(data, 0L, nonNullSamplerCount);
        double_octave_sampler_data$amplitude.set(data, 0L, amplitude);
        double_octave_sampler_data$need_shift.set(data, 0L, (int) need_shift_offset);
        double_octave_sampler_data$lacunarity_powd.set(data, 0L, (int) lacunarity_powd_offset);
        double_octave_sampler_data$persistence_powd.set(data, 0L, (int) persistence_powd_offset);
        double_octave_sampler_data$sampler_permutations.set(data, 0L, (int) sampler_permutations_offset);
        double_octave_sampler_data$sampler_originX.set(data, 0L, (int) sampler_originX_offset);
        double_octave_sampler_data$sampler_originY.set(data, 0L, (int) sampler_originY_offset);
        double_octave_sampler_data$sampler_originZ.set(data, 0L, (int) sampler_originZ_offset);
        double_octave_sampler_data$amplitudes.set(data, 0L, (int) amplitudes_offset);
        long index = 0;
        {
            PerlinNoiseSampler[] octaveSamplers = ((IOctavePerlinNoiseSampler) firstSampler).getOctaveSamplers();
            for (int i = 0, octaveSamplersLength = octaveSamplers.length; i < octaveSamplersLength; i++) {
                PerlinNoiseSampler sampler = octaveSamplers[i];
                if (sampler != null) {
                    need_shift.set(ValueLayout.JAVA_BOOLEAN, index, false);
                    lacunarity_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) firstSampler).getLacunarity() * Math.pow(2.0, i));
                    persistence_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) firstSampler).getPersistence() * Math.pow(2.0, -i));
                    MemorySegment.copy(int8 ? MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()) : MemorySegment.ofArray(MemoryUtil.byte2int(((IPerlinNoiseSampler) (Object) sampler).getPermutation())), 0, sampler_permutations, index * 256L * (int8 ? 1L : 4L), 256 * (int8 ? 1L : 4L));
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originZ);
                    amplitudes.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) firstSampler).getAmplitudes().getDouble(i));
                    index++;
                }
            }
        }
        {
            PerlinNoiseSampler[] octaveSamplers = ((IOctavePerlinNoiseSampler) secondSampler).getOctaveSamplers();
            for (int i = 0, octaveSamplersLength = octaveSamplers.length; i < octaveSamplersLength; i++) {
                PerlinNoiseSampler sampler = octaveSamplers[i];
                if (sampler != null) {
                    need_shift.set(ValueLayout.JAVA_BOOLEAN, index, true);
                    lacunarity_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) secondSampler).getLacunarity() * Math.pow(2.0, i));
                    persistence_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) secondSampler).getPersistence() * Math.pow(2.0, -i));
                    MemorySegment.copy(int8 ? MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()) : MemorySegment.ofArray(MemoryUtil.byte2int(((IPerlinNoiseSampler) (Object) sampler).getPermutation())), 0, sampler_permutations, index * 256L * (int8 ? 1L : 4L), 256 * (int8 ? 1L : 4L));
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originZ);
                    amplitudes.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) secondSampler).getAmplitudes().getDouble(i));
                    index++;
                }
            }
        }

        VarHandle.fullFence();

        return data;
    }

    // c2me_natives_noise_interpolated, double, (const interpolated_noise_sampler_t *const data, const double x, const double y, const double z)
    public static final MethodHandle c2me_natives_noise_interpolated = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );
    public static final MethodHandle c2me_natives_noise_interpolated_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );

    public static final StructLayout interpolated_noise_sub_sampler = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("length"),
            ValueLayout.JAVA_INT.withName("sampler_permutations"),
            ValueLayout.JAVA_INT.withName("sampler_originX"),
            ValueLayout.JAVA_INT.withName("sampler_originY"),
            ValueLayout.JAVA_INT.withName("sampler_originZ"),
            ValueLayout.JAVA_INT.withName("sampler_mulFactor")
    ).withName("interpolated_noise_sub_sampler_t");

    public static final StructLayout interpolated_noise_sampler = MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName("scaledXzScale"),
            ValueLayout.JAVA_DOUBLE.withName("scaledYScale"),
            ValueLayout.JAVA_DOUBLE.withName("xzFactor"),
            ValueLayout.JAVA_DOUBLE.withName("yFactor"),
            ValueLayout.JAVA_DOUBLE.withName("smearScaleMultiplier"),
            ValueLayout.JAVA_DOUBLE.withName("xzScale"),
            ValueLayout.JAVA_DOUBLE.withName("yScale"),

            interpolated_noise_sub_sampler.withName("lower"),
            interpolated_noise_sub_sampler.withName("upper"),
            interpolated_noise_sub_sampler.withName("normal")
    ).withByteAlignment(32).withName("interpolated_noise_sampler_t");

    public static final VarHandle interpolated_noise_sampler$scaledXzScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("scaledXzScale"));
    public static final VarHandle interpolated_noise_sampler$scaledYScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("scaledYScale"));
    public static final VarHandle interpolated_noise_sampler$xzFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("xzFactor"));
    public static final VarHandle interpolated_noise_sampler$yFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("yFactor"));
    public static final VarHandle interpolated_noise_sampler$smearScaleMultiplier = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("smearScaleMultiplier"));
    public static final VarHandle interpolated_noise_sampler$xzScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("xzScale"));
    public static final VarHandle interpolated_noise_sampler$yScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("yScale"));
    public static final VarHandle interpolated_noise_sampler$lower$sampler_permutations = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("lower"), MemoryLayout.PathElement.groupElement("sampler_permutations"));
    public static final VarHandle interpolated_noise_sampler$lower$sampler_originX = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("lower"), MemoryLayout.PathElement.groupElement("sampler_originX"));
    public static final VarHandle interpolated_noise_sampler$lower$sampler_originY = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("lower"), MemoryLayout.PathElement.groupElement("sampler_originY"));
    public static final VarHandle interpolated_noise_sampler$lower$sampler_originZ = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("lower"), MemoryLayout.PathElement.groupElement("sampler_originZ"));
    public static final VarHandle interpolated_noise_sampler$lower$sampler_mulFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("lower"), MemoryLayout.PathElement.groupElement("sampler_mulFactor"));
    public static final VarHandle interpolated_noise_sampler$lower$length = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("lower"), MemoryLayout.PathElement.groupElement("length"));
    public static final VarHandle interpolated_noise_sampler$upper$sampler_permutations = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upper"), MemoryLayout.PathElement.groupElement("sampler_permutations"));
    public static final VarHandle interpolated_noise_sampler$upper$sampler_originX = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upper"), MemoryLayout.PathElement.groupElement("sampler_originX"));
    public static final VarHandle interpolated_noise_sampler$upper$sampler_originY = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upper"), MemoryLayout.PathElement.groupElement("sampler_originY"));
    public static final VarHandle interpolated_noise_sampler$upper$sampler_originZ = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upper"), MemoryLayout.PathElement.groupElement("sampler_originZ"));
    public static final VarHandle interpolated_noise_sampler$upper$sampler_mulFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upper"), MemoryLayout.PathElement.groupElement("sampler_mulFactor"));
    public static final VarHandle interpolated_noise_sampler$upper$length = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upper"), MemoryLayout.PathElement.groupElement("length"));
    public static final VarHandle interpolated_noise_sampler$normal$sampler_permutations = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normal"), MemoryLayout.PathElement.groupElement("sampler_permutations"));
    public static final VarHandle interpolated_noise_sampler$normal$sampler_originX = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normal"), MemoryLayout.PathElement.groupElement("sampler_originX"));
    public static final VarHandle interpolated_noise_sampler$normal$sampler_originY = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normal"), MemoryLayout.PathElement.groupElement("sampler_originY"));
    public static final VarHandle interpolated_noise_sampler$normal$sampler_originZ = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normal"), MemoryLayout.PathElement.groupElement("sampler_originZ"));
    public static final VarHandle interpolated_noise_sampler$normal$sampler_mulFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normal"), MemoryLayout.PathElement.groupElement("sampler_mulFactor"));
    public static final VarHandle interpolated_noise_sampler$normal$length = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normal"), MemoryLayout.PathElement.groupElement("length"));

    public static boolean interpolated_noise_sampler$isSpecializedBase3dNoiseFunction(InterpolatedNoiseSampler interpolated) {
        return IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise()::getOctave).filter(Objects::nonNull).count() == 16 &&
                IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise()::getOctave).filter(Objects::nonNull).count() == 16 &&
                IntStream.range(0, 8).mapToObj(((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise()::getOctave).filter(Objects::nonNull).count() == 8;
    }

    public static MemorySegment interpolated_noise_sampler$create(Arena arena, InterpolatedNoiseSampler interpolated, boolean int8) {
        final MemorySegment data = arena.allocate(MemoryUtil.roundUp(interpolated_noise_sampler.byteSize(), 64) + 40 * 256L * 4L + 40 * 8L * 4, 64);
        interpolated_noise_sampler$scaledXzScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getScaledXzScale());
        interpolated_noise_sampler$scaledYScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getScaledYScale());
        interpolated_noise_sampler$xzFactor.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getXzFactor());
        interpolated_noise_sampler$yFactor.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getYFactor());
        interpolated_noise_sampler$smearScaleMultiplier.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getSmearScaleMultiplier());
        interpolated_noise_sampler$xzScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getXzScale());
        interpolated_noise_sampler$yScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getYScale());

//        if (true) {
//            System.out.println(String.format("Interpolated total: %d", countNonNull));
//            System.out.println(String.format("lower: %d", IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise()::getOctave).filter(Objects::nonNull).count()));
//            System.out.println(String.format("upper: %d", IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise()::getOctave).filter(Objects::nonNull).count()));
//            System.out.println(String.format("normal: %d", IntStream.range(0, 8).mapToObj(((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise()::getOctave).filter(Objects::nonNull).count()));
//        }

        final long sampler_permutations_offset = MemoryUtil.roundUp(interpolated_noise_sampler.byteSize(), 64);
        final long sampler_originX_offset = sampler_permutations_offset + 40 * 256L * (int8 ? 1L : 4L);
        final long sampler_originY_offset = sampler_originX_offset + 40 * 8L;
        final long sampler_originZ_offset = sampler_originY_offset + 40 * 8L;
        final long sampler_mulFactor_offset = sampler_originZ_offset + 40 * 8L;

        final MemorySegment sampler_permutations = data.asSlice(sampler_permutations_offset, 40 * 256L * (int8 ? 1L : 4L), 64);
        final MemorySegment sampler_originX = data.asSlice(sampler_originX_offset, 40 * 8L, 64);
        final MemorySegment sampler_originY = data.asSlice(sampler_originY_offset, 40 * 8L, 64);
        final MemorySegment sampler_originZ = data.asSlice(sampler_originZ_offset, 40 * 8L, 64);
        final MemorySegment sampler_mulFactor = data.asSlice(sampler_mulFactor_offset, 40 * 8L, 64);

        int index = 0;

        {
            int startIndex = index;

            for (int i = 0; i < 8; i++) {
                PerlinNoiseSampler sampler = ((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise().getOctave(i);
                if (sampler != null) {
                    MemorySegment.copy(int8 ? MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()) : MemorySegment.ofArray(MemoryUtil.byte2int(((IPerlinNoiseSampler) (Object) sampler).getPermutation())), 0, sampler_permutations, index * 256L * (int8 ? 1L : 4L), 256 * (int8 ? 1L : 4L));
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                    sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                    index ++;
                }
            }

            BindingsTemplate.interpolated_noise_sampler$normal$sampler_permutations.set(data, 0L, (int) (sampler_permutations_offset + startIndex * 256L * (int8 ? 1L : 4L)));
            BindingsTemplate.interpolated_noise_sampler$normal$sampler_originX.set(data, 0L, (int) (sampler_originX_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$normal$sampler_originY.set(data, 0L, (int) (sampler_originY_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$normal$sampler_originZ.set(data, 0L, (int) (sampler_originZ_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$normal$sampler_mulFactor.set(data, 0L, (int) (sampler_mulFactor_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$normal$length.set(data, 0L, index - startIndex);
        }

        {
            int startIndex = index = 8;

            for (int i = 0; i < 16; i++) {
                PerlinNoiseSampler sampler = ((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise().getOctave(i);
                if (sampler != null) {
                    MemorySegment.copy(int8 ? MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()) : MemorySegment.ofArray(MemoryUtil.byte2int(((IPerlinNoiseSampler) (Object) sampler).getPermutation())), 0, sampler_permutations, index * 256L * (int8 ? 1L : 4L), 256 * (int8 ? 1L : 4L));
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                    sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                    index ++;
                }
            }

            BindingsTemplate.interpolated_noise_sampler$lower$sampler_permutations.set(data, 0L, (int) (sampler_permutations_offset + startIndex * 256L * (int8 ? 1L : 4L)));
            BindingsTemplate.interpolated_noise_sampler$lower$sampler_originX.set(data, 0L, (int) (sampler_originX_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$lower$sampler_originY.set(data, 0L, (int) (sampler_originY_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$lower$sampler_originZ.set(data, 0L, (int) (sampler_originZ_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$lower$sampler_mulFactor.set(data, 0L, (int) (sampler_mulFactor_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$lower$length.set(data, 0L, index - startIndex);
        }

        {
            int startIndex = index = 8 + 16;

            for (int i = 0; i < 16; i++) {
                PerlinNoiseSampler sampler = ((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise().getOctave(i);
                if (sampler != null) {
                    MemorySegment.copy(int8 ? MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()) : MemorySegment.ofArray(MemoryUtil.byte2int(((IPerlinNoiseSampler) (Object) sampler).getPermutation())), 0, sampler_permutations, index * 256L * (int8 ? 1L : 4L), 256 * (int8 ? 1L : 4L));
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                    sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                    index ++;
                }
            }

            BindingsTemplate.interpolated_noise_sampler$upper$sampler_permutations.set(data, 0L, (int) (sampler_permutations_offset + startIndex * 256L * (int8 ? 1L : 4L)));
            BindingsTemplate.interpolated_noise_sampler$upper$sampler_originX.set(data, 0L, (int) (sampler_originX_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$upper$sampler_originY.set(data, 0L, (int) (sampler_originY_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$upper$sampler_originZ.set(data, 0L, (int) (sampler_originZ_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$upper$sampler_mulFactor.set(data, 0L, (int) (sampler_mulFactor_offset + startIndex * 8L));
            BindingsTemplate.interpolated_noise_sampler$upper$length.set(data, 0L, index - startIndex);
        }

        VarHandle.fullFence();

        return data;
    }

    // c2me_natives_end_islands_sample, float, (const int32_t *const simplex_permutations, const int32_t x, const int32_t z)
    public static final MethodHandle c2me_natives_end_islands_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_FLOAT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(true)
    );
    public static final MethodHandle c2me_natives_end_islands_sample_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_FLOAT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );

    // c2me_natives_biome_access_sample, uint32_t, (const int64_t theSeed, const int32_t x, const int32_t y, const int32_t z)
    public static final MethodHandle c2me_natives_biome_access_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );

    // (const uint16_t *restrict const packedBlockPositions, uint32_t *restrict const res,
    //  const aquifer_data_t *restrict const aquiferData,
    //  const int32_t x, const int32_t y, const int32_t z)
    public static final MethodHandle c2me_natives_aquifer_refreshDistPosIdx = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(true)
    );

    public static final MethodHandle c2me_natives_aquifer_refreshDistPosIdx_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(true)
    );

    // uint32_t, (const biome_search_tree_node_t * restrict const nodes,
    // const uint32_t nodes_c, const uint32_t tree_depth,
    // uint16_t p0, uint16_t p1, uint16_t p2, uint16_t p3,
    // uint16_t p4, uint16_t p5, uint16_t p6)

    public static final MethodHandle c2me_natives_biome_search_tree_calc_args = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT
            ),
            Linker.Option.critical(true)
    );

    public static final MethodHandle c2me_natives_biome_search_tree_calc_args_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT
            ),
            Linker.Option.critical(true)
    );

    // typedef const struct biome_search_tree_node {
    //    // bit 31: set if branch node, clear if leaf node
    //    // bit 30: set if is branch node children offsets
    //    // bit 0-29: biome ID (only valid for leaf nodes)
    //    uint32_t state;
    //    union {
    //        struct {
    //            uint32_t children_offset[7]; // at most 7 children, 0 is reserved and means no child
    //        } branch_children;
    //        struct {
    //            int16_t maxs[7];
    //            int16_t mins[7];
    //        } node_minmaxs;
    //    };
    //} biome_search_tree_node_t;

    public static final StructLayout biome_search_tree_node = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("state"),
            MemoryLayout.unionLayout(
                    MemoryLayout.structLayout(
                            MemoryLayout.sequenceLayout(7, ValueLayout.JAVA_INT).withName("children_offset")
                    ).withName("branch_children"),
                    MemoryLayout.structLayout(
                            MemoryLayout.sequenceLayout(7, ValueLayout.JAVA_SHORT).withName("maxs"),
                            MemoryLayout.sequenceLayout(7, ValueLayout.JAVA_SHORT).withName("mins")
                    ).withName("node_minmaxs")
            ).withName("union0")
    ).withName("biome_search_tree_node_t");

    public static final VarHandle biome_search_tree_node$state = biome_search_tree_node.varHandle(MemoryLayout.PathElement.groupElement("state"));

    public static MemorySegment biome_search_tree_node$branch_children$children_offset(MemorySegment segment) {
        return segment.asSlice(biome_search_tree_node.byteOffset(MemoryLayout.PathElement.groupElement("union0"), MemoryLayout.PathElement.groupElement("branch_children"), MemoryLayout.PathElement.groupElement("children_offset")), 7 * ValueLayout.JAVA_INT.byteSize());
    }

    public static MemorySegment biome_search_tree_node$node_minmaxs$maxs(MemorySegment segment) {
        return segment.asSlice(biome_search_tree_node.byteOffset(MemoryLayout.PathElement.groupElement("union0"), MemoryLayout.PathElement.groupElement("node_minmaxs"), MemoryLayout.PathElement.groupElement("maxs")), 7 * ValueLayout.JAVA_SHORT.byteSize());
    }

    public static MemorySegment biome_search_tree_node$node_minmaxs$mins(MemorySegment segment) {
        return segment.asSlice(biome_search_tree_node.byteOffset(MemoryLayout.PathElement.groupElement("union0"), MemoryLayout.PathElement.groupElement("node_minmaxs"), MemoryLayout.PathElement.groupElement("mins")), 7 * ValueLayout.JAVA_SHORT.byteSize());
    }

    public static NativeBiomeSearchTree biome_search_tree_node$create(Arena arena, MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> searchTree) {
        class TreeFlattener {
            private final List<SerializedTreeNode> nodes = new ArrayList<>();
            private final Object2IntLinkedOpenHashMap<RegistryEntry<Biome>> biomeIdMap = new Object2IntLinkedOpenHashMap<>();
            private int treeDepth;

            {
                this.biomeIdMap.defaultReturnValue(Integer.MIN_VALUE);
                this.nodes.add(new SerializedTreeNode()); // padding node
            }

            public int consume(MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>> node, int depth) {
                Objects.requireNonNull(node, "node cannot be null");
                if (depth > this.treeDepth) {
                    this.treeDepth = depth;
                }
                SerializedTreeNode serializedNode = new SerializedTreeNode();
                MultiNoiseUtil.ParameterRange[] parameters = ((IMultiNoiseUtilSearchTreeTreeNode) node).getParameters();
                Assertions.assertTrue(parameters.length == 7);
                for (int i = 0; i < 7; i++) {
                    serializedNode.maxs[i] = (short) parameters[i].max();
                    serializedNode.mins[i] = (short) parameters[i].min();
                    Assertions.assertTrue((long) serializedNode.maxs[i] == parameters[i].max(), "max value out of range: " + parameters[i].max() + " for parameter " + i);
                    Assertions.assertTrue((long) serializedNode.mins[i] == parameters[i].min(), "min value out of range: " + parameters[i].min() + " for parameter " + i);
                }
                int index = this.nodes.size();
                this.nodes.add(serializedNode);
                if (node instanceof MultiNoiseUtil.SearchTree.TreeBranchNode<RegistryEntry<Biome>> branchNode) {
                    serializedNode.isBranch = true;
                    SerializedTreeNode childrenOffsetNode = new SerializedTreeNode();
                    childrenOffsetNode.isBranch = true;
                    childrenOffsetNode.isChildrenOffsets = true;
                    Arrays.fill(childrenOffsetNode.childrenOffsets, 0);
                    this.nodes.add(childrenOffsetNode);
                    MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>>[] subTree = ((IMultiNoiseUtilSearchTreeTreeBranchNode<RegistryEntry<Biome>>) (Object) branchNode).getSubTree();
                    Assertions.assertTrue(subTree.length <= 7);
                    for (int i = 0; i < subTree.length; i++) {
                        MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>> child = subTree[i];
                        if (child != null) {
                            childrenOffsetNode.childrenOffsets[i] = consume(child, depth + 1);
                        } else {
                            childrenOffsetNode.childrenOffsets[i] = 0; // 0 means no child
                        }
                    }
                } else if (node instanceof MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> leafNode) {
                    int biomeId = this.biomeIdMap.computeIfAbsent(((IMultiNoiseUtilSearchTreeTreeLeafNode<RegistryEntry<Biome>>) (Object) leafNode).getValue(), _ -> this.biomeIdMap.size());
                    serializedNode.isBranch = false;
                    serializedNode.biomeId = biomeId;
                }
                return index;
            }

            public void validate() {
                Iterator<SerializedTreeNode> iterator = this.nodes.iterator();
                iterator.next(); // skip padding node
                while (iterator.hasNext()) {
                    SerializedTreeNode node = iterator.next();
                    if (!node.isBranch && node.isChildrenOffsets) {
                        throw new IllegalStateException("Leaf node cannot have children offsets");
                    }
                    if (node.isBranch) {
                        SerializedTreeNode childrenOffsetsNode = iterator.next();
                        if (!childrenOffsetsNode.isBranch || !childrenOffsetsNode.isChildrenOffsets) {
                            throw new IllegalStateException("Branch node must have children offsets in the next node");
                        }
                        if (!childrenOffsetsNode.isBranch && childrenOffsetsNode.isChildrenOffsets) {
                            throw new IllegalStateException("Leaf node cannot have children offsets");
                        }
                    }
                }
            }

            static class SerializedTreeNode {
                public boolean isBranch;
                public boolean isChildrenOffsets = false;
                public int biomeId;
                public short[] maxs = new short[7];
                public short[] mins = new short[7];
                public int[] childrenOffsets = new int[7];
            }

        }

        TreeFlattener treeFlattener = new TreeFlattener();
        treeFlattener.consume(((IMultiNoiseUtilSearchTree<RegistryEntry<Biome>>) (Object) searchTree).getFirstNode(), 1);
        treeFlattener.validate();

        RegistryEntry<Biome>[] biomes = new RegistryEntry[treeFlattener.biomeIdMap.size()];

        for (ObjectBidirectionalIterator<Object2IntMap.Entry<RegistryEntry<Biome>>> iterator = treeFlattener.biomeIdMap.object2IntEntrySet().fastIterator(); iterator.hasNext(); ) {
            Object2IntMap.Entry<RegistryEntry<Biome>> entry = iterator.next();
            if (biomes[entry.getIntValue()] != null) {
                throw new IllegalStateException("Duplicate biome ID found: " + entry.getIntValue() + " for biome " + entry.getKey());
            } else {
                biomes[entry.getIntValue()] = entry.getKey();
            }
        }


        MemorySegment segment = arena.allocate(treeFlattener.nodes.size() * biome_search_tree_node.byteSize(), 64);
        List<TreeFlattener.SerializedTreeNode> nodes = treeFlattener.nodes;
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            TreeFlattener.SerializedTreeNode node = nodes.get(i);
            MemorySegment slice = segment.asSlice(i * biome_search_tree_node.byteSize(), biome_search_tree_node.byteSize());
            biome_search_tree_node$state.set(slice, 0L, (node.isBranch ? 0x80000000 : 0) | (node.isChildrenOffsets ? 0x40000000 : 0) | node.biomeId);
            if (node.isChildrenOffsets) {
                MemorySegment childrenOffsets = biome_search_tree_node$branch_children$children_offset(slice);
                for (int j = 0; j < 7; j++) {
                    childrenOffsets.set(ValueLayout.JAVA_INT, j * ValueLayout.JAVA_INT.byteSize(), node.childrenOffsets[j]);
                }
            } else {
                MemorySegment maxs = biome_search_tree_node$node_minmaxs$maxs(slice);
                MemorySegment mins = biome_search_tree_node$node_minmaxs$mins(slice);
                for (int j = 0; j < 7; j++) {
                    maxs.set(ValueLayout.JAVA_SHORT, j * ValueLayout.JAVA_SHORT.byteSize(), node.maxs[j]);
                    mins.set(ValueLayout.JAVA_SHORT, j * ValueLayout.JAVA_SHORT.byteSize(), node.mins[j]);
                }
            }
        }

        return new NativeBiomeSearchTree(segment, biomes, treeFlattener.nodes.size(), treeFlattener.treeDepth);
    }

    public record NativeBiomeSearchTree(MemorySegment segment, RegistryEntry<Biome>[] biomes, int node_c, int tree_depth) {
    }

}
