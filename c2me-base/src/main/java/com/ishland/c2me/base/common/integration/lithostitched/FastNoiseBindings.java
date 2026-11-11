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

package com.ishland.c2me.base.common.integration.lithostitched;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FastNoiseBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastNoiseBindings.class);

    public static final Class<?> CLASS_FastNoiseDensityFunction;
    public static final Class<?> CLASS_FastNoiseConfig;
    public static final MethodHandle MH_config;
    public static final MethodHandle MH_xzScale;
    public static final MethodHandle MH_yScale;
    public static final MethodHandle MH_shiftX;
    public static final MethodHandle MH_shiftY;
    public static final MethodHandle MH_shiftZ;

    public static final boolean AVAILABLE;

    static {
        Class<?> class_FastNoiseDensityFunction = null;
        Class<?> class_FastNoiseConfig = null;
        MethodHandle mh_config = null;
        MethodHandle mh_xzScale = null;
        MethodHandle mh_yScale = null;
        MethodHandle mh_shiftX = null;
        MethodHandle mh_shiftY = null;
        MethodHandle mh_shiftZ = null;
        boolean available = false;

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            try {
                class_FastNoiseDensityFunction = Class.forName("dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
                class_FastNoiseConfig = Class.forName("dev.worldgen.lithostitched.api.worldgen.densityfunction.fastnoise.FastNoiseConfig");
                mh_config = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "config", MethodType.methodType(RegistryEntry.class));
                mh_xzScale = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "xzScale", MethodType.methodType(double.class));
                mh_yScale = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "yScale", MethodType.methodType(double.class));
                mh_shiftX = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftX", MethodType.methodType(DensityFunction.class));
                mh_shiftY = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftY", MethodType.methodType(DensityFunction.class));
                mh_shiftZ = MethodHandles.lookup().findVirtual(class_FastNoiseDensityFunction, "shiftZ", MethodType.methodType(DensityFunction.class));
                available = true;
                LOGGER.info("Bound to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction");
            } catch (Throwable t) {
                LOGGER.warn("Failed to bind to lithostitched dev.worldgen.lithostitched.impl.worldgen.densityfunction.FastNoiseDensityFunction", t);
            }
        }

        CLASS_FastNoiseDensityFunction = class_FastNoiseDensityFunction;
        CLASS_FastNoiseConfig = class_FastNoiseConfig;
        MH_config = mh_config;
        MH_xzScale = mh_xzScale;
        MH_yScale = mh_yScale;
        MH_shiftX = mh_shiftX;
        MH_shiftY = mh_shiftY;
        MH_shiftZ = mh_shiftZ;
        AVAILABLE = available;
    }

}
