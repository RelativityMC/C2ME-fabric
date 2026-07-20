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

package com.ishland.c2me.opts.dfc.mixin.lithostitched.equality;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

import static com.ishland.c2me.opts.natives_math.common.integration.lithostitched.FNLUnsafeBindings.*;

@Pseudo
@Mixin(targets = "dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL", remap = false)
public class MixinFNL {

    @Shadow private int mSeed;
    @Shadow private float mFrequency;
    @Shadow private int mOctaves;
    @Shadow private float mLacunarity;
    @Shadow private float mGain;
    @Shadow private float mWeightedStrength;
    @Shadow private float mPingPongStrength;
    @Shadow private float mFractalBounding;
    @Shadow private float mCellularJitterModifier;
    @Shadow private float mDomainWarpAmp;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinFNL that = (MixinFNL) object;
        return Integer.compare(mSeed, that.mSeed) == 0
                && Float.compare(mFrequency, that.mFrequency) == 0
                && Objects.equals(field(this, "mNoiseType", false), field(that, "mNoiseType", false))
                && Objects.equals(field(this, "mRotationType3D", false), field(that, "mRotationType3D", false))
                && Objects.equals(field(this, "mTransformType3D", false), field(that, "mTransformType3D", false))
                && Objects.equals(field(this, "mFractalType", false), field(that, "mFractalType", false))
                && Integer.compare(mOctaves, that.mOctaves) == 0
                && Float.compare(mLacunarity, that.mLacunarity) == 0
                && Float.compare(mGain, that.mGain) == 0
                && Float.compare(mWeightedStrength, that.mWeightedStrength) == 0
                && Float.compare(mPingPongStrength, that.mPingPongStrength) == 0
                && Float.compare(mFractalBounding, that.mFractalBounding) == 0
                && Objects.equals(field(this, "mCellularDistanceFunction", false), field(that, "mCellularDistanceFunction", false))
                && Objects.equals(field(this, "mCellularReturnType", false), field(that, "mCellularReturnType", false))
                && Objects.equals(mCellularJitterModifier, that.mCellularJitterModifier)
                && Objects.equals(field(this, "mDomainWarpType", false), field(that, "mDomainWarpType", false))
                && Objects.equals(field(this, "mWarpTransformType3D", false), field(that, "mWarpTransformType3D", false))
                && Float.compare(mDomainWarpAmp, that.mDomainWarpAmp) == 0;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Integer.hashCode(mSeed);
        result = 31 * result + Float.hashCode(mFrequency);
        result = 31 * result + Objects.hashCode(field(this, "mNoiseType", false));
        result = 31 * result + Objects.hashCode(field(this, "mRotationType3D", false));
        result = 31 * result + Objects.hashCode(field(this, "mTransformType3D", false));
        result = 31 * result + Objects.hashCode(field(this, "mFractalType", false));
        result = 31 * result + Integer.hashCode(mOctaves);
        result = 31 * result + Float.hashCode(mLacunarity);
        result = 31 * result + Float.hashCode(mGain);
        result = 31 * result + Float.hashCode(mWeightedStrength);
        result = 31 * result + Float.hashCode(mPingPongStrength);
        result = 31 * result + Float.hashCode(mFractalBounding);
        result = 31 * result + Objects.hashCode(field(this, "mCellularDistanceFunction", false));
        result = 31 * result + Objects.hashCode(field(this, "mCellularReturnType", false));
        result = 31 * result + Objects.hashCode(mCellularJitterModifier);
        result = 31 * result + Objects.hashCode(field(this, "mDomainWarpType", false));
        result = 31 * result + Objects.hashCode(field(this, "mWarpTransformType3D", false));
        result = 31 * result + Float.hashCode(mDomainWarpAmp);
        return result;
    }
}
