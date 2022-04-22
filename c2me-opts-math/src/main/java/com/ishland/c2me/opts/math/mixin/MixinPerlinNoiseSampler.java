package com.ishland.c2me.opts.math.mixin;

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
    private static final double[] FLAT_SIMPLEX_GRAD = new double[]{
            1, 1, 0, 0,
            -1, 1, 0, 0,
            1, -1, 0, 0,
            -1, -1, 0, 0,
            1, 0, 1, 0,
            -1, 0, 1, 0,
            1, 0, -1, 0,
            -1, 0, -1, 0,
            0, 1, 1, 0,
            0, -1, 1, 0,
            0, 1, -1, 0,
            0, -1, -1, 0,
            1, 1, 0, 0,
            0, -1, 1, 0,
            -1, 1, 0, 0,
            0, -1, -1, 0,
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
        // TODO [VanillaCopy] but optimized
        final int var0 = sectionX & 0xFF;
        final int var1 = (sectionX + 1) & 0xFF;
        final int var2 = this.permutations[var0] & 0xFF;
        final int var3 = this.permutations[var1] & 0xFF;
        final int var4 = (var2 + sectionY) & 0xFF;
        final int var5 = (var3 + sectionY) & 0xFF;
        final int var6 = (var2 + sectionY + 1) & 0xFF;
        final int var7 = (var3 + sectionY + 1) & 0xFF;
        final int var8 = this.permutations[var4] & 0xFF;
        final int var9 = this.permutations[var5] & 0xFF;
        final int var10 = this.permutations[var6] & 0xFF;
        final int var11 = this.permutations[var7] & 0xFF;

        final int var12 = (var8 + sectionZ) & 0xFF;
        final int var13 = (var9 + sectionZ) & 0xFF;
        final int var14 = (var10 + sectionZ) & 0xFF;
        final int var15 = (var11 + sectionZ) & 0xFF;
        final int var16 = (var8 + sectionZ + 1) & 0xFF;
        final int var17 = (var9 + sectionZ + 1) & 0xFF;
        final int var18 = (var10 + sectionZ + 1) & 0xFF;
        final int var19 = (var11 + sectionZ + 1) & 0xFF;
        final int var20 = (this.permutations[var12] & 15) << 2;
        final int var21 = (this.permutations[var13] & 15) << 2;
        final int var22 = (this.permutations[var14] & 15) << 2;
        final int var23 = (this.permutations[var15] & 15) << 2;
        final int var24 = (this.permutations[var16] & 15) << 2;
        final int var25 = (this.permutations[var17] & 15) << 2;
        final int var26 = (this.permutations[var18] & 15) << 2;
        final int var27 = (this.permutations[var19] & 15) << 2;
        final double var60 = localX - 1.0;
        final double var61 = localY - 1.0;
        final double var62 = localZ - 1.0;
        final double var87 = FLAT_SIMPLEX_GRAD[(var20) | 0] * localX + FLAT_SIMPLEX_GRAD[(var20) | 1] * localY + FLAT_SIMPLEX_GRAD[(var20) | 2] * localZ;
        final double var88 = FLAT_SIMPLEX_GRAD[(var21) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var21) | 1] * localY + FLAT_SIMPLEX_GRAD[(var21) | 2] * localZ;
        final double var89 = FLAT_SIMPLEX_GRAD[(var22) | 0] * localX + FLAT_SIMPLEX_GRAD[(var22) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var22) | 2] * localZ;
        final double var90 = FLAT_SIMPLEX_GRAD[(var23) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var23) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var23) | 2] * localZ;
        final double var91 = FLAT_SIMPLEX_GRAD[(var24) | 0] * localX + FLAT_SIMPLEX_GRAD[(var24) | 1] * localY + FLAT_SIMPLEX_GRAD[(var24) | 2] * var62;
        final double var92 = FLAT_SIMPLEX_GRAD[(var25) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var25) | 1] * localY + FLAT_SIMPLEX_GRAD[(var25) | 2] * var62;
        final double var93 = FLAT_SIMPLEX_GRAD[(var26) | 0] * localX + FLAT_SIMPLEX_GRAD[(var26) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var26) | 2] * var62;
        final double var94 = FLAT_SIMPLEX_GRAD[(var27) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var27) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var27) | 2] * var62;

        final double var95 = localX * 6.0 - 15.0;
        final double var96 = fadeLocalX * 6.0 - 15.0;
        final double var97 = localZ * 6.0 - 15.0;
        final double var98 = localX * var95 + 10.0;
        final double var99 = fadeLocalX * var96 + 10.0;
        final double var100 = localZ * var97 + 10.0;
        final double var101 = localX * localX * localX * var98;
        final double var102 = fadeLocalX * fadeLocalX * fadeLocalX * var99;
        final double var103 = localZ * localZ * localZ * var100;

        final double var113 = var87 + var101 * (var88 - var87);
        final double var114 = var93 + var101 * (var94 - var93);
        final double var115 = var91 + var101 * (var92 - var91);
        final double var116 = var89 + var101 * (var90 - var89);
        final double var117 = var114 - var115;
        final double var118 = var102 * (var116 - var113);
        final double var119 = var102 * var117;
        final double var120 = var113 + var118;
        final double var121 = var115 + var119;
        return var120 + (var103 * (var121 - var120));
    }

}
