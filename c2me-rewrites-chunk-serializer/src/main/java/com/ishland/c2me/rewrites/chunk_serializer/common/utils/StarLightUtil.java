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

package com.ishland.c2me.rewrites.chunk_serializer.common.utils;

import com.ishland.c2me.rewrites.chunk_serializer.mixin.IStarlightSaveState;
import net.minecraft.world.chunk.Chunk;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public class StarLightUtil {
    static final MethodHandle getBlockNibbles;
    static final MethodHandle getSkyNibbles;
    static final MethodHandle getSaveState;

    static {
        try {
            var lookup = MethodHandles.lookup();

            Class<?> ExtendedChunkInterface = lookup.findClass("ca.spottedleaf.starlight.common.chunk.ExtendedChunk");
            Class<?> SWMRNibbleArrayClass = lookup.findClass("ca.spottedleaf.starlight.common.light.SWMRNibbleArray");
            Class<?> SWMRNibbleArrayClassArray = SWMRNibbleArrayClass.arrayType();
            Class<?> SaveStateClass = lookup.findClass("ca.spottedleaf.starlight.common.light.SWMRNibbleArray$SaveState");

            MethodType nibbleArrayGetter = MethodType.methodType(SWMRNibbleArrayClassArray);

            getBlockNibbles = lookup.findVirtual(ExtendedChunkInterface, "getBlockNibbles", nibbleArrayGetter);
            getSkyNibbles = lookup.findVirtual(ExtendedChunkInterface, "getSkyNibbles", nibbleArrayGetter);

            MethodType saveStateGetter = MethodType.methodType(SaveStateClass);

            getSaveState = lookup.findVirtual(SWMRNibbleArrayClass, "getSaveState", saveStateGetter);

        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("StarLightUtil failed to resolve starlight classes", e);
        }
    }

    public static Object[] getBlockNibbles(Chunk chunk) {
        try {
            return (Object[]) getBlockNibbles.invoke(chunk);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Object[] getSkyNibbles(Chunk chunk) {
        try {
            return (Object[]) getSkyNibbles.invoke(chunk);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static IStarlightSaveState getSaveState(Object nibbleArray) {
        try {
            return (IStarlightSaveState) getSaveState.invoke(nibbleArray);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
