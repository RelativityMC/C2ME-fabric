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

package com.ishland.c2me.opts.natives_math.common.integration.lithostitched;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class FNLUnsafeBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(FNLUnsafeBindings.class);

    private static final MethodHandle MH_fnl;
    private static final MethodHandle MH_mNoiseType;
    private static final MethodHandle MH_mRotationType3D;
    private static final MethodHandle MH_mTransformType3D;
    private static final MethodHandle MH_mFractalType;
    private static final MethodHandle MH_mCellularDistanceFunction;
    private static final MethodHandle MH_mCellularReturnType;
    private static final MethodHandle MH_mDomainWarpType;
    private static final MethodHandle MH_mWarpTransformType3D;

    static {
        MethodHandle mh_fnl = null;
        MethodHandle mh_mNoiseType = null;
        MethodHandle mh_mRotationType3D = null;
        MethodHandle mh_mTransformType3D = null;
        MethodHandle mh_mFractalType = null;
        MethodHandle mh_mCellularDistanceFunction = null;
        MethodHandle mh_mCellularReturnType = null;
        MethodHandle mh_mDomainWarpType = null;
        MethodHandle mh_mWarpTransformType3D = null;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                Class<?> class_FastNoiseConfig = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig");
                Class<?> class_FNL = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL");
                Class<?> class_FNL$NoiseType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$NoiseType");
                Class<?> class_FNL$RotationType3D = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$RotationType3D");
                Class<?> class_FNL$TransformType3D = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$TransformType3D");
                Class<?> class_FNL$FractalType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$FractalType");
                Class<?> class_FNL$CellularDistanceFunction = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$CellularDistanceFunction");
                Class<?> class_FNL$CellularReturnType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$CellularReturnType");
                Class<?> class_FNL$DomainWarpType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$DomainWarpType");
                mh_fnl = MethodHandles.privateLookupIn(class_FastNoiseConfig, MethodHandles.lookup()).findGetter(class_FastNoiseConfig, "fnl", class_FNL);
                mh_mNoiseType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mNoiseType", class_FNL$NoiseType);
                mh_mRotationType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mRotationType3D", class_FNL$RotationType3D);
                mh_mTransformType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mTransformType3D", class_FNL$TransformType3D);
                mh_mFractalType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mFractalType", class_FNL$FractalType);
                mh_mCellularDistanceFunction = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mCellularDistanceFunction", class_FNL$CellularDistanceFunction);
                mh_mCellularReturnType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mCellularReturnType", class_FNL$CellularReturnType);
                mh_mDomainWarpType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mDomainWarpType", class_FNL$DomainWarpType);
                mh_mWarpTransformType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mWarpTransformType3D", class_FNL$TransformType3D);
                LOGGER.info("Bound to FastNoiseLite dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to FastNoiseLite dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL", t);
            }
        }

        MH_fnl = mh_fnl;
        MH_mNoiseType = mh_mNoiseType;
        MH_mRotationType3D = mh_mRotationType3D;
        MH_mTransformType3D = mh_mTransformType3D;
        MH_mFractalType = mh_mFractalType;
        MH_mCellularDistanceFunction = mh_mCellularDistanceFunction;
        MH_mCellularReturnType = mh_mCellularReturnType;
        MH_mDomainWarpType = mh_mDomainWarpType;
        MH_mWarpTransformType3D = mh_mWarpTransformType3D;
    }

    public static Object fnl(Object fnlConfig) {
        try {
            return MH_fnl.invoke(fnlConfig);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static Object field(Object src, String fieldName, boolean config) {
        try {
            Object fnl = config ? MH_fnl.invoke(src) : src;
            return switch (fieldName) {
                case "mNoiseType" -> MH_mNoiseType.invoke(fnl);
                case "mRotationType3D" -> MH_mRotationType3D.invoke(fnl);
                case "mTransformType3D" -> MH_mTransformType3D.invoke(fnl);
                case "mFractalType" -> MH_mFractalType.invoke(fnl);
                case "mCellularDistanceFunction" -> MH_mCellularDistanceFunction.invoke(fnl);
                case "mCellularReturnType" -> MH_mCellularReturnType.invoke(fnl);
                case "mDomainWarpType" -> MH_mDomainWarpType.invoke(fnl);
                case "mWarpTransformType3D" -> MH_mWarpTransformType3D.invoke(fnl);
                default -> throw new IllegalArgumentException("Unknown field " + fieldName);
            };
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
