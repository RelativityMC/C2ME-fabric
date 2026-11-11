/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
        return Math.abs(Double.doubleToRawLongBits(original) - Double.doubleToRawLongBits(that));
    }

    private static boolean equals(double original, double that) {
        return (Double.isNaN(original) && Double.isNaN(that)) || original == that;
    }

    protected static long ulpDistance(float original, float that) {
        return Math.abs(Float.floatToRawIntBits(original) - Float.floatToRawIntBits(that));
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
