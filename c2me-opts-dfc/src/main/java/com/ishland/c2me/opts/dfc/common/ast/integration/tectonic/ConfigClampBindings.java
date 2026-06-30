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
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ConfigClampBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigClampBindings.class);

    private static final Class<?> CLASS_ConfigClamp;
    private static final MethodHandle MH_input;
    private static final MethodHandle MH_min;
    private static final MethodHandle MH_max;
    private static final boolean AVAILABLE;

    static {
        Class<?> class_ConfigClamp = null;
        MethodHandle mh_input = null;
        MethodHandle mh_min = null;
        MethodHandle mh_max = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("tectonic")) {
            try {
                class_ConfigClamp = Class.forName("dev.worldgen.tectonic.worldgen.densityfunction.ConfigClamp");
                mh_input = MethodHandles.lookup().findVirtual(class_ConfigClamp, "input", MethodType.methodType(DensityFunction.class));
                mh_min = MethodHandles.lookup().findVirtual(class_ConfigClamp, "min", MethodType.methodType(DensityFunction.class));
                mh_max = MethodHandles.lookup().findVirtual(class_ConfigClamp, "max", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to tectonic dev.worldgen.tectonic.worldgen.densityfunction.ConfigClamp");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to tectonic dev.worldgen.tectonic.worldgen.densityfunction.ConfigClamp");
            }
        }

        CLASS_ConfigClamp = class_ConfigClamp;
        MH_input = mh_input;
        MH_min = mh_min;
        MH_max = mh_max;
        AVAILABLE = available;
    }

    public static AstNode tryParse(DensityFunction function) {
        if (!AVAILABLE) return null;

        if (function.getClass() == CLASS_ConfigClamp) {
            try {
                return new MinNode(
                        new MaxNode(
                                McToAst.toAst((DensityFunction) MH_input.invoke(function)),
                                McToAst.toAst((DensityFunction) MH_min.invoke(function))
                        ),
                        McToAst.toAst((DensityFunction) MH_max.invoke(function))
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}
