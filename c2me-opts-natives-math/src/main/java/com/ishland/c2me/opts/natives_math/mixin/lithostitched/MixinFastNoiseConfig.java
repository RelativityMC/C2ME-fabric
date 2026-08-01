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

package com.ishland.c2me.opts.natives_math.mixin.lithostitched;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.TrackingVH;
import com.ishland.c2me.opts.natives_math.common.ducks.IFNLState;
import com.ishland.c2me.opts.natives_math.common.ducks.INativePointer;
import com.ishland.c2me.opts.natives_math.common.integration.lithostitched.FNLBindings;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

@Pseudo
@Mixin(targets = "dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig", remap = false)
public class MixinFastNoiseConfig implements INativePointer, IFNLState {

    @Unique
    private Arena c2me$arena;
    @Unique
    private BindingsTemplate.FNLState c2me$state = null;
    @Unique
    private MemorySegment c2me$stateSegment = null;
    @Unique
    private long c2me$statePtr;

    // see TrackingVH
    @Unique
    public int c2me$sampledCount;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.c2me$sampledCount = 0;
    }

    @Inject(method = "bind", at = @At("RETURN"))
    private void postBind(long seed, CallbackInfo ci) {
        this.c2me$state = FNLBindings.tryParseState(this);
        // ensures fnl has been modified in constructor & seed is set
    }

    @Unique
    private void c2me$profileInitPointers() {
        if (this.c2me$arena == null && (int) TrackingVH.VH_FastNoiseConfig.get((Object) this) <= TrackingVH.THRESHOLD) {
            if ((int) TrackingVH.VH_FastNoiseConfig.getAndAdd( (Object) this, 1) == TrackingVH.THRESHOLD) {
//              new Throwable(String.format("Promoting FastNoiseConfig sampler %d to native", System.identityHashCode(this))).printStackTrace();
                if (this.c2me$state != null) {
                    this.c2me$arena = Arena.ofAuto();
                    this.c2me$stateSegment = BindingsTemplate.fnl_state$create(this.c2me$arena, this.c2me$state);
                    this.c2me$statePtr = this.c2me$stateSegment.address();
                }
            }
        }
    }

    /**
     * @author ebo2022
     * @reason replace impl
     */
    @Overwrite
    public double sample(double x, double y, double z) {
        long c2me$statePtr1 = this.c2me$statePtr;
        if (c2me$statePtr1 != 0L) {
            return Bindings.c2me_natives_fnlGetNoise3D(c2me$statePtr1, x, y, z);
        } else {
            this.c2me$profileInitPointers();
            return FNLBindings.call_FNL$GetNoise(this, x, y, z);
        }
    }

    @Override
    public BindingsTemplate.FNLState c2me$getState() {
        return this.c2me$state;
    }

    @Override
    public long c2me$getPointer() {
        return this.c2me$statePtr;
    }

}
