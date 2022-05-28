package com.ishland.c2me.opts.math.mixin;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SimplexNoiseSampler.class)
public abstract class MixinSimplexNoiseSampler {

    @Shadow
    @Final
    private static double SKEW_FACTOR_2D;

    @Shadow
    @Final
    private static double UNSKEW_FACTOR_2D;

    @Shadow
    @Final
    private int[] permutation;

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

    private double grad(int hash, double x, double y, double z, double distance) {
        double d = distance - x * x - y * y - z * z;
        if (d < 0.0) {
            return 0.0;
        } else {
            final int i = hash << 2;
            final double var0 = FLAT_SIMPLEX_GRAD[i | 0] * x;
            final double var1 = FLAT_SIMPLEX_GRAD[i | 1] * y;
            final double var2 = FLAT_SIMPLEX_GRAD[i | 2] * z;
            return d * d * d * d * (var0 + var1 + var2);
        }
    }

    /**
     * @author ishland
     * @reason inline math & small optimization
     */
    @Overwrite
    public double sample(double x, double y) {
        final double var0 = (x + y) * SKEW_FACTOR_2D;
        final double var1 = Math.floor(x + var0);
        final double var2 = Math.floor(y + var0);
        final double var3 = (var1 + var2) * UNSKEW_FACTOR_2D;
        final double var4 = x - var1 + var3;
        final double var5 = y - var2 + var3;
        final int var6;
        final int var7;
        if (var4 > var5) {
            var6 = 1;
            var7 = 0;
        } else {
            var6 = 0;
            var7 = 1;
        }

        final double var8 = var4 - (double) var6 + UNSKEW_FACTOR_2D;
        final double var9 = var5 - (double) var7 + UNSKEW_FACTOR_2D;
        final double var10 = var4 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
        final double var11 = var5 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
        final int var12 = ((int) var1) & 0xFF;
        final int var13 = ((int) var2) & 0xFF;
        final int var16 = this.permutation[var13 & 255];
        final int var17 = this.permutation[(var13 + var7) & 255];
        final int var18 = this.permutation[(var13 + 1) & 255];
        final int var22 = this.permutation[var12 + var16 & 255] % 12;
        final int var23 = this.permutation[var12 + var6 + var17 & 255] % 12;
        final int ver24 = this.permutation[var12 + 1 + var18 & 255] % 12;
        final double var25 = this.grad(var22, var4, var5, 0.0, 0.5);
        final double var26 = this.grad(var23, var8, var9, 0.0, 0.5);
        final double var27 = this.grad(ver24, var10, var11, 0.0, 0.5);
        return 70.0 * (var25 + var26 + var27);
    }

}
