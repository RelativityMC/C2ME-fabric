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
import com.ishland.c2me.base.mixin.access.IEndOuterIslandsDensityFunction;
import com.ishland.c2me.base.mixin.access.ILatticedNoiseSampler;
import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.flowsched.util.Assertions;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.sampler.SamplingContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

@Mixin(targets = "net.minecraft.world.gen.densityfunction.EndOuterIslandsDensityFunction$Sampler")
public abstract class MixinEndOuterIslandsDensityFunctionSampler {

    @Shadow @Final private SimplexNoiseSampler islandNoise;

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private MemorySegment c2me$samplerData = null;
    @Unique
    private long c2me$samplerDataPtr;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        byte[] permutation = ((ILatticedNoiseSampler) this.islandNoise).getPermutation();
        Assertions.assertTrue(permutation.length == 256);
        MemorySegment segment = this.c2me$arena.allocate(permutation.length, 64);
        MemorySegment.copy(MemorySegment.ofArray(MemoryUtil.packByte2int(permutation)), 0L, segment, 0L, permutation.length);
        VarHandle.fullFence();
        this.c2me$samplerData = segment;
        this.c2me$samplerDataPtr = segment.address();
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public float sample(final SamplingContext context, final int x, final int y, final int z) {
        if (this.c2me$samplerDataPtr != 0L) {
            return (Bindings.c2me_natives_end_islands_sample(this.c2me$samplerDataPtr, x / 8, z / 8) - 8.0F) / 128.0F;
        } else {
            return (IEndOuterIslandsDensityFunction.invokeSample(this.islandNoise, x / 8, z / 8) - 8.0F) / 128.0F;
        }
    }

}
