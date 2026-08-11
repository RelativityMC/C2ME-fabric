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
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.FrontendRegistry;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.SelectNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class SelectBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectBindings.class);

    private static final Class<?> CLASS_SelectDensityFunction;
    private static final MethodHandle MH_input;
    private static final MethodHandle MH_fallback;
    private static final MethodHandle MH_selections;
    private static final MethodHandle MH_range;
    private static final MethodHandle MH_function;
    public static final boolean AVAILABLE;

    static {
        Class<?> class_SelectDensityFunction = null;
        MethodHandle mh_input = null;
        MethodHandle mh_fallback = null;
        MethodHandle mh_selections = null;
        MethodHandle mh_function = null;
        MethodHandle mh_range = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_SelectDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.SelectDensityFunction");
                mh_input = MethodHandles.lookup().findVirtual(class_SelectDensityFunction, "input", MethodType.methodType(DensityFunction.class));
                mh_fallback = MethodHandles.lookup().findVirtual(class_SelectDensityFunction, "fallback", MethodType.methodType(DensityFunction.class));
                mh_selections = MethodHandles.lookup().findVirtual(class_SelectDensityFunction, "selections", MethodType.methodType(List.class));
                Class<?> class_SelectDensityFunction$Selection = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.SelectDensityFunction$Selection");
                mh_range = MethodHandles.lookup().findVirtual(class_SelectDensityFunction$Selection, "range", MethodType.methodType(Range.class));
                mh_function = MethodHandles.lookup().findVirtual(class_SelectDensityFunction$Selection, "function", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.SelectDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.SelectDensityFunction", t);
            }
        }

        CLASS_SelectDensityFunction = class_SelectDensityFunction;
        MH_input = mh_input;
        MH_fallback = mh_fallback;
        MH_selections = mh_selections;
        MH_range = mh_range;
        MH_function = mh_function;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_SelectDensityFunction, function -> {
            try {
                List<?> selections = (List<?>) MH_selections.invoke(function);

                AstNode input = McToAst.toAst((DensityFunction) MH_input.invoke(function));
                AstNode fallback = McToAst.toAst((DensityFunction) MH_fallback.invoke(function));
                AstNode[] functions = new AstNode[selections.size()];
                double[] mins = new double[selections.size()];
                double[] maxs = new double[selections.size()];

                for (int i = 0; i < selections.size(); i++) {
                    Object selection = selections.get(i);
                    Range<Double> range = (Range<Double>) MH_range.invoke(selection);

                    mins[i] = range.minInclusive();
                    maxs[i] = range.maxInclusive();
                    functions[i] = McToAst.toAst((DensityFunction) MH_function.invoke(selection));
                }

                return new SelectNode(input, fallback, mins, maxs, functions);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
