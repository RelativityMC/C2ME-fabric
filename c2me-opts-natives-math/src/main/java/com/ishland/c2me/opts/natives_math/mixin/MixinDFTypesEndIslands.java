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

import com.ishland.c2me.base.mixin.access.ISimplexNoiseSampler;
import com.ishland.c2me.opts.natives_math.common.Bindings;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

@Mixin(DensityFunctionTypes.EndIslands.class)
public abstract class MixinDFTypesEndIslands {

    @Shadow @Final private SimplexNoiseSampler sampler;

    @Shadow
    protected static float sample(SimplexNoiseSampler sampler, int x, int z) {
        return 0;
    }

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private MemorySegment c2me$samplerData = null;
    @Unique
    private long c2me$samplerDataPtr;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        int[] permutation = ((ISimplexNoiseSampler) this.sampler).getPermutation();
        MemorySegment segment = this.c2me$arena.allocate(permutation.length * 4L, 64);
        MemorySegment.copy(MemorySegment.ofArray(permutation), 0L, segment, 0L, permutation.length * 4L);
        VarHandle.fullFence();
        this.c2me$samplerData = segment;
        this.c2me$samplerDataPtr = segment.address();
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        if (this.c2me$samplerDataPtr != 0L) {
            return ((double) Bindings.c2me_natives_end_islands_sample(this.c2me$samplerDataPtr, pos.blockX() / 8, pos.blockZ() / 8) - 8.0) / 128.0;
        } else {
            return ((double)sample(this.sampler, pos.blockX() / 8, pos.blockZ() / 8) - 8.0) / 128.0;
        }
    }

}
