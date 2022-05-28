package noise;

import com.ishland.c2me.natives.ModuleEntryPoint;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativesUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InterpolatedNoiseBenchmark {

    static {
        ModuleEntryPoint.init();
    }

    private final InterpolatedNoiseSamplerCopy sampler = InterpolatedNoiseSamplerCopy.createBase3dNoiseFunction(1.0, 1.0, 80.0, 160.0, 2.0);

    private final long pointer = NativesUtils.createInterpolatedSamplerPointer(sampler, InterpolatedNoiseSamplerCopy.class);

    @Param({"100"})
    private int x;
    @Param({"100"})
    private int y;
    @Param({"100"})
    private int z;

    @Benchmark
    public double nativeSampler() {
        return NativeInterface.perlinSampleInterpolated(pointer, x, y, z);
    }

    @Benchmark
    public double vanillaSampler() {
        return sampler.sample(new InterpolatedNoiseSamplerCopy.NoisePos(x, y, z));
    }

}
