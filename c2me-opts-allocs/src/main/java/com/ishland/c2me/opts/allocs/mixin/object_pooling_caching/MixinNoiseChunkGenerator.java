/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.opts.allocs.mixin.object_pooling_caching;

import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;

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
