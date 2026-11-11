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

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.allocs.common.ObjectCachingUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.sampler.PooledSampleBuffer;
import net.minecraft.world.gen.sampler.SampleBuffer;
import net.minecraft.world.gen.sampler.SampleBufferPool;
import net.minecraft.world.gen.surfacebuilder.MaterialRule;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.IntFunction;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    @WrapOperation(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;buildSurface(Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/biome/source/BiomeAccess;Lnet/minecraft/world/gen/HeightContext;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/gen/chunk/ChunkNoiseSampler;Lnet/minecraft/world/gen/surfacebuilder/MaterialRule;Ljava/util/Set;)V"))
    private void wrapSurfaceBuilder(SurfaceBuilder instance, NoiseConfig noiseConfig, BiomeAccess biomeAccess, HeightContext heightContext, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, MaterialRule materialRule, @Nullable Set<RegistryEntry<Biome>> includedBiomes, Operation<Void> original) {
        ArrayList<PooledSampleBuffer> allocatedBuffers = new ArrayList<>();
        SampleBufferPool pool = ((IChunkNoiseSampler) chunkNoiseSampler).getPool();
        IntFunction<SampleBuffer> allocator = size -> {
            PooledSampleBuffer buffer = pool.allocate(size);
            allocatedBuffers.add(buffer);
            return buffer;
        };
        try {
            ScopedValue
                    .where(ObjectCachingUtils.POOLED_SAMPLE_BUFFER_ALLOCATOR, allocator)
                    .run(() -> original.call(instance, noiseConfig, biomeAccess, heightContext, chunk, chunkNoiseSampler, materialRule, includedBiomes));
        } finally {
            for (int i = 0, allocatedBuffersSize = allocatedBuffers.size(); i < allocatedBuffersSize; i++) {
                PooledSampleBuffer buffer = allocatedBuffers.get(i);
                buffer.close();
            }
        }
    }

}
