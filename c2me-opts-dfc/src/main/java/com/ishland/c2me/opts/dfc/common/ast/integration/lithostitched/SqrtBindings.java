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
import com.ishland.c2me.opts.dfc.common.ast.FrontendRegistry;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqrtNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SqrtBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqrtBindings.class);

    private static final Class<?> CLASS_SqrtDensityFunction;
    private static final MethodHandle MH_argument;
    public static final boolean AVAILABLE;

    static {
        Class<?> class_SqrtDensityFunction = null;
        MethodHandle mh_argument = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_SqrtDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.SqrtDensityFunction");
                mh_argument = MethodHandles.lookup().findVirtual(class_SqrtDensityFunction, "argument", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.SqrtDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.SqrtDensityFunction", t);
            }
        }

        CLASS_SqrtDensityFunction = class_SqrtDensityFunction;
        MH_argument = mh_argument;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_SqrtDensityFunction, function -> {
            try {
               return new SqrtNode(McToAst.toAst((DensityFunction) MH_argument.invoke(function)));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
