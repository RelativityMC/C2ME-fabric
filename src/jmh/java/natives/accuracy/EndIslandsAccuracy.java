/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package natives.accuracy;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.ISATarget;
import natives.EndIslandsBenchmark;
import natives.support.ReflectUtils;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.LocalRandom;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Random;

public class EndIslandsAccuracy extends AbstractAccuracy {

    private final Random random = new Random();
    private final SimplexNoiseSampler vanillaSampler;
    private final MemorySegment nativeSampler;
    private final long nativeSamplerPtr;

    protected EndIslandsAccuracy() {
        super(Arrays.stream(ISATarget.getInstance().getEnumConstants()).toArray(ISATarget[]::new), BindingsTemplate.c2me_natives_end_islands_sample_ptr, "c2me_natives_end_islands_sample");
        vanillaSampler = new SimplexNoiseSampler(new LocalRandom(0xcafe));
        int[] permutation = (int[]) ReflectUtils.getField(SimplexNoiseSampler.class, this.vanillaSampler, "permutation");
        nativeSampler = Arena.ofAuto().allocate(permutation.length * 4L, 64);
        MemorySegment.copy(MemorySegment.ofArray(permutation), 0L, nativeSampler, 0L, permutation.length * 4L);
        nativeSamplerPtr = nativeSampler.address();
    }

    private float invokeNative(MethodHandle handle, int x, int z) {
        if ((int) (x * x + z * z) < 0) {
            return Float.NaN;
        }
        try {
            return (float) handle.invokeExact(nativeSamplerPtr, x, z);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private float invokeVanilla(int x, int z) {
        return EndIslandsBenchmark.sampleVanilla(vanillaSampler, x, z);
    }

    private void loopBody() {
        int x = random.nextInt(-30000000, 30000000);
        int z = random.nextInt(-30000000, 30000000);

        float original = invokeVanilla(x, z);
        for (int i = 0; i < this.MHs.length; i ++) {
            float actual = invokeNative(this.MHs[i], x, z);
            long ulpDiff = ulpDistance(original, actual);
            if (ulpDiff > this.maxUlp[i]) {
                this.maxUlp[i] = ulpDiff;
                System.out.println(String.format("%s: new max error %d ulps at x=%d, z=%d (expected %.10g but got %.10g)", this.targets[i], ulpDiff, x, z, original, actual));
            }
        }
    }

    public static void main(String[] args) {
        final long printInterval = 10_000_000_000L;
        EndIslandsAccuracy instance = new EndIslandsAccuracy();
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
