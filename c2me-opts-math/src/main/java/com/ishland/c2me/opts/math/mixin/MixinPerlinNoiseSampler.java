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
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.ishland.c2me.base.common.util.NoisePacking.FLAT_SIMPLEX_GRAD_F32;

@Mixin(value = PerlinNoiseSampler.class, priority = 1090)
public abstract class MixinPerlinNoiseSampler extends LatticedNoiseSampler implements PerlinNoiseSamplerExtension {

    protected MixinPerlinNoiseSampler(Random random) {
        super(random);
    }

    @Unique
    private int[] c2me$packedPermutationsForJava;

    @Unique
    @Override
    public int[] c2me$initPackedPermutationsForJava() {
        int[] packedPermutations = this.c2me$packedPermutationsForJava;
        if (packedPermutations == null) {
            this.c2me$packedPermutationsForJava = packedPermutations = NoisePacking.packPermutation512b(this.permutation);
        }
        return packedPermutations;
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.c2me$initPackedPermutationsForJava();
    }

//    /**
//     * @author ishland
//     * @reason optimize
//     */
//    @Overwrite
//    protected float sample(
//            final int px0, final int py0, final int pz0, final float fx0, final float fy0, final float fz0, final float fadeLocalY
//    ) {
//        final int[] permutations = this.c2me$initPackedPermutations();
//
//        if (permutations.length != 128) throw new AssertionError("permutations.length != 128");
//
//        final float fx1 = fx0 - 1.0f;
//        final float fy1 = fy0 - 1.0f;
//        final float fz1 = fz0 - 1.0f;
//
//        final int hashr__ = NoisePacking.indexPacked512bPermutation(permutations, px0);
//        final int hash0__ = hashr__ & 0xff;
//        final int hash1__ = (hashr__ >>> 8) & 0xff;
//        final int hash0r_ = NoisePacking.indexPacked512bPermutation(permutations, hash0__ + py0);
//        final int hash1r_ = NoisePacking.indexPacked512bPermutation(permutations, hash1__ + py0);
//        final int hash00_ = hash0r_ & 0xff;
//        final int hash01_ = (hash0r_ >>> 8) & 0xff;
//        final int hash10_ = hash1r_ & 0xff;
//        final int hash11_ = (hash1r_ >>> 8) & 0xff;
//        final int hash00r = NoisePacking.indexPacked512bPermutation(permutations, hash00_ + pz0);
//        final int hash10r = NoisePacking.indexPacked512bPermutation(permutations, hash10_ + pz0);
//        final int hash01r = NoisePacking.indexPacked512bPermutation(permutations, hash01_ + pz0);
//        final int hash11r = NoisePacking.indexPacked512bPermutation(permutations, hash11_ + pz0);
//        final int hash000 = hash00r & 0xf;
//        final int hash100 = hash10r & 0xf;
//        final int hash010 = hash01r & 0xf;
//        final int hash110 = hash11r & 0xf;
//        final int hash001 = (hash00r >>> 8) & 0xf;
//        final int hash101 = (hash10r >>> 8) & 0xf;
//        final int hash011 = (hash01r >>> 8) & 0xf;
//        final int hash111 = (hash11r >>> 8) & 0xf;
//
//        final int res000 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash000];
//        final int res100 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash100];
//        final int res010 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash010];
//        final int res110 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash110];
//        final int res001 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash001];
//        final int res101 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash101];
//        final int res011 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash011];
//        final int res111 = NoisePacking.FLAT_SIMPLEX_GRAD_I8[hash111];
//
//        final float f000 = NoisePacking.gradX(res000) * fx0 + NoisePacking.gradY(res000) * fy0 + NoisePacking.gradZ(res000) * fz0;
//        final float f100 = NoisePacking.gradX(res100) * fx1 + NoisePacking.gradY(res100) * fy0 + NoisePacking.gradZ(res100) * fz0;
//        final float f010 = NoisePacking.gradX(res010) * fx0 + NoisePacking.gradY(res010) * fy1 + NoisePacking.gradZ(res010) * fz0;
//        final float f110 = NoisePacking.gradX(res110) * fx1 + NoisePacking.gradY(res110) * fy1 + NoisePacking.gradZ(res110) * fz0;
//        final float f001 = NoisePacking.gradX(res001) * fx0 + NoisePacking.gradY(res001) * fy0 + NoisePacking.gradZ(res001) * fz1;
//        final float f101 = NoisePacking.gradX(res101) * fx1 + NoisePacking.gradY(res101) * fy0 + NoisePacking.gradZ(res101) * fz1;
//        final float f011 = NoisePacking.gradX(res011) * fx0 + NoisePacking.gradY(res011) * fy1 + NoisePacking.gradZ(res011) * fz1;
//        final float f111 = NoisePacking.gradX(res111) * fx1 + NoisePacking.gradY(res111) * fy1 + NoisePacking.gradZ(res111) * fz1;
//
//        final float dx = MathHelper.perlinFade(fx0);
//        final float dy = MathHelper.perlinFade(fadeLocalY);
//        final float dz = MathHelper.perlinFade(fz0);
//
//        return MathHelper.lerp3(dx, dy, dz,
//                f000, f100, f010, f110,
//                f001, f101, f011, f111);
//    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    public void fill(final SampleBuffer buf, final SamplingRegion region, final double scaleXz, final double scaleY, final float outputScale) {
        final int[] permutations = this.c2me$initPackedPermutationsForJava();

