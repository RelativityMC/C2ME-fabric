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

package com.ishland.c2me.base.common.integration.lithostitched;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FNLBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(FNLBindings.class);

    public static final Class<?> CLASS_FastNoiseConfig;
    private static final MethodHandle MH_sample;
    private static final MethodHandle MH_fnl;
    private static final MethodHandle MH_GetNoise;
    private static final MethodHandle MH_mSeed;
    private static final MethodHandle MH_mFrequency;
    private static final MethodHandle MH_mNoiseType;
    private static final MethodHandle MH_mRotationType3D;
    private static final MethodHandle MH_mTransformType3D;
    private static final MethodHandle MH_mFractalType;
    private static final MethodHandle MH_mOctaves;
    private static final MethodHandle MH_mLacunarity;
    private static final MethodHandle MH_mGain;
    private static final MethodHandle MH_mWeightedStrength;
    private static final MethodHandle MH_mPingPongStrength;
    private static final MethodHandle MH_mCellularDistanceFunction;
    private static final MethodHandle MH_mCellularReturnType;
    private static final MethodHandle MH_mCellularJitterModifier;
    private static final MethodHandle MH_mDomainWarpType;
    private static final MethodHandle MH_mDomainWarpAmp;
    private static final MethodHandle MH_mWarpTransformType3D;
    private static final boolean AVAILABLE;

    static {
        Class<?> class_FastNoiseConfig = null;
        MethodHandle mh_sample = null;
        MethodHandle mh_fnl = null;
        MethodHandle mh_GetNoise = null;
        MethodHandle mh_mSeed = null;
        MethodHandle mh_mFrequency = null;
        MethodHandle mh_mNoiseType = null;
        MethodHandle mh_mRotationType3D = null;
        MethodHandle mh_mTransformType3D = null;
        MethodHandle mh_mFractalType = null;
        MethodHandle mh_mOctaves = null;
        MethodHandle mh_mLacunarity = null;
        MethodHandle mh_mGain = null;
        MethodHandle mh_mWeightedStrength = null;
        MethodHandle mh_mPingPongStrength = null;
        MethodHandle mh_mCellularDistanceFunction = null;
        MethodHandle mh_mCellularReturnType = null;
        MethodHandle mh_mCellularJitterModifier = null;
        MethodHandle mh_mDomainWarpType = null;
        MethodHandle mh_mDomainWarpAmp = null;
        MethodHandle mh_mWarpTransformType3D = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_FastNoiseConfig = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig");
                Class<?> class_FNL = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL");
                Class<?> class_FNL$NoiseType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$NoiseType");
                Class<?> class_FNL$RotationType3D = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$RotationType3D");
                Class<?> class_FNL$TransformType3D = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$TransformType3D");
                Class<?> class_FNL$FractalType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$FractalType");
                Class<?> class_FNL$CellularDistanceFunction = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$CellularDistanceFunction");
                Class<?> class_FNL$CellularReturnType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$CellularReturnType");
                Class<?> class_FNL$DomainWarpType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$DomainWarpType");
                mh_sample = MethodHandles.lookup().findVirtual(class_FastNoiseConfig, "sample", MethodType.methodType(double.class, double.class, double.class, double.class));
                mh_fnl = MethodHandles.privateLookupIn(class_FastNoiseConfig, MethodHandles.lookup()).findGetter(class_FastNoiseConfig, "fnl", class_FNL);
                mh_GetNoise = MethodHandles.lookup().findVirtual(class_FNL, "GetNoise", MethodType.methodType(float.class, double.class, double.class, double.class));
                mh_mSeed = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mSeed", int.class);
                mh_mFrequency = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mFrequency", float.class);
                mh_mNoiseType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mNoiseType", class_FNL$NoiseType);
                mh_mRotationType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mRotationType3D", class_FNL$RotationType3D);
                mh_mTransformType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mTransformType3D", class_FNL$TransformType3D);
                mh_mFractalType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mFractalType", class_FNL$FractalType);
                mh_mOctaves = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mOctaves", int.class);
                mh_mLacunarity = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mLacunarity", float.class);
                mh_mGain = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mGain", float.class);
                mh_mWeightedStrength = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mWeightedStrength", float.class);
                mh_mPingPongStrength = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mPingPongStrength", float.class);
                mh_mCellularDistanceFunction = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mCellularDistanceFunction", class_FNL$CellularDistanceFunction);
                mh_mCellularReturnType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mCellularReturnType", class_FNL$CellularReturnType);
                mh_mCellularJitterModifier = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mCellularJitterModifier", float.class);
                mh_mDomainWarpType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mDomainWarpType", class_FNL$DomainWarpType);
                mh_mDomainWarpAmp = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mDomainWarpAmp", float.class);
                mh_mWarpTransformType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mWarpTransformType3D", class_FNL$TransformType3D);
                available = true;
                LOGGER.info("Bound to FastNoiseLite dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to FastNoiseLite dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL", t);
            }
        }

        CLASS_FastNoiseConfig = class_FastNoiseConfig;
        MH_sample = mh_sample;
        MH_fnl = mh_fnl;
        MH_GetNoise = mh_GetNoise;
        MH_mSeed = mh_mSeed;
        MH_mFrequency = mh_mFrequency;
        MH_mNoiseType = mh_mNoiseType;
        MH_mRotationType3D = mh_mRotationType3D;
        MH_mTransformType3D = mh_mTransformType3D;
        MH_mFractalType = mh_mFractalType;
        MH_mOctaves = mh_mOctaves;
        MH_mLacunarity = mh_mLacunarity;
        MH_mGain = mh_mGain;
        MH_mWeightedStrength = mh_mWeightedStrength;
        MH_mPingPongStrength = mh_mPingPongStrength;
        MH_mCellularDistanceFunction = mh_mCellularDistanceFunction;
        MH_mCellularReturnType = mh_mCellularReturnType;
        MH_mCellularJitterModifier = mh_mCellularJitterModifier;
        MH_mDomainWarpType = mh_mDomainWarpType;
        MH_mDomainWarpAmp = mh_mDomainWarpAmp;
        MH_mWarpTransformType3D = mh_mWarpTransformType3D;
        AVAILABLE = available;
    }

    public static double call_FastNoiseConfig$sample(Object config, double x, double y, double z) {
        try {
            return (double) MH_sample.invoke(config, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static float call_FNL$GetNoise(Object config, double x, double y, double z) {
        try {
            Object fnl = MH_fnl.invoke(config);
            return (float) MH_GetNoise.invoke(fnl, x, y, z);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static FNLState tryParseState(Object config) {
        if (!AVAILABLE) return null;

        try {
            Object fnl = MH_fnl.invoke(config);
            return new FNLState(
                    (int) MH_mSeed.invoke(fnl),
                    (float) MH_mFrequency.invoke(fnl),
                    ((Enum<?>) MH_mNoiseType.invoke(fnl)).ordinal(),
                    ((Enum<?>) MH_mRotationType3D.invoke(fnl)).ordinal(),
                    ((Enum<?>) MH_mFractalType.invoke(fnl)).ordinal(),
                    (int) MH_mOctaves.invoke(fnl),
                    (float) MH_mLacunarity.invoke(fnl),
                    (float) MH_mGain.invoke(fnl),
                    (float) MH_mWeightedStrength.invoke(fnl),
                    (float) MH_mPingPongStrength.invoke(fnl),
                    ((Enum<?>) MH_mCellularDistanceFunction.invoke(fnl)).ordinal(),
                    ((Enum<?>) MH_mCellularReturnType.invoke(fnl)).ordinal(),
                    (float) MH_mCellularJitterModifier.invoke(fnl),
                    ((Enum<?>) MH_mDomainWarpType.invoke(fnl)).ordinal(),
                    (float) MH_mDomainWarpAmp.invoke(fnl)
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static record FNLState(int seed, float frequency, int noise_type, int rotation_type_3d, int fractal_type, int octaves,
                                  float lacunarity, float gain, float weighted_strength,
                                  float ping_pong_strength, int cellular_distance_func, int cellular_return_type,
                                  float cellular_jitter_mod, int domain_warp_type, float domain_warp_amp) {
    }
}
