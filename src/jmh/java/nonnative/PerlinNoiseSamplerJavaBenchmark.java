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

package nonnative;

import com.ishland.c2me.base.common.util.NoisePacking;
import natives.support.ReflectUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OperationsPerInvocation(PerlinNoiseSamplerJavaBenchmark.invocations * PerlinNoiseSamplerJavaBenchmark.SAMPLER_COUNT)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerlinNoiseSamplerJavaBenchmark {

    public static final int SAMPLER_COUNT = 4096;

    public static final int invocations = 32;

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];

    private PerlinNoiseSampler[] vanillaSamplers;
    private VendoredOptimizedPerlinSampler[] optimizedSamplers;

    @Setup(Level.Trial)
    public void setup() {
        this.vanillaSamplers = new PerlinNoiseSampler[SAMPLER_COUNT];
        this.optimizedSamplers = new VendoredOptimizedPerlinSampler[SAMPLER_COUNT];

        final net.minecraft.util.math.random.Random samplerRandom = net.minecraft.util.math.random.Random.create(0x1234);

        for (int s = 0; s < SAMPLER_COUNT; s++) {
            final PerlinNoiseSampler vanilla = new PerlinNoiseSampler(samplerRandom);
            this.vanillaSamplers[s] = vanilla;

            final byte[] permutation = (byte[]) ReflectUtils.getField(
                    LatticedNoiseSampler.class, vanilla, "permutation");
            final double originX = (double) ReflectUtils.getField(
                    LatticedNoiseSampler.class, vanilla, "originX");
            final double originY = (double) ReflectUtils.getField(
                    LatticedNoiseSampler.class, vanilla, "originY");
            final double originZ = (double) ReflectUtils.getField(
                    LatticedNoiseSampler.class, vanilla, "originZ");

            this.optimizedSamplers[s] = new VendoredOptimizedPerlinSampler(
                    permutation, originX, originY, originZ);
        }

        final Random pointRandom = new Random(0xC0FFEEL);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = pointRandom.nextDouble(-102400.0, 1024000.0);
            sampleY[i] = pointRandom.nextDouble(-102400.0, 1024000.0);
            sampleZ[i] = pointRandom.nextDouble(-102400.0, 1024000.0);
        }

        Collections.shuffle(Arrays.asList(this.optimizedSamplers), new Random(0x1234));
        Collections.shuffle(Arrays.asList(this.vanillaSamplers), new Random(0x1234));

        for (int s = 0; s < SAMPLER_COUNT; s++) {
            final PerlinNoiseSampler vanilla = this.vanillaSamplers[s];
            final VendoredOptimizedPerlinSampler optimized = this.optimizedSamplers[s];
            for (int i = 0; i < invocations; i++) {
                final float a = vanilla.sample(sampleX[i], sampleY[i], sampleZ[i]);
                final float b = optimized.sample(sampleX[i], sampleY[i], sampleZ[i]);
                if (a != b) {
                    throw new AssertionError(
                            "Sampler " + s + " mismatch at i=" + i
                                    + ": vanilla=" + a + " optimized=" + b);
                }
            }
        }

        Collections.shuffle(Arrays.asList(this.optimizedSamplers), new Random(0x1234));
        Collections.shuffle(Arrays.asList(this.vanillaSamplers), new Random(0x1234));
    }

    @Setup(Level.Iteration)
    public void evil(Blackhole bh) {
        Collections.shuffle(Arrays.asList(this.optimizedSamplers), new Random(0x1234));
        Collections.shuffle(Arrays.asList(this.vanillaSamplers), new Random(0x1234));
        for (int i = 0; i < SAMPLER_COUNT; i ++) {
            bh.consume(new int[1024]);
        }
        Collections.shuffle(Arrays.asList(this.optimizedSamplers), new Random(0x1234));
        Collections.shuffle(Arrays.asList(this.vanillaSamplers), new Random(0x1234));
    }

    @Benchmark
    public void vanilla(Blackhole bh) {
        final PerlinNoiseSampler[] samplers = this.vanillaSamplers;
        final double[] xs = this.sampleX;
        final double[] ys = this.sampleY;
        final double[] zs = this.sampleZ;
        for (int i = 0; i < invocations; i++) {
            final double x = xs[i], y = ys[i], z = zs[i];
            for (int s = 0; s < SAMPLER_COUNT; s++) {
                bh.consume(samplers[s].sample(x, y, z));
            }
        }
    }

    @Benchmark
    public void optimized(Blackhole bh) {
        final VendoredOptimizedPerlinSampler[] samplers = this.optimizedSamplers;
        final double[] xs = this.sampleX;
        final double[] ys = this.sampleY;
        final double[] zs = this.sampleZ;
        for (int i = 0; i < invocations; i++) {
            final double x = xs[i], y = ys[i], z = zs[i];
            for (int s = 0; s < SAMPLER_COUNT; s++) {
                bh.consume(samplers[s].sample(x, y, z));
            }
        }
    }

    public static final class VendoredOptimizedPerlinSampler {

        private static final double MAX_SAFE_ABS_COORDINATE = Math.nextDown(1.6777216E7);

        private final byte[] permutation;
        private final double originX;
        private final double originY;
        private final double originZ;

        private short[] c2me$packedPermutations;

        VendoredOptimizedPerlinSampler(byte[] permutation,
                                       double originX, double originY, double originZ) {
            this.permutation = permutation;
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.c2me$initPackedPermutations();
        }

        private short[] c2me$initPackedPermutations() {
            short[] packed = this.c2me$packedPermutations;
            if (packed != null) return packed;
            packed = NoisePacking.packPermutation512bShort(this.permutation);
            this.c2me$packedPermutations = packed;
            return packed;
        }

        public float sample(double x, double y, double z) {
            double xx = wrapCoord(x) + this.originX;
            double yx = wrapCoord(y) + this.originY;
            double zx = wrapCoord(z) + this.originZ;
            double floorX = Math.floor(xx);
            double floorY = Math.floor(yx);
            double floorZ = Math.floor(zx);
            float relativeX = (float) (xx - floorX);
            float relativeY = (float) (yx - floorY);
            float relativeZ = (float) (zx - floorZ);
            return this.sample((int) floorX, (int) floorY, (int) floorZ, relativeX, relativeY, relativeZ, relativeY);
        }

        protected static double wrapCoord(final double coordinate) {
            return coordinate >= -MAX_SAFE_ABS_COORDINATE && coordinate < MAX_SAFE_ABS_COORDINATE
                    ? coordinate
                    : coordinate - Math.floor(coordinate / 3.3554432E7 + 0.5) * 3.3554432E7;
        }

        private float sample(final int px0, final int py0, final int pz0,
                             final float fx0, final float fy0, final float fz0,
                             final float fadeLocalY) {
            final byte[] permutations = this.permutation;

            if (permutations.length != 256) throw new AssertionError("permutations.length != 256");

            final int hash0__ = permutations[px0 & 0xff];
            final int hash1__ = permutations[(px0 + 1) & 0xff];
            final int hash00_ = permutations[(hash0__ + py0) & 0xff];
            final int hash01_ = permutations[(hash0__ + py0 + 1) & 0xff];
            final int hash10_ = permutations[(hash1__ + py0) & 0xff];
            final int hash11_ = permutations[(hash1__ + py0 + 1) & 0xff];
            final float fx1 = fx0 - 1.0f;
            final float fy1 = fy0 - 1.0f;
            final float fz1 = fz0 - 1.0f;
            final int hash000 = permutations[(hash00_ + pz0) & 0xff] & 0xf;
            final float f000 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash000 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash000 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash000 << 2) + 2] * fz0;
            final int hash001 = permutations[(hash00_ + pz0 + 1) & 0xff] & 0xf;
            final float f001 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash001 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash001 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash001 << 2) + 2] * fz1;
            final int hash010 = permutations[(hash01_ + pz0) & 0xff] & 0xf;
            final float f010 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash010 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash010 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash010 << 2) + 2] * fz0;
            final int hash011 = permutations[(hash01_ + pz0 + 1) & 0xff] & 0xf;
            final float f011 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash011 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash011 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash011 << 2) + 2] * fz1;
            final int hash100 = permutations[(hash10_ + pz0) & 0xff] & 0xf;
            final float f100 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash100 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash100 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash100 << 2) + 2] * fz0;
            final int hash101 = permutations[(hash10_ + pz0 + 1) & 0xff] & 0xf;
            final float f101 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash101 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash101 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash101 << 2) + 2] * fz1;
            final int hash110 = permutations[(hash11_ + pz0) & 0xff] & 0xf;
            final float f110 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash110 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash110 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash110 << 2) + 2] * fz0;
            final int hash111 = permutations[(hash11_ + pz0 + 1) & 0xff] & 0xf;
            final float f111 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash111 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash111 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash111 << 2) + 2] * fz1;

            final float dx = MathHelper.perlinFade(fx0);
            final float dy = MathHelper.perlinFade(fadeLocalY);
            final float dz = MathHelper.perlinFade(fz0);

            float lerpx00 = MathHelper.lerp(dx, f000, f100);
            float lerpx10 = MathHelper.lerp(dx, f010, f110);
            float lerpx01 = MathHelper.lerp(dx, f001, f101);
            float lerpx11 = MathHelper.lerp(dx, f011, f111);
            float lerpxx0 = MathHelper.lerp(dy, lerpx00, lerpx10);
            float lerpxx1 = MathHelper.lerp(dy, lerpx01, lerpx11);
            float lerpxxx = MathHelper.lerp(dz, lerpxx0, lerpxx1);

