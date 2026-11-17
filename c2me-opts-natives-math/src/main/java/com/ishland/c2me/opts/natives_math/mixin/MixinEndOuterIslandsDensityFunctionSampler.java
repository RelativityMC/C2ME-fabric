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
