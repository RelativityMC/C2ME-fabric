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

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.base.common.util.MemoryUtil;
import com.ishland.c2me.opts.natives_math.common.ducks.LatticedNoiseSamplerExtension;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.foreign.MemorySegment;

@Mixin(LatticedNoiseSampler.class)
public class MixinLatticedNoiseSampler implements LatticedNoiseSamplerExtension {

    @Shadow
    @Final
    protected byte[] permutation;

    @Unique
    private int[] c2me$packedPermutations;

    @Unique
    private MemorySegment c2me$packedPermutationsMemorySegment;

    @Override
    public int[] c2me$getPackedPermutations() {
        int[] packedPermutations = this.c2me$packedPermutations;
        if (packedPermutations == null) {
            packedPermutations = this.c2me$packedPermutations = MemoryUtil.packByte2int(this.permutation);
        }
        return packedPermutations;
    }

    @Override
    public MemorySegment c2me$getPackedPermutationsMemorySegment() {
        MemorySegment segment = this.c2me$packedPermutationsMemorySegment;
        if (segment == null) {
            segment = this.c2me$packedPermutationsMemorySegment = MemorySegment.ofArray(this.c2me$getPackedPermutations());
        }
        return segment;
    }
}
