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

import com.ishland.c2me.base.common.integration.lithostitched.FNLBindings;
import com.ishland.c2me.opts.dfc.common.ast.AstEmitter;
import com.ishland.c2me.opts.dfc.common.ast.FrontendRegistry;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.GenericFastNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public class FastNoiseBindings {
    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.CLASS_FastNoiseDensityFunction, function -> {
            try {
                Object config = ((RegistryEntry<?>) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_config.invoke(function)).value();
                FNLBindings.FNLState state = FNLBindings.tryParseState(config);
                if (state == null) return null; // soft fallback to DelegateNode if FNLBinding unavailable
                return new GenericFastNoiseNode(
                        new AddNode(new MulNode(CoordinateNode.AXIS_X, new ConstantNode((double) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_xzScale.invoke(function))), McToAst.toAst((DensityFunction) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_shiftX.invoke(function))),
                        new AddNode(new MulNode(CoordinateNode.AXIS_Y, new ConstantNode((double) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_yScale.invoke(function))), McToAst.toAst((DensityFunction) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_shiftY.invoke(function))),
                        new AddNode(new MulNode(CoordinateNode.AXIS_Z, new ConstantNode((double) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_xzScale.invoke(function))), McToAst.toAst((DensityFunction) com.ishland.c2me.base.common.integration.lithostitched.FastNoiseBindings.MH_shiftZ.invoke(function))),
                        state, config
                );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
