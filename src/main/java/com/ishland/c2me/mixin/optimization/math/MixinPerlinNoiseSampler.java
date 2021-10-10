package com.ishland.c2me.mixin.optimization.math;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PerlinNoiseSampler.class)
public abstract class MixinPerlinNoiseSampler {

//    @Shadow protected abstract double sample(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX);

    @Shadow @Final public double originY;

    @Shadow @Final public double originX;

    @Shadow @Final public double originZ;

//    /**
//     * @author ishland
//     * @reason optimize
//     */
//    @Deprecated
//    @Overwrite
//    public double sample(double x, double y, double z, double yScale, double yMax) {
//        double d = x + this.originX;
//        double e = y + this.originY;
//        double f = z + this.originZ;
//        double i = Math.floor(d);
//        double j = Math.floor(e);
//        double k = Math.floor(f);
//        double g = d - i;
//        double h = e - j;
//        double l = f - k;
//        double o = 0.0D;
//        if (yScale != 0.0) {
//            double m;
//            if (yMax >= 0.0 && yMax < h) {
//                m = yMax;
//            } else {
//                m = h;
//            }
//
//            o = Math.floor(m / yScale + 1.0E-7F) * yScale;
//        }
//
//        return this.sample((int) i, (int) j, (int) k, g, h - o, l, h);
//    }

}
