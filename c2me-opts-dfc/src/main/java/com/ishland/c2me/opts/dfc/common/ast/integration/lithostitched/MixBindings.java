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

import com.ishland.c2me.opts.dfc.common.ast.AstEmitter;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.FrontendRegistry;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MixBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixBindings.class);

    private static final Class<?> CLASS_MixDensityFunction;
    private static final MethodHandle MH_input;
    private static final MethodHandle MH_argument1;
    private static final MethodHandle MH_argument2;
    public static final boolean AVAILABLE;

    static {
        Class<?> class_MixDensityFunction = null;
        MethodHandle mh_input = null;
        MethodHandle mh_argument1 = null;
        MethodHandle mh_argument2 = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_MixDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.MixDensityFunction");
                mh_input = MethodHandles.lookup().findVirtual(class_MixDensityFunction, "input", MethodType.methodType(DensityFunction.class));
                mh_argument1 = MethodHandles.lookup().findVirtual(class_MixDensityFunction, "argument1", MethodType.methodType(DensityFunction.class));
                mh_argument2 = MethodHandles.lookup().findVirtual(class_MixDensityFunction, "argument2", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.MixDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.MixDensityFunction", t);
            }
        }

        CLASS_MixDensityFunction = class_MixDensityFunction;
        MH_input = mh_input;
        MH_argument1 = mh_argument1;
        MH_argument2 = mh_argument2;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_MixDensityFunction, function -> {
            try {
               return new MixNode(
                       McToAst.toAst((DensityFunction) MH_input.invoke(function)),
                       McToAst.toAst((DensityFunction) MH_argument1.invoke(function)),
                       McToAst.toAst((DensityFunction) MH_argument2.invoke(function))
               );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
