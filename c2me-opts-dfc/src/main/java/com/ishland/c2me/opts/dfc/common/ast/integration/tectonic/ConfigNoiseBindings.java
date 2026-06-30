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

package com.ishland.c2me.opts.dfc.common.ast.integration.tectonic;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ConfigNoiseBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigNoiseBindings.class);

    private static final Class<?> CLASS_ConfigNoise;
    private static final MethodHandle MH_noise;
    private static final MethodHandle MH_shiftX;
    private static final MethodHandle MH_shiftZ;
    private static final MethodHandle MH_scale;
    private static final MethodHandle MH_multiplier;
    private static final MethodHandle MH_offset;
    private static final MethodHandle MH_smootherScaling;
    private static final boolean AVAILABLE;

    static {
        Class<?> class_ConfigNoise = null;
        MethodHandle mh_noise = null;
        MethodHandle mh_shiftX = null;
        MethodHandle mh_shiftZ = null;
        MethodHandle mh_scale = null;
        MethodHandle mh_multiplier = null;
        MethodHandle mh_offset = null;
        MethodHandle mh_smootherScaling = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("tectonic")) {
            try {
                class_ConfigNoise = Class.forName("dev.worldgen.tectonic.worldgen.densityfunction.ConfigNoise");
                mh_noise = MethodHandles.lookup().findVirtual(class_ConfigNoise, "noise", MethodType.methodType(DensityFunction.Noise.class));
                mh_shiftX = MethodHandles.lookup().findVirtual(class_ConfigNoise, "shiftX", MethodType.methodType(DensityFunction.class));
                mh_shiftZ = MethodHandles.lookup().findVirtual(class_ConfigNoise, "shiftZ", MethodType.methodType(DensityFunction.class));
                mh_scale = MethodHandles.lookup().findVirtual(class_ConfigNoise, "scale", MethodType.methodType(double.class));
                mh_multiplier = MethodHandles.lookup().findVirtual(class_ConfigNoise, "multiplier", MethodType.methodType(double.class));
                mh_offset = MethodHandles.lookup().findVirtual(class_ConfigNoise, "offset", MethodType.methodType(double.class));
                mh_smootherScaling = MethodHandles.lookup().findVirtual(class_ConfigNoise, "smootherScaling", MethodType.methodType(boolean.class));
                available = true;
                LOGGER.info("Bound to tectonic dev.worldgen.tectonic.worldgen.densityfunction.ConfigNoise");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to tectonic dev.worldgen.tectonic.worldgen.densityfunction.ConfigNoise", t);
            }
        }

        CLASS_ConfigNoise = class_ConfigNoise;
        MH_noise = mh_noise;
        MH_shiftX = mh_shiftX;
        MH_shiftZ = mh_shiftZ;
        MH_scale = mh_scale;
        MH_multiplier = mh_multiplier;
        MH_offset = mh_offset;
        MH_smootherScaling = mh_smootherScaling;
        AVAILABLE = available;
    }

    public static AstNode tryParse(DensityFunction function) {
        if (!AVAILABLE) return null;

        if (function.getClass() == CLASS_ConfigNoise) {
            try {
                if ((boolean) MH_smootherScaling.invoke(function)) {
                    return new AddNode(
                            new MulNode(
                                    new GenericShiftedNoiseNode(
                                            new MulNode(new AddNode(CoordinateNode.AXIS_X, McToAst.toAst((DensityFunction) MH_shiftX.invoke(function))), new ConstantNode((double) MH_scale.invoke(function))),
                                            new ConstantNode(0.0),
                                            new MulNode(new AddNode(CoordinateNode.AXIS_Z, McToAst.toAst((DensityFunction) MH_shiftZ.invoke(function))), new ConstantNode((double) MH_scale.invoke(function))),
                                            (DensityFunction.Noise) MH_noise.invoke(function)
                                    ),
                                    new ConstantNode((double) MH_multiplier.invoke(function))
                            ),
                            new ConstantNode((double) MH_offset.invoke(function))
                    );
                } else {
                    return new AddNode(
                            new MulNode(
                                    new GenericShiftedNoiseNode(
                                            new AddNode(new MulNode(CoordinateNode.AXIS_X, new ConstantNode((double) MH_scale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftX.invoke(function))),
                                            new ConstantNode(0.0),
                                            new AddNode(new MulNode(CoordinateNode.AXIS_Z, new ConstantNode((double) MH_scale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftZ.invoke(function))),
                                            (DensityFunction.Noise) MH_noise.invoke(function)
                                    ),
                                    new ConstantNode((double) MH_multiplier.invoke(function))
                            ),
                            new ConstantNode((double) MH_offset.invoke(function))
                    );
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}
