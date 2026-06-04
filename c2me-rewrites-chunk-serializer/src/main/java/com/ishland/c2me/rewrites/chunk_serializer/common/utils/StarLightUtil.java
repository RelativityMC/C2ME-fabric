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
