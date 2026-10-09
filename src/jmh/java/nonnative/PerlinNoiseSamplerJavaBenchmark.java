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

        private volatile int[] c2me$packedPermutations;

        VendoredOptimizedPerlinSampler(byte[] permutation,
                                       double originX, double originY, double originZ) {
            this.permutation = permutation;
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
        }

        private int[] c2me$initPackedPermutations() {
            int[] packed = this.c2me$packedPermutations;
            if (packed != null) return packed;
            packed = NoisePacking.packPermutation0(this.permutation);
            this.c2me$packedPermutations = packed;
            return packed;
        }

        public float sample(double x, double y, double z) {
            double xx = wrapCoord(x) + this.originX;
            double yx = wrapCoord(y) + this.originY;
            double zx = wrapCoord(z) + this.originZ;
            int floorX = MathHelper.floor(xx);
            int floorY = MathHelper.floor(yx);
            int floorZ = MathHelper.floor(zx);
            float relativeX = (float) (xx - floorX);
            float relativeY = (float) (yx - floorY);
            float relativeZ = (float) (zx - floorZ);
            return this.sample(floorX, floorY, floorZ, relativeX, relativeY, relativeZ, relativeY);
        }

        protected static double wrapCoord(final double coordinate) {
            return coordinate >= -MAX_SAFE_ABS_COORDINATE && coordinate < MAX_SAFE_ABS_COORDINATE
                    ? coordinate
                    : coordinate - Math.floor(coordinate / 3.3554432E7 + 0.5) * 3.3554432E7;
        }

        private float sample(final int px0, final int py0, final int pz0,
                             final float fx0, final float fy0, final float fz0,
                             final float fadeLocalY) {
            final int[] permutations = this.c2me$initPackedPermutations();

            final float fx1 = fx0 - 1.0f;
            final float fy1 = fy0 - 1.0f;
            final float fz1 = fz0 - 1.0f;

            final int hashr__ = NoisePacking.indexPackedPermutation(permutations, px0);
            final int hash0__ = hashr__ & 0xff;
            final int hash1__ = (hashr__ >>> 8) & 0xff;
            final int hash0r_ = NoisePacking.indexPackedPermutation(permutations, hash0__ + py0);
            final int hash1r_ = NoisePacking.indexPackedPermutation(permutations, hash1__ + py0);
            final int hash00_ = hash0r_ & 0xff;
            final int hash01_ = (hash0r_ >>> 8) & 0xff;
            final int hash10_ = hash1r_ & 0xff;
            final int hash11_ = (hash1r_ >>> 8) & 0xff;
            final int hash00r = NoisePacking.indexPackedPermutation(permutations, hash00_ + pz0);
            final int hash10r = NoisePacking.indexPackedPermutation(permutations, hash10_ + pz0);
            final int hash01r = NoisePacking.indexPackedPermutation(permutations, hash01_ + pz0);
            final int hash11r = NoisePacking.indexPackedPermutation(permutations, hash11_ + pz0);
            final int hash000 = hash00r & 0xf;
            final int hash100 = hash10r & 0xf;
            final int hash010 = hash01r & 0xf;
            final int hash110 = hash11r & 0xf;
            final int hash001 = (hash00r >>> 8) & 0xf;
            final int hash101 = (hash10r >>> 8) & 0xf;
            final int hash011 = (hash01r >>> 8) & 0xf;
            final int hash111 = (hash11r >>> 8) & 0xf;

//            final int res000 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash000];
//            final int res100 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash100];
//            final int res010 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash010];
//            final int res110 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash110];
//            final int res001 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash001];
//            final int res101 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash101];
//            final int res011 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash011];
//            final int res111 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash111];
//
//            final float f000 = NoisePacking.gradX(res000) * fx0 + NoisePacking.gradY(res000) * fy0 + NoisePacking.gradZ(res000) * fz0;
//            final float f100 = NoisePacking.gradX(res100) * fx1 + NoisePacking.gradY(res100) * fy0 + NoisePacking.gradZ(res100) * fz0;
//            final float f010 = NoisePacking.gradX(res010) * fx0 + NoisePacking.gradY(res010) * fy1 + NoisePacking.gradZ(res010) * fz0;
//            final float f110 = NoisePacking.gradX(res110) * fx1 + NoisePacking.gradY(res110) * fy1 + NoisePacking.gradZ(res110) * fz0;
//            final float f001 = NoisePacking.gradX(res001) * fx0 + NoisePacking.gradY(res001) * fy0 + NoisePacking.gradZ(res001) * fz1;
//            final float f101 = NoisePacking.gradX(res101) * fx1 + NoisePacking.gradY(res101) * fy0 + NoisePacking.gradZ(res101) * fz1;
//            final float f011 = NoisePacking.gradX(res011) * fx0 + NoisePacking.gradY(res011) * fy1 + NoisePacking.gradZ(res011) * fz1;
//            final float f111 = NoisePacking.gradX(res111) * fx1 + NoisePacking.gradY(res111) * fy1 + NoisePacking.gradZ(res111) * fz1;

            // Base indices into FLAT_SIMPLEX_GRAD_F32 (each gradient is 4 floats)
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

            return MathHelper.lerp3(dx, dy, dz,
                    f000, f100, f010, f110,
                    f001, f101, f011, f111);
        }
    }
}