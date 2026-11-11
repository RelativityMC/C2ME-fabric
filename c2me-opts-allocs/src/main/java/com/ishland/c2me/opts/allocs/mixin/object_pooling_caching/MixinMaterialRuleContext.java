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
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.sampler.SampleBuffer;
import net.minecraft.world.gen.surfacebuilder.MaterialRuleContext;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MaterialRuleContext.class)
public class MixinMaterialRuleContext {

    @Redirect(method = "getDensityProvider", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/sampler/SampleBuffer;withCount(I)Lnet/minecraft/world/gen/sampler/SampleBuffer;"))
    private SampleBuffer redirectPooledBuffer(int count) {
        if (ObjectCachingUtils.POOLED_SAMPLE_BUFFER_ALLOCATOR.isBound()) {
            return ObjectCachingUtils.POOLED_SAMPLE_BUFFER_ALLOCATOR.get().apply(count);
        } else {
            return SampleBuffer.withCount(count);
        }
    }

    @Unique
    private Object2ReferenceOpenHashMap<DensityFunction, MaterialRules.DensityProvider> c2me$densityProviderCache;

    @WrapMethod(method = "getDensityProvider")
    private MaterialRules.DensityProvider dedupDensityProvider(DensityFunction func, boolean eager, Operation<MaterialRules.DensityProvider> original) {
        var c2me$densityProviderCache = this.c2me$densityProviderCache;
        if (c2me$densityProviderCache == null) {
            c2me$densityProviderCache = this.c2me$densityProviderCache = new Object2ReferenceOpenHashMap<>();
        }
        MaterialRules.DensityProvider densityProvider = c2me$densityProviderCache.get(func);
        if (densityProvider == null) {
            densityProvider = original.call(func, eager);
            if (eager) {
                c2me$densityProviderCache.put(func, densityProvider);
            }
        }
        return densityProvider;
    }

}
