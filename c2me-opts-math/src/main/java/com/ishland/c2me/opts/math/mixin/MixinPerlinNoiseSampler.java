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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PerlinNoiseSampler.class, priority = 1090)
public abstract class MixinPerlinNoiseSampler extends LatticedNoiseSampler {

    protected MixinPerlinNoiseSampler(Random random) {
        super(random);
    }

    @Unique
    private int[] c2me$packedPermutations;

    @Unique
    private int[] c2me$initPackedPermutations() {
        int[] packedPermutations = this.c2me$packedPermutations;
        if (packedPermutations == null) {
            this.c2me$packedPermutations = packedPermutations = NoisePacking.packPermutation0(this.permutation);
        }
        return packedPermutations;
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.c2me$initPackedPermutations();
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
//        final int hashr__ = NoisePacking.indexPackedPermutation(permutations, px0);
//        final int hash0__ = hashr__ & 0xff;
//        final int hash1__ = (hashr__ >>> 8) & 0xff;
//        final int hash0r_ = NoisePacking.indexPackedPermutation(permutations, hash0__ + py0);
//        final int hash1r_ = NoisePacking.indexPackedPermutation(permutations, hash1__ + py0);
//        final int hash00_ = hash0r_ & 0xff;
//        final int hash01_ = (hash0r_ >>> 8) & 0xff;
//        final int hash10_ = hash1r_ & 0xff;
//        final int hash11_ = (hash1r_ >>> 8) & 0xff;
//        final int hash00r = NoisePacking.indexPackedPermutation(permutations, hash00_ + pz0);
//        final int hash10r = NoisePacking.indexPackedPermutation(permutations, hash10_ + pz0);
//        final int hash01r = NoisePacking.indexPackedPermutation(permutations, hash01_ + pz0);
//        final int hash11r = NoisePacking.indexPackedPermutation(permutations, hash11_ + pz0);
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

}
