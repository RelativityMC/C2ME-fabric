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

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.TrackingVH;
import com.ishland.c2me.opts.natives_math.common.ducks.INativePointer;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

@Mixin(DoublePerlinNoiseSampler.class)
public class MixinDoublePerlinNoiseSampler implements INativePointer {

    @Shadow @Final private double amplitude;
    @Shadow @Final private OctavePerlinNoiseSampler firstSampler;
    @Shadow @Final private OctavePerlinNoiseSampler secondSampler;
    @Unique
    private Arena c2me$arena = null;
    @Unique
    private MemorySegment c2me$samplerData = null;
    @Unique
    private long c2me$samplerDataPtr;

    // see TrackingVH
    @Unique
    public int c2me$sampledCount;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.c2me$sampledCount = 0;
    }

    @Unique
    private void c2me$profileInitPointers() {
        if (this.c2me$arena == null && (int) TrackingVH.VH_DoublePerlinNoiseSampler.get((DoublePerlinNoiseSampler) (Object) this) <= TrackingVH.THRESHOLD) {
            if ((int) TrackingVH.VH_DoublePerlinNoiseSampler.getAndAdd((DoublePerlinNoiseSampler) (Object) this, 1) == TrackingVH.THRESHOLD) {
//                new Throwable(String.format("Promoting DoublePerlinNoiseSampler %d to native", System.identityHashCode(this))).printStackTrace();
                this.c2me$arena = Arena.ofAuto();
                this.c2me$samplerData = BindingsTemplate.double_octave_sampler_data$create(this.c2me$arena, this.firstSampler, this.secondSampler, this.amplitude, false);
                this.c2me$samplerDataPtr = this.c2me$samplerData.address();
            }
        }
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public double sample(double x, double y, double z) {
        long c2me$samplerDataPtr1 = this.c2me$samplerDataPtr;
        if (c2me$samplerDataPtr1 != 0L) {
            return Bindings.c2me_natives_noise_perlin_double(c2me$samplerDataPtr1, x, y, z);
        } else {
            this.c2me$profileInitPointers();
            double d = x * 1.0181268882175227;
            double e = y * 1.0181268882175227;
            double f = z * 1.0181268882175227;
            return (this.firstSampler.sample(x, y, z) + this.secondSampler.sample(d, e, f)) * this.amplitude;
        }
    }

    @Override
    public long c2me$getPointer() {
        return this.c2me$samplerDataPtr;
    }
}
