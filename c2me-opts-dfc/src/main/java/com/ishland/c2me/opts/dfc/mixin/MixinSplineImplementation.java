package com.ishland.c2me.opts.dfc.mixin;

import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Spline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Spline.Implementation.class)
public abstract class MixinSplineImplementation<C, I extends ToFloatFunction<C>> {

    /**
     * @author
     * @reason
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
     * @author
     * @reason
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


}
