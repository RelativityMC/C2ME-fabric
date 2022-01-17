package com.ishland.c2me.mixin.optimization.math;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PerlinNoiseSampler.class, priority = 1090)
public abstract class MixinPerlinNoiseSampler {

    @Shadow @Final public double originY;

    @Shadow @Final public double originX;

    @Shadow @Final public double originZ;

    @Shadow @Final private byte[] permutations;

    @Unique
    private static final double[][] SIMPLEX_NOISE_GRADIENTS = new double[][]{
            {1, 1, 0},
            {-1, 1, 0},
            {1, -1, 0},
            {-1, -1, 0},
            {1, 0, 1},
            {-1, 0, 1},
            {1, 0, -1},
            {-1, 0, -1},
            {0, 1, 1},
            {0, -1, 1},
            {0, 1, -1},
            {0, -1, -1},
            {1, 1, 0},
            {0, -1, 1},
            {-1, 1, 0},
            {0, -1, -1}
    };

    /**
     * @author ishland
     * @reason optimize: remove frequent type conversions
     */
    @Deprecated
    @Overwrite
    public double sample(double x, double y, double z, double yScale, double yMax) {
        double d = x + this.originX;
        double e = y + this.originY;
        double f = z + this.originZ;
        double i = Math.floor(d);
        double j = Math.floor(e);
        double k = Math.floor(f);
        double g = d - i;
        double h = e - j;
        double l = f - k;
        double o = 0.0D;
        if (yScale != 0.0) {
            double m;
            if (yMax >= 0.0 && yMax < h) {
                m = yMax;
            } else {
                m = h;
            }

            o = Math.floor(m / yScale + 1.0E-7F) * yScale;
        }

        return this.sample((int) i, (int) j, (int) k, g, h - o, l, h);
    }

    /**
     * @author ishland
     * @reason inline math & small optimization: remove frequent type conversions and redundant ops
     */
    @Overwrite
    private double sample(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX) {
        // TODO [VanillaCopy] but inlined
        int i = this.permutations[sectionX & 0xFF];
        int j = this.permutations[sectionX + 1 & 0xFF];
        int k = this.permutations[i + sectionY & 0xFF];
        int l = this.permutations[i + sectionY + 1 & 0xFF];
        int m = this.permutations[j + sectionY & 0xFF];
        int n = this.permutations[j + sectionY + 1 & 0xFF];

        final double[] grad0 = SIMPLEX_NOISE_GRADIENTS[this.permutations[k + sectionZ & 0xFF] & 15];
        final double[] grad1 = SIMPLEX_NOISE_GRADIENTS[this.permutations[m + sectionZ & 0xFF] & 15];
        final double[] grad2 = SIMPLEX_NOISE_GRADIENTS[this.permutations[l + sectionZ & 0xFF] & 15];
        final double[] grad3 = SIMPLEX_NOISE_GRADIENTS[this.permutations[n + sectionZ & 0xFF] & 15];
        final double[] grad4 = SIMPLEX_NOISE_GRADIENTS[this.permutations[k + sectionZ + 1 & 0xFF] & 15];
        final double[] grad5 = SIMPLEX_NOISE_GRADIENTS[this.permutations[m + sectionZ + 1 & 0xFF] & 15];
        final double[] grad6 = SIMPLEX_NOISE_GRADIENTS[this.permutations[l + sectionZ + 1 & 0xFF] & 15];
        final double[] grad7 = SIMPLEX_NOISE_GRADIENTS[this.permutations[n + sectionZ + 1 & 0xFF] & 15];
        double d = (grad0[0] * localX) + (grad0[1] * localY) + (grad0[2] * localZ);
        double e = (grad1[0] * (localX - 1.0)) + (grad1[1] * localY) + (grad1[2] * localZ);
        double f = (grad2[0] * localX) + (grad2[1] * (localY - 1.0)) + (grad2[2] * localZ);
        double g = (grad3[0] * (localX - 1.0)) + (grad3[1] * (localY - 1.0)) + (grad3[2] * localZ);
        double h = (grad4[0] * localX) + (grad4[1] * localY) + (grad4[2] * (localZ - 1.0));
        double o = (grad5[0] * (localX - 1.0)) + (grad5[1] * localY) + (grad5[2] * (localZ - 1.0));
        double p = (grad6[0] * localX) + (grad6[1] * (localY - 1.0)) + (grad6[2] * (localZ - 1.0));
        double q = (grad7[0] * (localX - 1.0)) + (grad7[1] * (localY - 1.0)) + (grad7[2] * (localZ - 1.0));

        double r = localX * localX * localX * (localX * (localX * 6.0 - 15.0) + 10.0);
        double s = fadeLocalX * fadeLocalX * fadeLocalX * (fadeLocalX * (fadeLocalX * 6.0 - 15.0) + 10.0);
        double t = localZ * localZ * localZ * (localZ * (localZ * 6.0 - 15.0) + 10.0);

        double v0 = d + r * (e - d) + s * (f + r * (g - f) - (d + r * (e - d)));
        double v1 = h + r * (o - h) + s * (p + r * (q - p) - (h + r * (o - h)));
        return v0 + (t * (v1 - v0));
    }

}
