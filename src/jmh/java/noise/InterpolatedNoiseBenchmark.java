package noise;

import com.ishland.c2me.natives.ModuleEntryPoint;
import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.NativesUtils;
import net.minecraft.world.gen.densityfunction.DensityFunction;
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

    private final InterpolatedNoiseSamplerCopy sampler = InterpolatedNoiseSamplerCopy.field_37205;

    private final long pointer = NativesUtils.createInterpolatedSamplerPointer(sampler, InterpolatedNoiseSamplerCopy.class);

    @Param({"100"})
    private int x;
    @Param({"100"})
    private int y;
    @Param({"100"})
    private int z;

    @Benchmark
    public double nativeSampler() {
        return NativesInterface.perlinSampleInterpolated(pointer, x, y, z);
    }

    @Benchmark
    public double vanillaSampler() {
        return sampler.sample(new NoisePos(x, y, z));
    }

    private record NoisePos(int blockX, int blockY, int blockZ) implements DensityFunction.NoisePos {
    }

}
