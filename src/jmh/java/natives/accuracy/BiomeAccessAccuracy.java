package natives.accuracy;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.ISATarget;
import natives.DoublePerlinNoiseBenchmark;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.source.SeedMixer;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Random;

public class BiomeAccessAccuracy extends AbstractAccuracy {

    private static final long seed = 0xcafebabeL;
    private final Random random = new Random();

    public static int sampleVanilla(long theSeed, int x, int y , int z) {
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

        return o;
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

    protected BiomeAccessAccuracy() {
        super(Arrays.stream(ISATarget.getInstance().getEnumConstants()).toArray(ISATarget[]::new), BindingsTemplate.c2me_natives_biome_access_sample, "c2me_natives_biome_access_sample");
    }

    private int invokeNative(MethodHandle handle, int x, int y, int z) {
        try {
            return (int) handle.invokeExact(seed, x, y, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private int invokeVanilla(int x, int y, int z) {
        return sampleVanilla(seed, x, y, z);
    }

    private void loopBody() {
        int x = this.random.nextInt(-30000000, 30000000);
        int y = this.random.nextInt(-2048, 2048);
        int z = this.random.nextInt(-30000000, 30000000);

        int original = invokeVanilla(x, y, z);
        for (int i = 0; i < this.MHs.length; i ++) {
            int actual = invokeNative(this.MHs[i], x, y, z);
            if (original != actual) {
                this.maxUlp[i] = 1;
                System.out.println(String.format("%s: new error at x=%d, y=%d, z=%d (expected %d but got %d)", this.targets[i], x, y, z, original, actual));
            }
        }
    }

    public static void main(String[] args) {
        final long printInterval = 10_000_000_000L;
        BiomeAccessAccuracy instance = new BiomeAccessAccuracy();
        long lastPrint = System.nanoTime();
        for (long iter = 0; ; iter ++) {
            instance.loopBody();
            if ((iter & (1L << 16L - 1L)) == 0) {
                long nanoTime = System.nanoTime();
                if (nanoTime > (lastPrint + printInterval)) {
                    lastPrint += printInterval;
                    System.out.println("=".repeat(30));
                    System.out.println(String.format("Iterations: %d", iter));
                    instance.printUlps();
                    System.out.println("=".repeat(30));
                }
            }
        }
    }

}
