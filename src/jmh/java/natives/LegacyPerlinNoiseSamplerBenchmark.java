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

package natives;

import com.ishland.c2me.base.common.util.MemoryUtil;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import natives.support.ReflectUtils;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.LegacyPerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.gen.sampler.SampleBuffer;
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

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(LegacyPerlinNoiseSamplerBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LegacyPerlinNoiseSamplerBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    private static final double xzScale = 0.25;
    private static final double yScale = 0.5;
    private static final double fudgedYScale = 4.2;
    private static final float outputScale = 0.125f;
    private static final int sizeX = 5;
    private static final int sizeY = 49;
    private static final int sizeZ = 5;
    private static final int stepX = 4;
    private static final int stepY = 8;
    private static final int stepZ = 4;

    private final SamplingRegion[] regions = new SamplingRegion[invocations];
    private PerlinNoiseSampler vanillaSampler;
    private double originX;
    private double originY;
    private double originZ;
    private MemorySegment nativeSamplerData;
    private SampleBuffer output;
    private MemorySegment outputBuffer;

    public LegacyPerlinNoiseSamplerBenchmark() {
        super(BindingsTemplate.c2me_natives_noise_perlin_sample_legacy_area, "c2me_natives_noise_perlin_sample_legacy_area");
    }

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            regions[i] = new SamplingRegion(
                    sizeX, sizeY, sizeZ,
                    random.nextInt(-30000000, 30000000),
                    random.nextInt(-30000000, 30000000),
                    random.nextInt(-30000000, 30000000),
                    stepX, stepY, stepZ
            );
        }
        LocalRandom random1 = new LocalRandom(random.nextLong());
        this.vanillaSampler = new LegacyPerlinNoiseSampler(random1, fudgedYScale);
        int[] permutation = (int[]) MemoryUtil.packByte2int((byte[]) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "permutation"));
        this.originX = (double) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "originX");
        this.originY = (double) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "originY");
        this.originZ = (double) ReflectUtils.getField(LatticedNoiseSampler.class, this.vanillaSampler, "originZ");
        this.nativeSamplerData = MemorySegment.ofArray(permutation);
        this.output = SampleBuffer.withCount(sizeX * sizeY * sizeZ);
        this.outputBuffer = MemorySegment.ofArray((float[]) ReflectUtils.getField(SampleBuffer.class, this.output, "elems"));
        VarHandle.fullFence();
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        this.output.fill(0.0f);
        for (int i = 0; i < invocations; i++) {
            try {
                SamplingRegion region = this.regions[i];
                handle.invokeExact(
                        this.nativeSamplerData,
                        originX, originY, originZ, fudgedYScale,
                        outputBuffer,
                        region.sizeX(), region.sizeY(), region.sizeZ(),
                        region.minBlockX(), region.minBlockY(), region.minBlockZ(),
                        region.stepBlockX(), region.stepBlockY(), region.stepBlockZ(),
                        MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL,
                        xzScale, yScale, outputScale
                );
                bh.consume(this.output);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    @Override
    public void spinning(Blackhole bh) {
        this.output.fill(0.0f);
        for (int i = 0; i < invocations; i++) {
            bh.consume(regions[i]);
            bh.consume(regions[i].stepBlockX());
        }
    }

    @Benchmark
    @Override
    public void vanilla(Blackhole bh) {
        this.output.fill(0.0f);
        for (int i = 0; i < invocations; i++) {
            this.vanillaSampler.fill(this.output, this.regions[i], xzScale, yScale, outputScale);
            bh.consume(this.output);
        }
    }
}
