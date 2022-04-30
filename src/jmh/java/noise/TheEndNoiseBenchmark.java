package noise;

import com.ishland.c2me.natives.ModuleEntryPoint;
import com.ishland.c2me.natives.common.Cleaners;
import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.UnsafeUtil;
import io.netty.util.internal.PlatformDependent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TheEndNoiseBenchmark extends AbstractSimplexNoise {

    private final long permutationsPointer;

    {
        ModuleEntryPoint.init();
        this.permutationsPointer = UnsafeUtil.getInstance().allocateMemory(4 * 256);
        byte[] tmp = new byte[4 * 256];
        UnsafeUtil.getInstance().copyMemory(
                permutations,
                UnsafeUtil.getInstance().arrayBaseOffset(int[].class),
                tmp,
                UnsafeUtil.getInstance().arrayBaseOffset(byte[].class),
                4 * 256
        );
        PlatformDependent.copyMemory(tmp, 0, this.permutationsPointer, 4 * 256);
        Cleaners.register(this, this.permutationsPointer);
    }

    public static float getNoiseAt(SimplexNoiseSampler simplexNoiseSampler, int i, int j) {
        int k = i / 2;
        int l = j / 2;
        int m = i % 2;
        int n = j % 2;
        float f = 100.0F - MathHelper.sqrt((float)(i * i + j * j)) * 8.0F;
        f = MathHelper.clamp(f, -100.0F, 80.0F);

        for(int o = -12; o <= 12; ++o) {
            for(int p = -12; p <= 12; ++p) {
                long q = (long)(k + o);
                long r = (long)(l + p);
                if (q * q + r * r > 4096L && simplexNoiseSampler.sample((double)q, (double)r) < -0.9F) {
                    float g = (MathHelper.abs((float)q) * 3439.0F + MathHelper.abs((float)r) * 147.0F) % 13.0F + 9.0F;
                    float h = (float)(m - o * 2);
                    float s = (float)(n - p * 2);
                    float t = 100.0F - MathHelper.sqrt(h * h + s * s) * g;
                    t = MathHelper.clamp(t, -100.0F, 80.0F);
                    f = Math.max(f, t);
                }
            }
        }

        return f;
    }

    @Benchmark
    public float vanillaSampler1() {
        return getNoiseAt(this.vanillaSampler, 20000, 20000);
    }

    @Benchmark
    public float nativeSampler() {
        return NativesInterface.theEndSample(this.permutationsPointer, 20000, 20000);
    }

}
