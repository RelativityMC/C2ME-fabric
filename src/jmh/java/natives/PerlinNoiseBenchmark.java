package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.LocalRandom;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(PerlinNoiseBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerlinNoiseBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    private final PerlinNoiseSampler vanillaSampler = new PerlinNoiseSampler(new LocalRandom(0xFF));

    private final byte[] permutations;
    private final MemorySegment permutationsSegment;
    private static final double[] FLAT_SIMPLEX_GRAD = new double[]{
            1, 1, 0, 0,
            -1, 1, 0, 0,
            1, -1, 0, 0,
            -1, -1, 0, 0,
            1, 0, 1, 0,
            -1, 0, 1, 0,
            1, 0, -1, 0,
            -1, 0, -1, 0,
            0, 1, 1, 0,
            0, -1, 1, 0,
            0, 1, -1, 0,
            0, -1, -1, 0,
            1, 1, 0, 0,
            0, -1, 1, 0,
            -1, 1, 0, 0,
            0, -1, -1, 0,
    };
    private final double originX = vanillaSampler.originX;
    private final double originY = vanillaSampler.originY;
    private final double originZ = vanillaSampler.originZ;

    private double optimizedSample(double x, double y, double z, double yScale, double yMax) {
        double d = x + this.originX;
        double e = y + this.originY;
        double f = z + this.originZ;
        double i = Math.floor(d);
        double j = Math.floor(e);
        double k = Math.floor(f);
        double g = d - i;
        double h = e - j;
        double l = f - k;
        double o = 0.0D;
        if (yScale != 0.0) {
            double m;
            if (yMax >= 0.0 && yMax < h) {
                m = yMax;
            } else {
                m = h;
            }

            o = Math.floor(m / yScale + 1.0E-7F) * yScale;
        }

        return this.optimizedSample0((int) i, (int) j, (int) k, g, h - o, l, h);
    }

    private double optimizedSample0(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX) {
        final int var0 = sectionX & 0xFF;
        final int var1 = (sectionX + 1) & 0xFF;
        final int var2 = this.permutations[var0] & 0xFF;
        final int var3 = this.permutations[var1] & 0xFF;
        final int var4 = (var2 + sectionY) & 0xFF;
        final int var5 = (var2 + sectionY + 1) & 0xFF;
        final int var6 = (var3 + sectionY) & 0xFF;
        final int var7 = (var3 + sectionY + 1) & 0xFF;
        final int var8 = this.permutations[var4] & 0xFF;
        final int var9 = this.permutations[var5] & 0xFF;
        final int var10 = this.permutations[var6] & 0xFF;
        final int var11 = this.permutations[var7] & 0xFF;

        final int var12 = (var8 + sectionZ) & 0xFF;
        final int var13 = (var10 + sectionZ) & 0xFF;
        final int var14 = (var9 + sectionZ) & 0xFF;
        final int var15 = (var11 + sectionZ) & 0xFF;
        final int var16 = (var8 + sectionZ + 1) & 0xFF;
        final int var17 = (var10 + sectionZ + 1) & 0xFF;
        final int var18 = (var9 + sectionZ + 1) & 0xFF;
        final int var19 = (var11 + sectionZ + 1) & 0xFF;
        final int var20 = (this.permutations[var12] & 15) << 2;
        final int var21 = (this.permutations[var13] & 15) << 2;
        final int var22 = (this.permutations[var14] & 15) << 2;
        final int var23 = (this.permutations[var15] & 15) << 2;
        final int var24 = (this.permutations[var16] & 15) << 2;
        final int var25 = (this.permutations[var17] & 15) << 2;
        final int var26 = (this.permutations[var18] & 15) << 2;
        final int var27 = (this.permutations[var19] & 15) << 2;
        final double var60 = localX - 1.0;
        final double var61 = localY - 1.0;
        final double var62 = localZ - 1.0;
        final double var87 = FLAT_SIMPLEX_GRAD[(var20) | 0] * localX + FLAT_SIMPLEX_GRAD[(var20) | 1] * localY + FLAT_SIMPLEX_GRAD[(var20) | 2] * localZ;
        final double var88 = FLAT_SIMPLEX_GRAD[(var21) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var21) | 1] * localY + FLAT_SIMPLEX_GRAD[(var21) | 2] * localZ;
        final double var89 = FLAT_SIMPLEX_GRAD[(var22) | 0] * localX + FLAT_SIMPLEX_GRAD[(var22) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var22) | 2] * localZ;
        final double var90 = FLAT_SIMPLEX_GRAD[(var23) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var23) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var23) | 2] * localZ;
        final double var91 = FLAT_SIMPLEX_GRAD[(var24) | 0] * localX + FLAT_SIMPLEX_GRAD[(var24) | 1] * localY + FLAT_SIMPLEX_GRAD[(var24) | 2] * var62;
        final double var92 = FLAT_SIMPLEX_GRAD[(var25) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var25) | 1] * localY + FLAT_SIMPLEX_GRAD[(var25) | 2] * var62;
        final double var93 = FLAT_SIMPLEX_GRAD[(var26) | 0] * localX + FLAT_SIMPLEX_GRAD[(var26) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var26) | 2] * var62;
        final double var94 = FLAT_SIMPLEX_GRAD[(var27) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var27) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var27) | 2] * var62;

        final double var95 = localX * 6.0 - 15.0;
        final double var96 = fadeLocalX * 6.0 - 15.0;
        final double var97 = localZ * 6.0 - 15.0;
        final double var98 = localX * var95 + 10.0;
        final double var99 = fadeLocalX * var96 + 10.0;
        final double var100 = localZ * var97 + 10.0;
        final double var101 = localX * localX * localX * var98;
        final double var102 = fadeLocalX * fadeLocalX * fadeLocalX * var99;
        final double var103 = localZ * localZ * localZ * var100;

        final double var113 = var87 + var101 * (var88 - var87);
        final double var114 = var93 + var101 * (var94 - var93);
        final double var115 = var91 + var101 * (var92 - var91);
        final double var117 = var114 - var115;
        final double var118 = var102 * (var89 + var101 * (var90 - var89) - var113);
        final double var119 = var102 * var117;
        final double var120 = var113 + var118;
        final double var121 = var115 + var119;
        return var120 + (var103 * (var121 - var120));
    }

    {
        try {
            final Field permutationsField = PerlinNoiseSampler.class.getDeclaredField("permutation");
            permutationsField.setAccessible(true);
            permutations = (byte[]) permutationsField.get(vanillaSampler);
            permutationsSegment = MemorySegment.ofArray(permutations);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public PerlinNoiseBenchmark() {
        super(BindingsTemplate.c2me_natives_noise_perlin_sample, "c2me_natives_noise_perlin_sample");
    }

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];
    private final double[] yScale = new double[invocations];
    private final double[] yMax = new double[invocations];

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            sampleY[i] = random.nextDouble(-2048.0D, 2048.0D);
            sampleZ[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            yScale[i] = random.nextDouble(-1.0D, 1.0D) * (random.nextBoolean() ? 1 : 0);
            yMax[i] = random.nextDouble(-2048.0D, 2048.0D);
        }
    }

    @Override
    @Benchmark
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(vanillaSampler.sample(sampleX[i], sampleY[i], sampleZ[i], yScale[i], yMax[i]));
        }
    }

    @Benchmark
    public void optimizedSampler(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(optimizedSample(sampleX[i], sampleY[i], sampleZ[i], yScale[i], yMax[i]));
        }
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            try {
                bh.consume((double) handle.invokeExact(permutationsSegment, originX, originY, originZ, sampleX[i], sampleY[i], sampleZ[i], yScale[i], yMax[i]));
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @Override
    @Benchmark
    public void spinning(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(sampleX[i] + sampleY[i] + sampleZ[i] + yScale[i] + yMax[i]);
        }
    }

}
