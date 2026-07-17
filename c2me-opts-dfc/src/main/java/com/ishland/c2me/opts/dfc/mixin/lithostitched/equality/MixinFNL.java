package com.ishland.c2me.opts.dfc.mixin.lithostitched.equality;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Pseudo
@Mixin(targets = "dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL", remap = false)
public class MixinFNL {

    @Shadow private int mSeed;
    @Shadow private float mFrequency;
    @Shadow private Object mNoiseType;
    @Shadow private Object mRotationType3D;
    @Shadow private Object mTransformType3D;
    @Shadow private Object mFractalType;
    @Shadow private int mOctaves;
    @Shadow private float mLacunarity;
    @Shadow private float mGain;
    @Shadow private float mWeightedStrength;
    @Shadow private float mPingPongStrength;
    @Shadow private float mFractalBounding;
    @Shadow private Object mCellularDistanceFunction;
    @Shadow private Object mCellularReturnType;
    @Shadow private float mCellularJitterModifier;
    @Shadow private Object mDomainWarpType;
    @Shadow private Object mWarpTransformType3D;
    @Shadow private float mDomainWarpAmp;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinFNL that = (MixinFNL) object;
        return Integer.compare(mSeed, that.mSeed) == 0
                && Float.compare(mFrequency, that.mFrequency) == 0
                && Objects.equals(mNoiseType, that.mNoiseType)
                && Objects.equals(mRotationType3D, that.mRotationType3D)
                && Objects.equals(mTransformType3D, that.mTransformType3D)
                && Objects.equals(mFractalType, that.mFractalType)
                && Integer.compare(mOctaves, that.mOctaves) == 0
                && Float.compare(mLacunarity, that.mLacunarity) == 0
                && Float.compare(mGain, that.mGain) == 0
                && Float.compare(mWeightedStrength, that.mWeightedStrength) == 0
                && Float.compare(mPingPongStrength, that.mPingPongStrength) == 0
                && Float.compare(mFractalBounding, that.mFractalBounding) == 0
                && Objects.equals(mCellularDistanceFunction, that.mCellularDistanceFunction)
                && Objects.equals(mCellularReturnType, that.mCellularReturnType)
                && Objects.equals(mCellularJitterModifier, that.mCellularJitterModifier)
                && Objects.equals(mDomainWarpType, that.mDomainWarpType)
                && Objects.equals(mWarpTransformType3D, that.mWarpTransformType3D)
                && Float.compare(mDomainWarpAmp, that.mDomainWarpAmp) == 0;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Integer.hashCode(mSeed);
        result = 31 * result + Float.hashCode(mFrequency);
        result = 31 * result + Objects.hashCode(mNoiseType);
        result = 31 * result + Objects.hashCode(mRotationType3D);
        result = 31 * result + Objects.hashCode(mTransformType3D);
        result = 31 * result + Objects.hashCode(mFractalType);
        result = 31 * result + Integer.hashCode(mOctaves);
        result = 31 * result + Float.hashCode(mLacunarity);
        result = 31 * result + Float.hashCode(mGain);
        result = 31 * result + Float.hashCode(mWeightedStrength);
        result = 31 * result + Float.hashCode(mPingPongStrength);
        result = 31 * result + Float.hashCode(mFractalBounding);
        result = 31 * result + Objects.hashCode(mCellularDistanceFunction);
        result = 31 * result + Objects.hashCode(mCellularReturnType);
        result = 31 * result + Objects.hashCode(mCellularJitterModifier);
        result = 31 * result + Objects.hashCode(mDomainWarpType);
        result = 31 * result + Objects.hashCode(mWarpTransformType3D);
        result = 31 * result + Float.hashCode(mDomainWarpAmp);
        return result;
    }
}
