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

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.allocs.common.ObjectCachingUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1_1690;
import net.minecraft.class_1_1703;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.sampler.ContextualSamplerProvider;
import net.minecraft.world.gen.sampler.PooledSampleBuffer;
import net.minecraft.world.gen.sampler.SampleBuffer;
import net.minecraft.world.gen.sampler.SampleBufferPool;
import net.minecraft.world.gen.surfacebuilder.MaterialRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.IntFunction;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

//    @WrapOperation(method = "method_1_10091", at = @At(value = "NEW", target = "(Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/sampler/ContextualSamplerProvider;Lnet/minecraft/util/math/noise/SamplingRegion;Lnet/minecraft/world/gen/YOffset$class_1_1694;Lnet/minecraft/world/gen/surfacebuilder/MaterialRule;Lnet/minecraft/class_1_1690;Ljava/util/Set;)Lnet/minecraft/class_1_1703;"))
//    private class_1_1703 wrapSurfaceBuilder(NoiseConfig randomState, ContextualSamplerProvider densitySamplers, SamplingRegion expectedVolume, YOffset.class_1_1694 verticalAnchorContext, MaterialRule materialRule, class_1_1690 biomeResolver, Set possibleBiomes, Operation<class_1_1703> original, @Local ChunkNoiseSampler chunkNoiseSampler) {
//        ArrayList<PooledSampleBuffer> allocatedBuffers = new ArrayList<>();
//        SampleBufferPool pool = ((IChunkNoiseSampler) chunkNoiseSampler).getPool();
//        IntFunction<SampleBuffer> allocator = size -> {
//            PooledSampleBuffer buffer = pool.allocate(size);
//            allocatedBuffers.add(buffer);
//            return buffer;
//        };
//        try {
//            return ScopedValue
//                    .where(ObjectCachingUtils.POOLED_SAMPLE_BUFFER_ALLOCATOR, allocator)
//                    .call(() -> original.call(randomState, densitySamplers, expectedVolume, verticalAnchorContext, materialRule, biomeResolver, possibleBiomes));
//        } finally {
//            for (int i = 0, allocatedBuffersSize = allocatedBuffers.size(); i < allocatedBuffersSize; i++) {
//                PooledSampleBuffer buffer = allocatedBuffers.get(i);
//                buffer.close();
//            }
//        }
//    }

}
