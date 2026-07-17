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

package com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.FastNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.ducks.IFNLState;
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
    public static final Class<?> CLASS_FastNoiseConfig;
    private static final MethodHandle MH_config;
    private static final MethodHandle MH_xzScale;
    private static final MethodHandle MH_yScale;
    private static final MethodHandle MH_shiftX;
    private static final MethodHandle MH_shiftY;
    private static final MethodHandle MH_shiftZ;

    public static final boolean AVAILABLE;

    static {
        Class<?> class_FastNoiseDensityFunction = null;
        Class<?> class_FastNoiseConfig = null;
        MethodHandle mh_config = null;
        MethodHandle mh_xzScale = null;
        MethodHandle mh_yScale = null;
        MethodHandle mh_shiftX = null;
        MethodHandle mh_shiftY = null;
        MethodHandle mh_shiftZ = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_FastNoiseDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
                class_FastNoiseConfig = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig");
                mh_config = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "config", MethodType.methodType(RegistryEntry.class));
                mh_xzScale = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "xzScale", MethodType.methodType(double.class));
                mh_yScale = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "yScale", MethodType.methodType(double.class));
                mh_shiftX = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftX", MethodType.methodType(DensityFunction.class));
                mh_shiftY = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftY", MethodType.methodType(DensityFunction.class));
                mh_shiftZ = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftZ", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
            }
        }

        CLASS_FastNoiseDensityFunction = class_FastNoiseDensityFunction;
        CLASS_FastNoiseConfig = class_FastNoiseConfig;
        MH_config = mh_config;
        MH_xzScale = mh_xzScale;
        MH_yScale = mh_yScale;
        MH_shiftX = mh_shiftX;
        MH_shiftY = mh_shiftY;
        MH_shiftZ = mh_shiftZ;
        AVAILABLE = available;
    }

    public static AstNode tryParse(DensityFunction function) {
        if (!AVAILABLE) return null;

        if (function.getClass() == CLASS_FastNoiseDensityFunction) {
            try {
                return new FastNoiseNode(
                        new AddNode(new MulNode(CoordinateNode.AXIS_X, new ConstantNode((double) MH_xzScale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftX.invoke(function))),
                        new AddNode(new MulNode(CoordinateNode.AXIS_Y, new ConstantNode((double) MH_yScale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftY.invoke(function))),
                        new AddNode(new MulNode(CoordinateNode.AXIS_Z, new ConstantNode((double) MH_xzScale.invoke(function))), McToAst.toAst((DensityFunction) MH_shiftZ.invoke(function))),
                        ((RegistryEntry<?>) MH_config.invoke(function)).value()
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
