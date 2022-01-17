package noise;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.gen.random.SimpleRandom;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerlinNoiseBenchmark {

    private final PerlinNoiseSampler vanillaSampler = new PerlinNoiseSampler(new SimpleRandom(0xFF));

    private final byte[] permutations;
    private static final double[][] SIMPLEX_NOISE_GRADIENTS = new double[][]{
            {1, 1, 0},
            {-1, 1, 0},
            {1, -1, 0},
            {-1, -1, 0},
            {1, 0, 1},
            {-1, 0, 1},
            {1, 0, -1},
            {-1, 0, -1},
            {0, 1, 1},
            {0, -1, 1},
            {0, 1, -1},
            {0, -1, -1},
            {1, 1, 0},
            {0, -1, 1},
            {-1, 1, 0},
            {0, -1, -1}
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
        int i = this.permutations[sectionX & 0xFF];
        int j = this.permutations[sectionX + 1 & 0xFF];
        int k = this.permutations[i + sectionY & 0xFF];
        int l = this.permutations[i + sectionY + 1 & 0xFF];
        int m = this.permutations[j + sectionY & 0xFF];
        int n = this.permutations[j + sectionY + 1 & 0xFF];

        final double[] grad0 = SIMPLEX_NOISE_GRADIENTS[this.permutations[k + sectionZ & 0xFF] & 15];
        final double[] grad1 = SIMPLEX_NOISE_GRADIENTS[this.permutations[m + sectionZ & 0xFF] & 15];
        final double[] grad2 = SIMPLEX_NOISE_GRADIENTS[this.permutations[l + sectionZ & 0xFF] & 15];
        final double[] grad3 = SIMPLEX_NOISE_GRADIENTS[this.permutations[n + sectionZ & 0xFF] & 15];
        final double[] grad4 = SIMPLEX_NOISE_GRADIENTS[this.permutations[k + sectionZ + 1 & 0xFF] & 15];
        final double[] grad5 = SIMPLEX_NOISE_GRADIENTS[this.permutations[m + sectionZ + 1 & 0xFF] & 15];
        final double[] grad6 = SIMPLEX_NOISE_GRADIENTS[this.permutations[l + sectionZ + 1 & 0xFF] & 15];
        final double[] grad7 = SIMPLEX_NOISE_GRADIENTS[this.permutations[n + sectionZ + 1 & 0xFF] & 15];
        double d = (grad0[0] * localX) + (grad0[1] * localY) + (grad0[2] * localZ);
        double e = (grad1[0] * (localX - 1.0)) + (grad1[1] * localY) + (grad1[2] * localZ);
        double f = (grad2[0] * localX) + (grad2[1] * (localY - 1.0)) + (grad2[2] * localZ);
        double g = (grad3[0] * (localX - 1.0)) + (grad3[1] * (localY - 1.0)) + (grad3[2] * localZ);
        double h = (grad4[0] * localX) + (grad4[1] * localY) + (grad4[2] * (localZ - 1.0));
        double o = (grad5[0] * (localX - 1.0)) + (grad5[1] * localY) + (grad5[2] * (localZ - 1.0));
        double p = (grad6[0] * localX) + (grad6[1] * (localY - 1.0)) + (grad6[2] * (localZ - 1.0));
        double q = (grad7[0] * (localX - 1.0)) + (grad7[1] * (localY - 1.0)) + (grad7[2] * (localZ - 1.0));

        double r = localX * localX * localX * (localX * (localX * 6.0 - 15.0) + 10.0);
        double s = fadeLocalX * fadeLocalX * fadeLocalX * (fadeLocalX * (fadeLocalX * 6.0 - 15.0) + 10.0);
        double t = localZ * localZ * localZ * (localZ * (localZ * 6.0 - 15.0) + 10.0);

        double v0 = d + r * (e - d) + s * (f + r * (g - f) - (d + r * (e - d)));
        double v1 = h + r * (o - h) + s * (p + r * (q - p) - (h + r * (o - h)));
        return v0 + (t * (v1 - v0));
    }

    {
        try {
            final Field permutationsField = PerlinNoiseSampler.class.getDeclaredField("permutations");
            permutationsField.setAccessible(true);
            permutations = (byte[]) permutationsField.get(vanillaSampler);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Param({"0.0", "0.5", "1.0", "8.0"})
    private double yScale;
    @Param({"-1.0", "0.0", "1.0", "2.0", "16.0"})
    private double yMax;

    @SuppressWarnings("deprecation")
    @Benchmark
    public double vanillaSampler() {
        return vanillaSampler.sample(4096, 128, 4096, yScale, yMax);
    }

    @Benchmark
    public double optimizedSampler() {
        return optimizedSample(4096, 128, 4096, yScale, yMax);
    }

}
