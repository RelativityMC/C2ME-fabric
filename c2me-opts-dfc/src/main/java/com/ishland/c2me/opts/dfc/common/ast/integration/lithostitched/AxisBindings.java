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
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class AxisBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(AxisBindings.class);

    private static final Class<?> CLASS_AxisDensityFunction;
    private static final MethodHandle MH_axis;
    private static final boolean AVAILABLE;

    static {
        Class<?> class_AxisDensityFunction = null;
        MethodHandle mh_axis = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_AxisDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.AxisDensityFunction");
                mh_axis = MethodHandles.lookup().findVirtual(class_AxisDensityFunction, "axis", MethodType.methodType(Direction.Axis.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.AxisDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.AxisDensityFunction", t);
            }
        }

        CLASS_AxisDensityFunction = class_AxisDensityFunction;
        MH_axis = mh_axis;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_AxisDensityFunction, function -> {
            try {
               return switch ((Direction.Axis) MH_axis.invoke(function)) {
                   case X -> CoordinateNode.AXIS_X;
                   case Y -> CoordinateNode.AXIS_Y;
                   case Z -> CoordinateNode.AXIS_Z;
               };
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
