package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import natives.support.InterpolatedNoiseSamplerCopy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(InterpolatedNoiseBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InterpolatedNoiseBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];
    private InterpolatedNoiseSamplerCopy vanillaSampler;
    private MemorySegment nativeSamplerData;
    private long nativeSamplerDataPtr;

    public InterpolatedNoiseBenchmark() {
        super(BindingsTemplate.c2me_natives_noise_interpolated_ptr, "c2me_natives_noise_interpolated");
    }

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            sampleY[i] = random.nextDouble(-2048.0D, 2048.0D);
            sampleZ[i] = random.nextDouble(-30000000.0D, 30000000.0D);
        }
        vanillaSampler = InterpolatedNoiseSamplerCopy.createBase3dNoiseFunction(0.25, 0.125, 80.0, 160.0, 8.0);
        nativeSamplerData = InterpolatedNoiseSamplerCopy.interpolated_noise_sampler$create(vanillaSampler);
        nativeSamplerDataPtr = nativeSamplerData.address();
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            try {
                bh.consume((double) handle.invokeExact(nativeSamplerDataPtr, sampleX[i], sampleY[i], sampleZ[i]));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    @Override
    public void spinning(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(sampleX[i] + sampleY[i] + sampleZ[i]);
        }
    }

    @Benchmark
    @Override
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(vanillaSampler.sample(sampleX[i], sampleY[i], sampleZ[i]));
        }
    }
}
