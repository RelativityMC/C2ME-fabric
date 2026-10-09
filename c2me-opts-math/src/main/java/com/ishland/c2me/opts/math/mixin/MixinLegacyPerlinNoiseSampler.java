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

package com.ishland.c2me.opts.math.mixin;

import com.ishland.c2me.base.common.util.NoisePacking;
import com.ishland.c2me.opts.math.common.ducks.PerlinNoiseSamplerExtension;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.LegacyPerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static com.ishland.c2me.base.common.util.NoisePacking.FLAT_SIMPLEX_GRAD_F32;

@Mixin(LegacyPerlinNoiseSampler.class)
public abstract class MixinLegacyPerlinNoiseSampler extends PerlinNoiseSampler implements PerlinNoiseSamplerExtension {

    @Shadow
    @Final
    private double offsetScale;

    public MixinLegacyPerlinNoiseSampler(Random random) {
        super(random);
    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    public void fill(final SampleBuffer buf, final SamplingRegion region, final double scaleXz, final double scaleY, final float outputScale) {
        final int[] permutations = this.c2me$initPackedPermutations();

        int px0_prev = Integer.MAX_VALUE;
        int px0_perm = 0;
        int px1_perm = 0;

        int py0_prev = Integer.MAX_VALUE;
        int px0_py0_perm = 0;
        int px1_py0_perm = 0;
        int px0_py1_perm = 0;
        int px1_py1_perm = 0;

        int pz0_prev = Integer.MAX_VALUE;
//        int a000_0 = 0, a000_1 = 0, a000_2 = 0;
//        int a100_0 = 0, a100_1 = 0, a100_2 = 0;
//        int a010_0 = 0, a010_1 = 0, a010_2 = 0;
//        int a110_0 = 0, a110_1 = 0, a110_2 = 0;
//        int a001_0 = 0, a001_1 = 0, a001_2 = 0;
//        int a101_0 = 0, a101_1 = 0, a101_2 = 0;
//        int a011_0 = 0, a011_1 = 0, a011_2 = 0;
//        int a111_0 = 0, a111_1 = 0, a111_2 = 0;
        float a000_0 = 0f, a000_1 = 0f, a000_2 = 0f;
        float a100_0 = 0f, a100_1 = 0f, a100_2 = 0f;
        float a010_0 = 0f, a010_1 = 0f, a010_2 = 0f;
        float a110_0 = 0f, a110_1 = 0f, a110_2 = 0f;
        float a001_0 = 0f, a001_1 = 0f, a001_2 = 0f;
        float a101_0 = 0f, a101_1 = 0f, a101_2 = 0f;
        float a011_0 = 0f, a011_1 = 0f, a011_2 = 0f;
        float a111_0 = 0f, a111_1 = 0f, a111_2 = 0f;

        for (int offX = 0; offX < region.sizeX(); offX++) {
            final int blockX = region.minBlockX() + offX * region.stepBlockX();
            final double x = (double) blockX * scaleXz;
            final double x1 = wrapCoord(x) + originX;
            final double floorX = Math.floor(x1);
            final double relX = x1 - floorX;
            final float fx0 = (float) relX;
            final int px0 = (int) floorX;
            final float fx1 = fx0 - 1.0f;

            if (px0_prev != px0 || px0_prev == Integer.MAX_VALUE) {
                final int pxr = NoisePacking.indexPackedPermutation(permutations, px0);
                px0_perm = pxr & 0xff;
                px1_perm = (pxr >>> 8) & 0xff;
                px0_prev = px0;
                py0_prev = Integer.MAX_VALUE;
                pz0_prev = Integer.MAX_VALUE;
            }

            for (int offY = 0; offY < region.sizeY(); offY++) {
                final int blockY = region.minBlockY() + offY * region.stepBlockY();
                final double y = (double) blockY * scaleY;
                final double y1 = wrapCoord(y) + originY;
                final double floorY = Math.floor(y1);
                final double relY = y1 - floorY;
                final double fy = Math.floor(((y >= 0.0 && y < relY) ? y : relY) / this.offsetScale + 1.0E-7) * this.offsetScale;
                final int py0 = (int) floorY;
                final float fy0 = (float) relY;
                final float fadeLocalY = (float) relY;
                final float fy1 = fy0 - 1.0f;

                if (py0_prev != py0 || py0_prev == Integer.MAX_VALUE) {
                    final int px0r = NoisePacking.indexPackedPermutation(permutations, px0_perm + py0);
                    final int px1r = NoisePacking.indexPackedPermutation(permutations, px1_perm + py0);
                    px0_py0_perm = px0r & 0xff;
                    px1_py0_perm = px1r & 0xff;
                    px0_py1_perm = (px0r >>> 8) & 0xff;
                    px1_py1_perm = (px1r >>> 8) & 0xff;
                    py0_prev = py0;
                    pz0_prev = Integer.MAX_VALUE;
                }

                for (int offZ = 0; offZ < region.sizeZ(); offZ++) {
                    final int blockZ = region.minBlockZ() + offZ * region.stepBlockZ();
                    final double z = (double) blockZ * scaleXz;
                    final int idx = offY + (offX + offZ * region.sizeX()) * region.sizeY();
                    final double z1 = wrapCoord(z) + originZ;
                    final double floorZ = Math.floor(z1);
                    final double relZ = z1 - floorZ;
                    final int pz0 = (int) floorZ;
                    final float fz0 = (float) relZ;
                    final float fz1 = fz0 - 1.0f;

                    if (pz0_prev != pz0 || pz0_prev == Integer.MAX_VALUE) {
                        // final int px00r = NoisePacking.indexPackedPermutation(perms, px0_py0_perm + pz0);
                        // final int px10r = NoisePacking.indexPackedPermutation(perms, px1_py0_perm + pz0);
                        // final int px01r = NoisePacking.indexPackedPermutation(perms, px0_py1_perm + pz0);
                        // final int px11r = NoisePacking.indexPackedPermutation(perms, px1_py1_perm + pz0);
                        //
                        // int r;
                        // r = FLAT_SIMPLEX_GRAD_I8[px00r        & 0xF];
                        // a000_0 = (byte) r; a000_1 = (byte) (r >> 8); a000_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[px10r        & 0xF];
                        // a100_0 = (byte) r; a100_1 = (byte) (r >> 8); a100_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[px01r        & 0xF];
                        // a010_0 = (byte) r; a010_1 = (byte) (r >> 8); a010_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[px11r        & 0xF];
                        // a110_0 = (byte) r; a110_1 = (byte) (r >> 8); a110_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[(px00r >>> 8) & 0xF];
                        // a001_0 = (byte) r; a001_1 = (byte) (r >> 8); a001_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[(px10r >>> 8) & 0xF];
                        // a101_0 = (byte) r; a101_1 = (byte) (r >> 8); a101_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[(px01r >>> 8) & 0xF];
                        // a011_0 = (byte) r; a011_1 = (byte) (r >> 8); a011_2 = (byte) (r >> 16);
                        // r = FLAT_SIMPLEX_GRAD_I8[(px11r >>> 8) & 0xF];
                        // a111_0 = (byte) r; a111_1 = (byte) (r >> 8); a111_2 = (byte) (r >> 16);

                        final int px00r = NoisePacking.indexPackedPermutation(permutations, px0_py0_perm + pz0);
                        final int px10r = NoisePacking.indexPackedPermutation(permutations, px1_py0_perm + pz0);
                        final int px01r = NoisePacking.indexPackedPermutation(permutations, px0_py1_perm + pz0);
                        final int px11r = NoisePacking.indexPackedPermutation(permutations, px1_py1_perm + pz0);

                        int b;
                        b = (px00r & 0xF) << 2;
                        a000_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a000_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a000_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = (px10r & 0xF) << 2;
                        a100_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a100_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a100_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = (px01r & 0xF) << 2;
                        a010_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a010_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a010_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = (px11r & 0xF) << 2;
                        a110_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a110_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a110_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = ((px00r >>> 8) & 0xF) << 2;
                        a001_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a001_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a001_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = ((px10r >>> 8) & 0xF) << 2;
                        a101_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a101_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a101_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = ((px01r >>> 8) & 0xF) << 2;
                        a011_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a011_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a011_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        b = ((px11r >>> 8) & 0xF) << 2;
                        a111_0 = FLAT_SIMPLEX_GRAD_F32[b];
                        a111_1 = FLAT_SIMPLEX_GRAD_F32[b + 1];
                        a111_2 = FLAT_SIMPLEX_GRAD_F32[b + 2];

                        pz0_prev = pz0;
                    }

                    final float f000 = a000_0 * fx0 + a000_1 * fy0 + a000_2 * fz0;
                    final float f100 = a100_0 * fx1 + a100_1 * fy0 + a100_2 * fz0;
                    final float f010 = a010_0 * fx0 + a010_1 * fy1 + a010_2 * fz0;
                    final float f110 = a110_0 * fx1 + a110_1 * fy1 + a110_2 * fz0;
                    final float f001 = a001_0 * fx0 + a001_1 * fy0 + a001_2 * fz1;
                    final float f101 = a101_0 * fx1 + a101_1 * fy0 + a101_2 * fz1;
                    final float f011 = a011_0 * fx0 + a011_1 * fy1 + a011_2 * fz1;
                    final float f111 = a111_0 * fx1 + a111_1 * fy1 + a111_2 * fz1;

                    final float dx = MathHelper.perlinFade(fx0);
                    final float dy = MathHelper.perlinFade(fadeLocalY);
                    final float dz = MathHelper.perlinFade(fz0);

                    buf.add(
                            idx,
                            MathHelper.lerp3(dx, dy, dz,
                                    f000, f100, f010, f110,
                                    f001, f101, f011, f111) * outputScale
                    );
                }
            }
        }
    }


}
