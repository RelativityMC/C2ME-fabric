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
