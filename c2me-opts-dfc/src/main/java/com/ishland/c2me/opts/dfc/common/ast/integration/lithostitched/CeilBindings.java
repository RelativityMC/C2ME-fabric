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

import com.ishland.c2me.opts.dfc.common.ast.AstEmitter;
import com.ishland.c2me.opts.dfc.common.ast.FrontendRegistry;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.unary.CeilNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CeilBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(CeilBindings.class);

    private static final Class<?> CLASS_CeilDensityFunction;
    private static final MethodHandle MH_argument;
    public static final boolean AVAILABLE;

    static {
        Class<?> class_CeilDensityFunction = null;
        MethodHandle mh_argument = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_CeilDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.CeilDensityFunction");
                mh_argument = MethodHandles.lookup().findVirtual(class_CeilDensityFunction, "argument", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.CeilDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.CeilDensityFunction", t);
            }
        }

        CLASS_CeilDensityFunction = class_CeilDensityFunction;
        MH_argument = mh_argument;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_CeilDensityFunction, function -> {
            try {
               return new CeilNode(McToAst.toAst((DensityFunction) MH_argument.invoke(function)));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
