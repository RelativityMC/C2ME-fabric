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

package com.ishland.c2me.opts.allocs.mixin.object_pooling_caching;

import com.ishland.c2me.opts.allocs.common.ObjectCachingUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.sampler.SampleBufferPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Redirect(method = "populateBiomesSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/noise/NoiseConfig;allocatePool()Lnet/minecraft/world/gen/sampler/SampleBufferPool;"))
    private SampleBufferPool redirectPoolAlloc(NoiseConfig instance) {
        return ObjectCachingUtils.borrowCachedOrNewSampleBufferPool();
    }

    @Redirect(method = "populateBiomesSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/noise/NoiseConfig;reclaimPool(Lnet/minecraft/world/gen/sampler/SampleBufferPool;)V"))
    private void redirectPoolRelease(NoiseConfig instance, SampleBufferPool pool) {
        ObjectCachingUtils.returnCachedSampleBufferPool(pool);
    }

}
