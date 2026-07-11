package com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.FastNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FastNoiseBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastNoiseBindings.class);

    private static final Class<?> CLASS_FastNoiseDensityFunction;
    private static final MethodHandle MH_config;
    private static final MethodHandle MH_xzScale;
    private static final MethodHandle MH_yScale;
    private static final MethodHandle MH_shiftX;
    private static final MethodHandle MH_shiftY;
    private static final MethodHandle MH_shiftZ;
    private static final MethodHandle MH_fnl;
    private static final MethodHandle MH_mSeed;
    private static final MethodHandle MH_mFrequency;
    private static final MethodHandle MH_mNoiseType;
    private static final MethodHandle MH_mRotationType3D;
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

    public static final boolean AVAILABLE;

    static {
        Class<?> class_FastNoiseDensityFunction = null;
        MethodHandle mh_config = null;
        MethodHandle mh_xzScale = null;
        MethodHandle mh_yScale = null;
        MethodHandle mh_shiftX = null;
        MethodHandle mh_shiftY = null;
        MethodHandle mh_shiftZ = null;
        MethodHandle mh_fnl = null;
        MethodHandle mh_mSeed = null;
        MethodHandle mh_mFrequency = null;
        MethodHandle mh_mNoiseType = null;
        MethodHandle mh_mRotationType3D = null;
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
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_FastNoiseDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
                Class<?> class_FastNoiseConfig = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig");
                Class<?> class_FNL = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL");
                Class<?> class_FNL$NoiseType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$NoiseType");
                Class<?> class_FNL$RotationType3D = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$RotationType3D");
                Class<?> class_FNL$FractalType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$FractalType");
                Class<?> class_FNL$CellularDistanceFunction = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$CellularDistanceFunction");
                Class<?> class_FNL$CellularReturnType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$CellularReturnType");
                Class<?> class_FNL$DomainWarpType = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FNL$DomainWarpType");
                mh_config = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "config", MethodType.methodType(RegistryEntry.class));
                mh_xzScale = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "xzScale", MethodType.methodType(double.class));
                mh_yScale = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "yScale", MethodType.methodType(double.class));
                mh_shiftX = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftX", MethodType.methodType(DensityFunction.class));
                mh_shiftY = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftY", MethodType.methodType(DensityFunction.class));
                mh_shiftZ = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftZ", MethodType.methodType(DensityFunction.class));
                mh_fnl = MethodHandles.privateLookupIn(class_FastNoiseConfig, MethodHandles.lookup()).findGetter(class_FastNoiseConfig, "fnl", class_FNL);
                mh_mSeed = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mSeed", int.class);
                mh_mFrequency = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mFrequency", float.class);
                mh_mNoiseType = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mNoiseType", class_FNL$NoiseType);
                mh_mRotationType3D = MethodHandles.privateLookupIn(class_FNL, MethodHandles.lookup()).findGetter(class_FNL, "mRotationType3D", class_FNL$RotationType3D);
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
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
            }
        }

        CLASS_FastNoiseDensityFunction = class_FastNoiseDensityFunction;
        MH_config = mh_config;
        MH_xzScale = mh_xzScale;
        MH_yScale = mh_yScale;
        MH_shiftX = mh_shiftX;
        MH_shiftY = mh_shiftY;
        MH_shiftZ = mh_shiftZ;
        MH_fnl = mh_fnl;
        MH_mSeed = mh_mSeed;
        MH_mFrequency = mh_mFrequency;
        MH_mNoiseType = mh_mNoiseType;
        MH_mRotationType3D = mh_mRotationType3D;
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
        AVAILABLE = available;
    }

    public static AstNode tryParse(DensityFunction function) {
        if (!AVAILABLE) return null;

        if (function.getClass() == CLASS_FastNoiseDensityFunction) {
            try {
                Object fnl = MH_fnl.invoke(((RegistryEntry<?>) MH_config.invoke(function)).value());
                return new FastNoiseNode(
                        new AddNode(new MulNode(CoordinateNode.AXIS_X, new ConstantNode((double) MH_xzScale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftX.invoke(function))),
                        new AddNode(new MulNode(CoordinateNode.AXIS_Y, new ConstantNode((double) MH_yScale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftY.invoke(function))),
                        new AddNode(new MulNode(CoordinateNode.AXIS_Z, new ConstantNode((double) MH_xzScale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftZ.invoke(function))),
                        new BindingsTemplate.FNLState(
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
                        )
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
