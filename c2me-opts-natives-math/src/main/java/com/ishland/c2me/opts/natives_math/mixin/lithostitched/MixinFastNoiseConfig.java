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

import com.ishland.c2me.base.mixin.access.lithostitched.IFNL;
import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.ducks.IFNLState;
import com.ishland.c2me.opts.natives_math.common.ducks.INativePointer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.ishland.c2me.opts.natives_math.common.integration.lithostitched.FNLUnsafeBindings.*;

@Pseudo
@Mixin(targets = "dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig", remap = false)
public class MixinFastNoiseConfig implements INativePointer, IFNLState {

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private BindingsTemplate.FNLState c2me$state = null;
    @Unique
    private MemorySegment c2me$stateSegment = null;
    @Unique
    private long c2me$statePtr;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.c2me$state = new BindingsTemplate.FNLState(
                ((IFNL) fnl(this)).getSeed(),
                ((IFNL) fnl(this)).getFrequency(),
                ((Enum<?>) field(this, "mNoiseType", true)).ordinal(),
                ((Enum<?>) field(this, "mRotationType3D", true)).ordinal(),
                ((Enum<?>) field(this, "mFractalType", true)).ordinal(),
                ((IFNL) fnl(this)).getOctaves(),
                ((IFNL) fnl(this)).getLacunarity(),
                ((IFNL) fnl(this)).getGain(),
                ((IFNL) fnl(this)).getWeightedStrength(),
                ((IFNL) fnl(this)).getPingPongStrength(),
                ((Enum<?>) field(this, "mCellularDistanceFunction", true)).ordinal(),
                ((Enum<?>) field(this, "mCellularReturnType", true)).ordinal(),
                ((IFNL) fnl(this)).getCellularJitterModifier(),
                ((Enum<?>) field(this, "mDomainWarpType", true)).ordinal(),
                ((IFNL) fnl(this)).getDomainWarpAmp()
        );        this.c2me$stateSegment = BindingsTemplate.fnl_state$create(this.c2me$arena, this.c2me$state);
        this.c2me$statePtr = this.c2me$stateSegment.address();
    }

    /**
     * @author ebo2022
     * @reason replace impl
     */
    @Overwrite
    public double sample(double x, double y, double z) {
        if (c2me$statePtr != 0L) {
            return Bindings.c2me_natives_fnlGetNoise3D(this.c2me$statePtr, x, y, z);
        } else {
            return ((IFNL) fnl(this)).invokeGetNoise(x, y, z);
        }
    }

    @Override
    public BindingsTemplate.FNLState c2me$getState() {
        return this.c2me$state;
    }

    @Override
    public long c2me$getPointer() {
        return c2me$statePtr;
    }
}
