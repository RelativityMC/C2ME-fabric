package noise;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.random.SimpleRandom;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SimplexNoiseBenchmark {

    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double SKEW_FACTOR_2D = 0.5 * (SQRT_3 - 1.0);
    private static final double UNSKEW_FACTOR_2D = (3.0 - SQRT_3) / 6.0;
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

    private final SimplexNoiseSampler vanillaSampler = new SimplexNoiseSampler(new SimpleRandom(0xFF));

    private final int[] permutations;

    {
        try {
            final Field permutationsField = SimplexNoiseSampler.class.getDeclaredField("permutations");
            permutationsField.setAccessible(true);
            permutations = (int[]) permutationsField.get(vanillaSampler);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private double grad(int hash, double x, double y, double z, double distance) {
        double d = distance - x * x - y * y - z * z;
        if (d < 0.0) {
            return 0.0;
        } else {
            final int i = hash << 2;
            final double var0 = FLAT_SIMPLEX_GRAD[i | 0] * x;
            final double var1 = FLAT_SIMPLEX_GRAD[i | 1] * y;
            final double var2 = FLAT_SIMPLEX_GRAD[i | 2] * z;
            return d * d * d * d * (var0 + var1 + var2);
        }
    }

    private double optimizedSample(double x, double y) {
        final double var0 = (x + y) * SKEW_FACTOR_2D;
        final double var1 = Math.floor(x + var0);
        final double var2 = Math.floor(y + var0);
        final double var3 = (var1 + var2) * UNSKEW_FACTOR_2D;
        final double var4 = x - var1 + var3;
        final double var5 = y - var2 + var3;
        final int var6;
        final int var7;
        if (var4 > var5) {
            var6 = 1;
            var7 = 0;
        } else {
            var6 = 0;
            var7 = 1;
        }

        final double var8 = var4 - (double) var6 + UNSKEW_FACTOR_2D;
        final double var9 = var5 - (double) var7 + UNSKEW_FACTOR_2D;
        final double var10 = var4 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
        final double var11 = var5 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
        final int var12 = ((int) var1) & 0xFF;
        final int var13 = ((int) var2) & 0xFF;
        final int var14 = (var13 + var7) & 255;
        final int var15 = (var13 + 1) & 255;
        final int var16 = this.permutations[var13 & 255];
        final int var17 = this.permutations[var14];
        final int var18 = this.permutations[var15];
        final int var19 = var12 + var16 & 255;
        final int var20 = var12 + var6 + var17 & 255;
        final int var21 = var12 + 1 + var18 & 255;
        final int var22 = this.permutations[var19] % 12;
        final int var23 = this.permutations[var20] % 12;
        final int ver24 = this.permutations[var21] % 12;
        final double var25 = this.grad(var22, var4, var5, 0.0, 0.5);
        final double var26 = this.grad(var23, var8, var9, 0.0, 0.5);
        final double var27 = this.grad(ver24, var10, var11, 0.0, 0.5);
        return 70.0 * (var25 + var26 + var27);
    }

    @Benchmark
    public double vanillaSampler() {
        return this.vanillaSampler.sample(4096, 4096);
    }

    @Benchmark
    public double optimizedSampler() {
        return this.vanillaSampler.sample(4096, 4096);
    }

}
