package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(DoublePerlinNoiseBatchBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DoublePerlinNoiseBatchBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];
    private DoublePerlinNoiseSampler vanillaSampler;
    private MemorySegment nativeSamplerData;
    private long nativeSamplerDataPtr;

    @Param({"1", "16"})
    private int scale;

    @Param({"2", "4", "8", "16", "32", "64"})
    private int batchSize;

    private double[] returnBuffer;
//    private double[] xBuf;
//    private double[] yBuf;
//    private double[] zBuf;

    public DoublePerlinNoiseBatchBenchmark() {
        super(BindingsTemplate.c2me_natives_noise_perlin_double_batch_ptr, "c2me_natives_noise_perlin_double_batch");
    }

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            sampleY[i] = random.nextDouble(-2048.0D, 2048.0D);
            sampleZ[i] = random.nextDouble(-30000000.0D, 30000000.0D);
        }
        final net.minecraft.util.math.random.Random minecraftRandom = net.minecraft.util.math.random.Random.create(random.nextInt());
        double[] octaves = new double[scale];
        for (int i = 0, octavesLength = octaves.length; i < octavesLength; i++) {
            octaves[i] = minecraftRandom.nextDouble() * 32.0D + 0.01D;
        }
        vanillaSampler = DoublePerlinNoiseSampler.create(minecraftRandom, minecraftRandom.nextInt(128), octaves);
        nativeSamplerData = DoublePerlinNoiseBenchmark.create(vanillaSampler);
        nativeSamplerDataPtr = nativeSamplerData.address();
        returnBuffer = new double[batchSize];
//        xBuf = new double[batchSize];
//        yBuf = new double[batchSize];
//        zBuf = new double[batchSize];
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; i += batchSize) {
            double[] xBuf = new double[batchSize];
            double[] yBuf = new double[batchSize];
            double[] zBuf = new double[batchSize];
            for (int j = 0; j < batchSize; j++) {
                xBuf[j] = sampleX[i + j];
                yBuf[j] = sampleY[i + j];
                zBuf[j] = sampleZ[i + j];
            }
            try {
                handle.invokeExact(
                        nativeSamplerDataPtr,
                        MemorySegment.ofArray(returnBuffer),
                        MemorySegment.ofArray(xBuf),
                        MemorySegment.ofArray(yBuf),
                        MemorySegment.ofArray(zBuf),
                        batchSize
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            for (int j = 0; j < batchSize; j ++) {
                bh.consume(returnBuffer[j]);
            }
        }
    }

    @Benchmark
    @Override
    public void spinning(Blackhole bh) {
        for (int i = 0; i < invocations; i += batchSize) {
            double[] xBuf = new double[batchSize];
            double[] yBuf = new double[batchSize];
            double[] zBuf = new double[batchSize];
            for (int j = 0; j < batchSize; j++) {
                xBuf[j] = sampleX[i + j];
                yBuf[j] = sampleY[i + j];
                zBuf[j] = sampleZ[i + j];
            }
            bh.consume(xBuf);
            bh.consume(yBuf);
            bh.consume(zBuf);
            for (int j = 0; j < batchSize; j ++) {
                bh.consume(returnBuffer[j]);
            }
        }
    }

    @Override
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            bh.consume(vanillaSampler.sample(sampleX[i], sampleY[i], sampleZ[i]));
        }
    }
}
