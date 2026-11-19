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
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ShiftBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShiftBindings.class);

    private static final Class<?> CLASS_ShiftDensityFunction;
    private static final MethodHandle MH_input;
    private static final MethodHandle MH_shiftX;
    private static final MethodHandle MH_shiftY;
    private static final MethodHandle MH_shiftZ;
    public static final boolean AVAILABLE;

    static {
        Class<?> class_ShiftDensityFunction = null;
        MethodHandle mh_input = null;
        MethodHandle mh_shiftX = null;
        MethodHandle mh_shiftY = null;
        MethodHandle mh_shiftZ = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_ShiftDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.ShiftDensityFunction");
                mh_input = MethodHandles.lookup().findVirtual(class_ShiftDensityFunction, "input", MethodType.methodType(DensityFunction.class));
                mh_shiftX = MethodHandles.lookup().findVirtual(class_ShiftDensityFunction, "shiftX", MethodType.methodType(DensityFunction.class));
                mh_shiftY = MethodHandles.lookup().findVirtual(class_ShiftDensityFunction, "shiftY", MethodType.methodType(DensityFunction.class));
                mh_shiftZ = MethodHandles.lookup().findVirtual(class_ShiftDensityFunction, "shiftZ", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.ShiftDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.ShiftDensityFunction", t);
            }
        }

        CLASS_ShiftDensityFunction = class_ShiftDensityFunction;
        MH_input = mh_input;
        MH_shiftX = mh_shiftX;
        MH_shiftY = mh_shiftY;
        MH_shiftZ = mh_shiftZ;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_ShiftDensityFunction, function -> {
            try {
               return new RepositionNode(
                       McToAst.toAst((DensityFunction) MH_input.invoke(function)),
                       new AddNode(CoordinateNode.AXIS_X, McToAst.toAst((DensityFunction) MH_shiftX.invoke(function))),
                       new AddNode(CoordinateNode.AXIS_Y, McToAst.toAst((DensityFunction) MH_shiftY.invoke(function))),
                       new AddNode(CoordinateNode.AXIS_Z, McToAst.toAst((DensityFunction) MH_shiftZ.invoke(function)))
               );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
