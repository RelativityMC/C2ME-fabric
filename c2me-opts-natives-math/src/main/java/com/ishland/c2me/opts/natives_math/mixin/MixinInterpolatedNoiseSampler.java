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

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

@Mixin(InterpolatedNoiseSampler.class)
public class MixinInterpolatedNoiseSampler {

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private MemorySegment c2me$samplerData = null;
    @Unique
    private long c2me$samplerDataPtr;

    @Inject(method = "<init>(Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;DDDDD)V", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.c2me$samplerData = BindingsTemplate.interpolated_noise_sampler$create(this.c2me$arena, (InterpolatedNoiseSampler) (Object) this, false);
        this.c2me$samplerDataPtr = this.c2me$samplerData.address();
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        return Bindings.c2me_natives_noise_interpolated(this.c2me$samplerDataPtr, pos.blockX(), pos.blockY(), pos.blockZ());
    }

}
