package com.ishland.c2me.natives.common;

import com.ishland.c2me.base.mixin.access.IPerlinNoiseSampler;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.util.Arrays;

public class NativesUtils {

    public static long createInterpolatedSamplerPointer(Object sampler, Class<?> clazz) {
        try {
            final OctavePerlinNoiseSampler lowerInterpolatedNoise = (OctavePerlinNoiseSampler) accessible(clazz.getDeclaredField("lowerInterpolatedNoise")).get(sampler);
            final OctavePerlinNoiseSampler upperInterpolatedNoise = (OctavePerlinNoiseSampler) accessible(clazz.getDeclaredField("upperInterpolatedNoise")).get(sampler);
            final OctavePerlinNoiseSampler interpolationNoise = (OctavePerlinNoiseSampler) accessible(clazz.getDeclaredField("interpolationNoise")).get(sampler);
            final double field_38271 = (double) accessible(clazz.getDeclaredField("field_38271")).get(sampler);
            final double field_38272 = (double) accessible(clazz.getDeclaredField("field_38272")).get(sampler);
            final double xzScale = (double) accessible(clazz.getDeclaredField("xzScale")).get(sampler);
            final double yScale = (double) accessible(clazz.getDeclaredField("yScale")).get(sampler);
            final double xzFactor = (double) accessible(clazz.getDeclaredField("xzFactor")).get(sampler);
            final double yFactor = (double) accessible(clazz.getDeclaredField("yFactor")).get(sampler);
            final double smearScaleMultiplier = (double) accessible(clazz.getDeclaredField("smearScaleMultiplier")).get(sampler);
            final double maxValue = (double) accessible(clazz.getDeclaredField("maxValue")).get(sampler);
            return NativeInterface.createPerlinInterpolatedSamplerData(
                    createOctaveSamplerPointer(lowerInterpolatedNoise),
                    createOctaveSamplerPointer(upperInterpolatedNoise),
                    createOctaveSamplerPointer(interpolationNoise),
                    field_38271,
                    field_38272,
                    xzScale,
                    yScale,
                    xzFactor,
                    yFactor,
                    smearScaleMultiplier,
                    maxValue
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @VisibleForTesting
    public static long createOctaveSamplerPointer(OctavePerlinNoiseSampler sampler) {
        try {
            final PerlinNoiseSampler[] octaveSamplers = (PerlinNoiseSampler[]) accessible(OctavePerlinNoiseSampler.class.getDeclaredField("octaveSamplers")).get(sampler);
            final double[] amplitudes = ((DoubleList) accessible(OctavePerlinNoiseSampler.class.getDeclaredField("amplitudes")).get(sampler)).toDoubleArray();
            final double lacunarity = (double) accessible(OctavePerlinNoiseSampler.class.getDeclaredField("lacunarity")).get(sampler);
            final double persistence = (double) accessible(OctavePerlinNoiseSampler.class.getDeclaredField("persistence")).get(sampler);

            return createOctaveSamplerPointer(sampler, octaveSamplers, Arrays.stream(octaveSamplers).map(NativesUtils::getPermutations).distinct().toArray(byte[][]::new), amplitudes, lacunarity, persistence);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static byte[] getPermutations(PerlinNoiseSampler sampler) {
        try {
            return (byte[]) accessible(PerlinNoiseSampler.class.getDeclaredField("permutation")).get(sampler);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long createOctaveSamplerPointer(Object owner, PerlinNoiseSampler[] octaveSamplers, byte[][] permutations, double[] amplitudes, double lacunarity, double persistence) {
        final int size = octaveSamplers.length;
        final long ptr_indexes = NativeMemoryTracker.allocateMemory(owner, size * 8L);
        final long ptr_sampler_permutations = NativeMemoryTracker.allocateMemory(owner, size * 256L);
        final long ptr_sampler_originX = NativeMemoryTracker.allocateMemory(owner, size * 8L);
        final long ptr_sampler_originY = NativeMemoryTracker.allocateMemory(owner, size * 8L);
        final long ptr_sampler_originZ = NativeMemoryTracker.allocateMemory(owner, size * 8L);
        final long ptr_amplitudes = NativeMemoryTracker.allocateMemory(owner, size * 8L);
        int pos = 0;
        for (int i = 0; i < size; i ++) {
            final PerlinNoiseSampler sampler = octaveSamplers[i];
            if (sampler != null) {
                UnsafeUtil.getInstance().putLong(ptr_indexes + pos * 8L, i);
                if (sampler instanceof IPerlinNoiseSampler) {
                    PlatformDependent.copyMemory(((IPerlinNoiseSampler) sampler).getPermutation(), 0, ptr_sampler_permutations + 256L * pos, 256);
                } else {
                    PlatformDependent.copyMemory(permutations[i], 0, ptr_sampler_permutations + 256L * pos, 256);
                }
                UnsafeUtil.getInstance().putDouble(ptr_sampler_originX + pos * 8L, sampler.originX);
                //noinspection SuspiciousNameCombination
                UnsafeUtil.getInstance().putDouble(ptr_sampler_originY + pos * 8L, sampler.originY);
                UnsafeUtil.getInstance().putDouble(ptr_sampler_originZ + pos * 8L, sampler.originZ);
                UnsafeUtil.getInstance().putDouble(ptr_amplitudes + pos * 8L, amplitudes[i]);
                pos ++;
            }
        }
        long octaveSamplerDataPointer = NativeInterface.createPerlinOctaveSamplerData(
                lacunarity,
                persistence,
                pos,
                octaveSamplers.length,
                ptr_indexes,
                ptr_sampler_permutations,
                ptr_sampler_originX,
                ptr_sampler_originY,
                ptr_sampler_originZ,
                ptr_amplitudes
        );
        NativeMemoryTracker.registerAllocatedMemory(owner, NativeInterface.SIZEOF_octave_sampler_data, octaveSamplerDataPointer);
        return octaveSamplerDataPointer;
    }

    private static Field accessible(Field field) {
        field.setAccessible(true);
        return field;
    }

}
