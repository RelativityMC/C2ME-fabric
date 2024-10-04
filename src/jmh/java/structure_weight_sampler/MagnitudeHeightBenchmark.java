package structure_weight_sampler;

import net.minecraft.util.math.MathHelper;
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

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(MagnitudeHeightBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MagnitudeHeightBenchmark {

    protected static final int invocations = 1 << 16;

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(0xcafe);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            sampleY[i] = random.nextDouble(-2048.0D, 2048.0D);
            sampleZ[i] = random.nextDouble(-30000000.0D, 30000000.0D);
        }
    }

    private static double vanilla(double x, double y, double z) {
        double d = MathHelper.magnitude(x, y, z);
        return MathHelper.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
    }

    @Benchmark
    public void benchVanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            bh.consume(vanilla(sampleX[i], sampleY[i], sampleZ[i]));
        }
    }

    private static double opt1(double x, double y, double z) {
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d > 6.0) {
            return 0.0;
        } else {
            return 1.0 - d / 6.0;
        }
    }

    @Benchmark
    public void benchOpt1(Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            bh.consume(opt1(sampleX[i], sampleY[i], sampleZ[i]));
        }
    }

}
