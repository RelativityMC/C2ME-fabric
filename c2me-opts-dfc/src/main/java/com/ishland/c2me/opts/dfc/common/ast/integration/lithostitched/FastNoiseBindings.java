/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
