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

package natives.accuracy;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.ISATarget;
import com.ishland.c2me.base.common.util.NoisePacking;
import natives.support.OptimizedPerlinNoise;
import natives.support.ReflectUtils;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.gen.sampler.SampleBuffer;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Random;

public class PerlinNoiseSamplerAccuracy extends AbstractAccuracy {

    private static final double xzScale = 0.25;
    private static final double yScale = 0.125;
    private static final float outputScale = 0.125f;
    private static final int sizeX = 65;
    private static final int sizeY = 129;
    private static final int sizeZ = 65;
    private static final int stepX = 4;
    private static final int stepY = 4;
    private static final int stepZ = 4;

    private final Random random = new Random();
    private final PerlinNoiseSampler vanillaSampler;
    private final double originX;
    private final double originY;
    private final double originZ;
    private final int[] packed512b;
    private final short[] packed512bShort;
    private final char[] packed512bChar;
    private final int[] nativeSamplerDataRaw;
    private final MemorySegment nativeSamplerData;
    private final SampleBuffer output;
    private final MemorySegment outputBuffer;
    private final SampleBuffer outputVanilla;

    protected PerlinNoiseSamplerAccuracy() {
        super(Arrays.stream(ISATarget.getInstance().getEnumConstants()).limit(12).toArray(ISATarget[]::new), BindingsTemplate.c2me_natives_noise_perlin_sample_base_area, "c2me_natives_noise_perlin_sample_base_area");
        LocalRandom random1 = new LocalRandom(random.nextLong());
        this.vanillaSampler = new PerlinNoiseSampler(random1);
        byte[] permutations = (byte[]) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "permutation");
        this.packed512b = NoisePacking.packPermutation512b(permutations);
        this.packed512bShort = NoisePacking.packPermutation512bShort(permutations);
        this.packed512bChar = NoisePacking.packPermutation512bChar(permutations);
        this.originX = (double) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "originX");
        this.originY = (double) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "originY");
        this.originZ = (double) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "originZ");
        this.nativeSamplerDataRaw = NoisePacking.buildFusedPermutationTable(permutations);
        this.nativeSamplerData = MemorySegment.ofArray(NoisePacking.buildFusedPermutationTable(permutations));
        this.output = SampleBuffer.withCount(sizeX * sizeY * sizeZ);
        this.outputBuffer = MemorySegment.ofArray((float[]) ReflectUtils.getField(SampleBuffer.class, this.output, "elems"));
        this.outputVanilla = SampleBuffer.withCount(sizeX * sizeY * sizeZ);
    }

    private void invokeNative(MethodHandle handle, SamplingRegion region) {
        try {
            handle.invokeExact(
                    this.nativeSamplerData,
                    originX, originY, originZ,
                    outputBuffer,
                    region.sizeX(), region.sizeY(), region.sizeZ(),
                    region.minBlockX(), region.minBlockY(), region.minBlockZ(),
                    region.stepBlockX(), region.stepBlockY(), region.stepBlockZ(),
                    xzScale, yScale, outputScale
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeVanilla(SamplingRegion region) {
        vanillaSampler.fill(outputVanilla, region, xzScale, yScale, outputScale);
    }

    private void invokeOptimized(SamplingRegion region) {
        OptimizedPerlinNoise.fillBase(packed512bShort, originX, originY, originZ, outputVanilla, region, xzScale, yScale, outputScale);
    }

    private void loopBody() {
        int x = random.nextInt(-30000000, 30000000);
        int y = random.nextInt(-30000000, 30000000);
        int z = random.nextInt(-30000000, 30000000);

        SamplingRegion region = new SamplingRegion(
                sizeX, sizeY, sizeZ,
                x, y, z,
                stepX, stepY, stepZ
        );

        outputVanilla.fill(0.0f);
        invokeOptimized(region);
        for (int i = 0; i < this.MHs.length; i ++) {
            output.fill(0.0f);
            invokeNative(this.MHs[i], region);

            for (int j = 0; j < outputVanilla.count(); j ++) {
                float original = outputVanilla.get(j);
                float actual = output.get(j);
                long ulpDiff = ulpDistance(original, actual);
                if (ulpDiff > this.maxUlp[i]) {
                    this.maxUlp[i] = ulpDiff;
                    System.out.println(String.format("%s: new max error %d ulps at x=%d, z=%d (expected %.10g but got %.10g)", this.targets[i], ulpDiff, x, z, original, actual));
                }
            }
        }
    }

    public static void main(String[] args) {
        final long printInterval = 10_000_000_000L;
        PerlinNoiseSamplerAccuracy instance = new PerlinNoiseSamplerAccuracy();
        long lastPrint = System.nanoTime();
        for (long iter = 0; ; iter ++) {
            instance.loopBody();
            if ((iter & (1L << 16L - 1L)) == 0) {
                long nanoTime = System.nanoTime();
                if (nanoTime > (lastPrint + printInterval)) {
                    lastPrint += printInterval;
                    System.out.println("=".repeat(30));
                    System.out.println(String.format("Iterations: %d", iter));
                    instance.printUlps();
                    System.out.println("=".repeat(30));
                }
            }
        }
    }

}
