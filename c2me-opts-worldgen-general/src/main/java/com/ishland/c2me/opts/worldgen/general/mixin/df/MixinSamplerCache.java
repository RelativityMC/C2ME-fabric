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

package com.ishland.c2me.opts.worldgen.general.mixin.df;

import com.google.common.collect.MapMaker;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.sampler.Sampler;
import net.minecraft.world.gen.sampler.SamplerCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Mixin(SamplerCache.class)
public class MixinSamplerCache {

    @Shadow
    @Final
    private Map<DensityFunction, Sampler> compileCache;
    @Shadow
    @Final
    private Function<DensityFunction, Sampler> compileFunc;
    @Unique
    private ConcurrentMap<DensityFunction, Sampler> c2me$weakSamplers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.c2me$initWeakSamplers();
    }

    @Unique
    private ConcurrentMap<DensityFunction, Sampler> c2me$initWeakSamplers() {
        ConcurrentMap<DensityFunction, Sampler> weakSamplers = this.c2me$weakSamplers;
        if (weakSamplers == null) {
            weakSamplers = this.c2me$weakSamplers = new MapMaker().weakKeys().makeMap();
        }
        return weakSamplers;
    }

    /**
     * @author ishland
     * @reason avoid expensive equals() wherever possible
     */
    @Overwrite
    public Sampler getOrCompile(final DensityFunction func) {
        ConcurrentMap<DensityFunction, Sampler> weakSamplers = this.c2me$initWeakSamplers();
        Sampler cached = weakSamplers.get(func);
        if (cached != null) return cached;
        Sampler real = this.compileCache.computeIfAbsent(func, this.compileFunc);
        weakSamplers.putIfAbsent(func, real);
        return real;
    }

}
