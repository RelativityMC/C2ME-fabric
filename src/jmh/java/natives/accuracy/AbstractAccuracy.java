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

import com.ishland.c2me.opts.natives_math.common.ISATarget;
import com.ishland.c2me.opts.natives_math.common.NativeLoader;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

public abstract class AbstractAccuracy {

    protected final ISATarget[] targets;
    protected final MethodHandle[] MHs;
    protected final long[] maxUlp;

    protected AbstractAccuracy(ISATarget[] targets, MethodHandle template, String prefix) {
        this.targets = Arrays.stream(targets).filter(ISATarget::isNativelySupported).toArray(ISATarget[]::new);
        this.MHs = Arrays.stream(this.targets)
                .map(isaTarget -> template.bindTo(NativeLoader.lookup.find(prefix + isaTarget.getSuffix()).get()))
                .toArray(MethodHandle[]::new);
        this.maxUlp = new long[this.targets.length];
    }

    protected static long ulpDistance(double original, double that) {
        long dist = 0;
        if (original > that) {
            double tmp = that;
            that = original;
            original = tmp;
        }
        while (that > original) {
            that = Math.nextAfter(that, original);
            dist ++;
        }
        if (dist == 0 && !equals(original, that)) {
            return Long.MAX_VALUE;
        }
        return dist;
    }

    private static boolean equals(double original, double that) {
        return (Double.isNaN(original) && Double.isNaN(that)) || original == that;
    }

    protected static long ulpDistance(float original, float that) {
        long dist = 0;
        if (original > that) {
            float tmp = that;
            that = original;
            original = tmp;
        }
        while (that > original) {
            that = Math.nextAfter(that, original);
            dist ++;
        }
        if (dist == 0 && !equals(original, that)) {
            return Long.MAX_VALUE;
        }
        return dist;
    }

    private static boolean equals(float original, float that) {
        return (Float.isNaN(original) && Float.isNaN(that)) || original == that;
    }

    protected void printUlps() {
        for (int i = 0; i < this.maxUlp.length; i++) {
            System.out.println(String.format("%s: max error %d ulps", this.targets[i], this.maxUlp[i]));
        }
    }

}
