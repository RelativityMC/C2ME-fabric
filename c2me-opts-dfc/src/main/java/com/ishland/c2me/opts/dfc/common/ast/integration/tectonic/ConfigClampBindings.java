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

package com.ishland.c2me.opts.dfc.common.ast.integration.tectonic;

import com.ishland.c2me.opts.dfc.common.ast.AstEmitter;
import com.ishland.c2me.opts.dfc.common.ast.FrontendRegistry;
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
                LOGGER.warn("Failed to bind to tectonic dev.worldgen.tectonic.worldgen.densityfunction.ConfigClamp", t);
            }
        }

        CLASS_ConfigClamp = class_ConfigClamp;
        MH_input = mh_input;
        MH_min = mh_min;
        MH_max = mh_max;
        AVAILABLE = available;
    }

    public static void register(FrontendRegistry<AstEmitter<? extends DensityFunction>> registry) {
        if (!AVAILABLE) return;

        registry.registerExactMatch((Class<? extends DensityFunction>) CLASS_ConfigClamp, function -> {
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
        });
    }

}
