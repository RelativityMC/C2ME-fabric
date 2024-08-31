package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.SeedMixer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(BiomeAccessBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BiomeAccessBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    public static void sampleVanilla(long theSeed, int x, int y , int z, Blackhole bh) {
        int i = x - 2;
        int j = y - 2;
        int k = z - 2;
        int l = i >> 2;
        int m = j >> 2;
        int n = k >> 2;
        double d = (double)(i & 3) / 4.0;
        double e = (double)(j & 3) / 4.0;
        double f = (double)(k & 3) / 4.0;
        int o = 0;
        double g = Double.POSITIVE_INFINITY;

        for(int p = 0; p < 8; ++p) {
            boolean bl = (p & 4) == 0;
            boolean bl2 = (p & 2) == 0;
            boolean bl3 = (p & 1) == 0;
            int q = bl ? l : l + 1;
            int r = bl2 ? m : m + 1;
            int s = bl3 ? n : n + 1;
            double h = bl ? d : d - 1.0;
            double t = bl2 ? e : e - 1.0;
            double u = bl3 ? f : f - 1.0;
            double v = method_38106(theSeed, q, r, s, h, t, u);
            if (g > v) {
                o = p;
                g = v;
            }
        }

        bh.consume((o & 4) == 0 ? l : l + 1);
        bh.consume((o & 2) == 0 ? m : m + 1);
        bh.consume((o & 1) == 0 ? n : n + 1);
    }

    private static double method_38108(long l) {
        double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0;
        return (d - 0.5) * 0.9;
    }

    private static double method_38106(long l, int i, int j, int k, double d, double e, double f) {
        long m = SeedMixer.mixSeed(l, (long)i);
        m = SeedMixer.mixSeed(m, (long)j);
        m = SeedMixer.mixSeed(m, (long)k);
        m = SeedMixer.mixSeed(m, (long)i);
        m = SeedMixer.mixSeed(m, (long)j);
        m = SeedMixer.mixSeed(m, (long)k);
        double g = method_38108(m);
        m = SeedMixer.mixSeed(m, l);
        double h = method_38108(m);
        m = SeedMixer.mixSeed(m, l);
        double n = method_38108(m);
        return MathHelper.square(f + n) + MathHelper.square(e + h) + MathHelper.square(d + g);
    }

    private final int[] sampleX = new int[invocations];
    private final int[] sampleY = new int[invocations];
    private final int[] sampleZ = new int[invocations];
    private long sampleSeed;

    public BiomeAccessBenchmark() {
        super(BindingsTemplate.c2me_natives_biome_access_sample, "c2me_natives_biome_access_sample");
    }

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextInt(-30000000, 30000000);
            sampleY[i] = random.nextInt(-2048, 2048);
            sampleZ[i] = random.nextInt(-30000000, 30000000);
        }
        sampleSeed = random.nextLong();
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; ++i) {
            try {
                int mask = (int) handle.invokeExact(sampleSeed, sampleX[i], sampleY[i], sampleZ[i]);
                bh.consume(((sampleX[i] - 2) >> 2) + ((mask & 4) != 0 ? 1 : 0));
                bh.consume(((sampleY[i] - 2) >> 2) + ((mask & 2) != 0 ? 1 : 0));
                bh.consume(((sampleY[i] - 2) >> 2) + ((mask & 1) != 0 ? 1 : 0));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    @Override
    public void spinning(Blackhole bh) {
        for (int i = 0; i < invocations; ++i) {
            bh.consume(sampleX[i]);
            bh.consume(sampleY[i]);
            bh.consume(sampleZ[i]);
        }
    }

    @Benchmark
    @Override
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; ++i) {
            sampleVanilla(sampleSeed, sampleX[i], sampleY[i], sampleZ[i], bh);
        }
    }
}