//        if (res != this.c2me$sampleVanillaSingle(px0, py0, pz0, fx0, fy0, fz0, fadeLocalY)) {
//            throw new AssertionError();
//        }

            return lerpxxx;
        }

        private float sample1(final int px0, final int py0, final int pz0,
                              final float fx0, final float fy0, final float fz0,
                              final float fadeLocalY) {
            final short[] permutations = this.c2me$initPackedPermutations();

            if (permutations.length != 256) throw new AssertionError();

            final float fx1 = fx0 - 1.0f;
            final float fy1 = fy0 - 1.0f;
            final float fz1 = fz0 - 1.0f;

            final int hashr__ = permutations[px0 & 0xff];
            final int hash0__ = hashr__ & 0xff;
            final int hash1__ = (hashr__ >>> 8) & 0xff;
            final int hash0r_ = permutations[(hash0__ + py0) & 0xff];
            final int hash1r_ = permutations[(hash1__ + py0) & 0xff];
            final int hash00_ = hash0r_ & 0xff;
            final int hash01_ = (hash0r_ >>> 8) & 0xff;
            final int hash10_ = hash1r_ & 0xff;
            final int hash11_ = (hash1r_ >>> 8) & 0xff;
            final int hash00r = permutations[(hash00_ + pz0) & 0xff];
            final int hash10r = permutations[(hash10_ + pz0) & 0xff];
            final int hash01r = permutations[(hash01_ + pz0) & 0xff];
            final int hash11r = permutations[(hash11_ + pz0) & 0xff];
            final int hash000 = hash00r & 0xf;
            final int hash100 = hash10r & 0xf;
            final int hash010 = hash01r & 0xf;
            final int hash110 = hash11r & 0xf;
            final int hash001 = (hash00r >>> 8) & 0xf;
            final int hash101 = (hash10r >>> 8) & 0xf;
            final int hash011 = (hash01r >>> 8) & 0xf;
            final int hash111 = (hash11r >>> 8) & 0xf;

            final int i000 = hash000 << 2;
            final int i100 = hash100 << 2;
            final int i010 = hash010 << 2;
            final int i110 = hash110 << 2;
            final int i001 = hash001 << 2;
            final int i101 = hash101 << 2;
            final int i011 = hash011 << 2;
            final int i111 = hash111 << 2;

            final float f000 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i000] * fx0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i000 + 1] * fy0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i000 + 2] * fz0;

            final float f100 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i100] * fx1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i100 + 1] * fy0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i100 + 2] * fz0;

            final float f010 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i010] * fx0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i010 + 1] * fy1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i010 + 2] * fz0;

            final float f110 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i110] * fx1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i110 + 1] * fy1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i110 + 2] * fz0;

            final float f001 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i001] * fx0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i001 + 1] * fy0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i001 + 2] * fz1;

            final float f101 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i101] * fx1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i101 + 1] * fy0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i101 + 2] * fz1;

            final float f011 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i011] * fx0
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i011 + 1] * fy1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i011 + 2] * fz1;

            final float f111 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[i111] * fx1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i111 + 1] * fy1
                    + NoisePacking.FLAT_SIMPLEX_GRAD_F32[i111 + 2] * fz1;

            final float dx = MathHelper.perlinFade(fx0);
            final float dy = MathHelper.perlinFade(fadeLocalY);
            final float dz = MathHelper.perlinFade(fz0);

            float lerpx00 = MathHelper.lerp(dx, f000, f100);
            float lerpx10 = MathHelper.lerp(dx, f010, f110);
            float lerpx01 = MathHelper.lerp(dx, f001, f101);
            float lerpx11 = MathHelper.lerp(dx, f011, f111);
            float lerpxx0 = MathHelper.lerp(dy, lerpx00, lerpx10);
            float lerpxx1 = MathHelper.lerp(dy, lerpx01, lerpx11);
            float lerpxxx = MathHelper.lerp(dz, lerpxx0, lerpxx1);

            return lerpxxx;
        }
    }
}