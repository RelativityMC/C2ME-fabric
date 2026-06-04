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

package com.ishland.c2me.opts.dfc.mixin;

import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Spline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mixin(Spline.Implementation.class)
public abstract class MixinSplineImplementation<C, I extends ToFloatFunction<C>> {

    /**
     * @author ishland
     * @reason inline binary search
     */
    @Overwrite
    private static int findRangeForLocation(float[] locations, float x) {
        int min = 0;
        int i = locations.length;

        while (i > 0) {
            int j = i / 2;
            int k = min + j;
            if (x < locations[k]) {
                i = j;
            } else {
                min = k + 1;
                i -= j + 1;
            }
        }

        return min - 1;
    }

    @Shadow @Final private I locationFunction;

    @Shadow @Final private float[] locations;

    @Shadow
    protected static float sampleOutsideRange(float point, float[] locations, float value, float[] derivatives, int i) {
        throw new AbstractMethodError();
    }

    @Shadow @Final private List<Spline<C, I>> values;

    @Shadow @Final private float[] derivatives;

    /**
     * @author ishland
     * @reason simplify method a bit
     */
    @Overwrite
    public float apply(C x) {
        float point = this.locationFunction.apply(x);
        int rangeForLocation = findRangeForLocation(this.locations, point);
        int last = this.locations.length - 1;
        if (rangeForLocation < 0) {
            return sampleOutsideRange(point, this.locations, this.values.get(0).apply(x), this.derivatives, 0);
        } else if (rangeForLocation == last) {
            return sampleOutsideRange(point, this.locations, this.values.get(last).apply(x), this.derivatives, last);
        } else {
            float loc0 = this.locations[rangeForLocation];
            float loc1 = this.locations[rangeForLocation + 1];
            float locDist = loc1 - loc0;
            float k = (point - loc0) / locDist;
            float n = this.values.get(rangeForLocation).apply(x);
            float o = this.values.get(rangeForLocation + 1).apply(x);
            float onDist = o - n;
            float p = this.derivatives[rangeForLocation] * locDist - onDist;
            float q = -this.derivatives[rangeForLocation + 1] * locDist + onDist;
            return MathHelper.lerp(k, n, o) + k * (1.0F - k) * MathHelper.lerp(k, p, q);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spline.Implementation<?, ?> that = (Spline.Implementation<?, ?>) o;
        return Objects.equals(locationFunction, that.locationFunction()) && Arrays.equals(locations, that.locations()) && Objects.equals(values, that.values()) && Arrays.equals(derivatives, that.derivatives());
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + Objects.hashCode(locationFunction);
        result = 31 * result + Arrays.hashCode(locations);
        result = 31 * result + Objects.hashCode(values);
        result = 31 * result + Arrays.hashCode(derivatives);

        return result;
    }
}
