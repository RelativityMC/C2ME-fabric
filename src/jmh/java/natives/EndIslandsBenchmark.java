package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import natives.support.ReflectUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.LocalRandom;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(EndIslandsBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class EndIslandsBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    private static float sampleVanilla(SimplexNoiseSampler sampler, int x, int z) {
        int i = x / 2;
        int j = z / 2;
        int k = x % 2;
        int l = z % 2;
        float f = 100.0F - MathHelper.sqrt((float)(x * x + z * z)) * 8.0F;
        f = MathHelper.clamp(f, -100.0F, 80.0F);

        for (int m = -12; m <= 12; m++) {
            for (int n = -12; n <= 12; n++) {
                long o = i + m;
                long p = j + n;
                if (o * o + p * p > 4096L && sampler.sample((double)o, (double)p) < -0.9F) {
                    float g = (MathHelper.abs((float)o) * 3439.0F + MathHelper.abs((float)p) * 147.0F) % 13.0F + 9.0F;
                    float h = (float)(k - m * 2);
                    float q = (float)(l - n * 2);
                    float r = 100.0F - MathHelper.sqrt(h * h + q * q) * g;
                    r = MathHelper.clamp(r, -100.0F, 80.0F);
                    f = Math.max(f, r);
                }
            }
        }

        return f;
    }

    private final int[] sampleX = new int[invocations];
    private final int[] sampleZ = new int[invocations];
    private SimplexNoiseSampler vanillaSampler;
    private MemorySegment nativeSamplerData;
    private long nativeSamplerDataPtr;

    public EndIslandsBenchmark() {
        super(BindingsTemplate.c2me_natives_end_islands_sample_ptr, "c2me_natives_end_islands_sample");
    }

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextInt(-30000000, 30000000);
            sampleZ[i] = random.nextInt(-30000000, 30000000);
        }
        LocalRandom random1 = new LocalRandom(random.nextLong());
        this.vanillaSampler = new SimplexNoiseSampler(random1);
        int[] permutation = (int[]) ReflectUtils.getField(SimplexNoiseSampler.class, this.vanillaSampler, "permutation");
        this.nativeSamplerData = Arena.ofAuto().allocate(permutation.length * 4L, 64);
        MemorySegment.copy(MemorySegment.ofArray(permutation), 0L, this.nativeSamplerData, 0L, permutation.length * 4L);
        this.nativeSamplerDataPtr = this.nativeSamplerData.address();
        VarHandle.fullFence();
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            try {
                bh.consume((float) handle.invokeExact(this.nativeSamplerDataPtr, sampleX[i], sampleZ[i]));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    @Override
    public void spinning(Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            bh.consume(sampleX[i] + sampleZ[i]);
        }
    }

    @Benchmark
    @Override
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i++) {
            bh.consume(sampleVanilla(this.vanillaSampler, sampleX[i], sampleZ[i]));
        }
    }
}