        int px0_prev = Integer.MAX_VALUE;
        int px0_perm = 0;
        int px1_perm = 0;

        int py0_prev = Integer.MAX_VALUE;
        int px0_py0_perm = 0;
        int px1_py0_perm = 0;
        int px0_py1_perm = 0;
        int px1_py1_perm = 0;

        int pz0_prev = Integer.MAX_VALUE;
        float a000_0 = 0f, a000_1 = 0f, a000_2 = 0f;
        float a100_0 = 0f, a100_1 = 0f, a100_2 = 0f;
        float a010_0 = 0f, a010_1 = 0f, a010_2 = 0f;
        float a110_0 = 0f, a110_1 = 0f, a110_2 = 0f;
        float a001_0 = 0f, a001_1 = 0f, a001_2 = 0f;
        float a101_0 = 0f, a101_1 = 0f, a101_2 = 0f;
        float a011_0 = 0f, a011_1 = 0f, a011_2 = 0f;
        float a111_0 = 0f, a111_1 = 0f, a111_2 = 0f;

        // note: ordering actually doesn't matter because there's only two non-zero values
        // also FMA is possible since multiplied are *exact* but not implemented here
        float xy000 = 0f, xy100 = 0f, xy010 = 0f, xy110 = 0f;
        float xy001 = 0f, xy101 = 0f, xy011 = 0f, xy111 = 0f;

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
                final int pxr = NoisePacking.indexPacked512bPermutation(permutations, px0);
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
                final int py0 = (int) floorY;
                final float fy0 = (float) relY;
                final float fadeLocalY = (float) relY;
                final float fy1 = fy0 - 1.0f;

                if (py0_prev != py0 || py0_prev == Integer.MAX_VALUE) {
                    final int px0r = NoisePacking.indexPacked512bPermutation(permutations, px0_perm + py0);
                    final int px1r = NoisePacking.indexPacked512bPermutation(permutations, px1_perm + py0);
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
                        final int px00r = NoisePacking.indexPacked512bPermutation(permutations, px0_py0_perm + pz0);
                        final int px10r = NoisePacking.indexPacked512bPermutation(permutations, px1_py0_perm + pz0);
                        final int px01r = NoisePacking.indexPacked512bPermutation(permutations, px0_py1_perm + pz0);
                        final int px11r = NoisePacking.indexPacked512bPermutation(permutations, px1_py1_perm + pz0);

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

                        xy000 = a000_0 * fx0 + a000_1 * fy0;
                        xy100 = a100_0 * fx1 + a100_1 * fy0;
                        xy010 = a010_0 * fx0 + a010_1 * fy1;
                        xy110 = a110_0 * fx1 + a110_1 * fy1;
                        xy001 = a001_0 * fx0 + a001_1 * fy0;
                        xy101 = a101_0 * fx1 + a101_1 * fy0;
                        xy011 = a011_0 * fx0 + a011_1 * fy1;
                        xy111 = a111_0 * fx1 + a111_1 * fy1;

                        pz0_prev = pz0;
                    }

                    final float f000 = xy000 + a000_2 * fz0;
                    final float f100 = xy100 + a100_2 * fz0;
                    final float f010 = xy010 + a010_2 * fz0;
                    final float f110 = xy110 + a110_2 * fz0;
                    final float f001 = xy001 + a001_2 * fz1;
                    final float f101 = xy101 + a101_2 * fz1;
                    final float f011 = xy011 + a011_2 * fz1;
                    final float f111 = xy111 + a111_2 * fz1;

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
