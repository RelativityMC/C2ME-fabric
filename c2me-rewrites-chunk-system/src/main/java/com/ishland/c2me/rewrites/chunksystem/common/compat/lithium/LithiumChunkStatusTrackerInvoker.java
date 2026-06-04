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

package com.ishland.c2me.rewrites.chunksystem.common.compat.lithium;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class LithiumChunkStatusTrackerInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LithiumChunkStatusTrackerInvoker.class);
    private static final MethodHandle MH_onChunkInaccessible;

    static {
        MethodHandle mh_onChunkInaccessible = null;

        Class<?> clazzChunkStatusTracker;
        try {
            clazzChunkStatusTracker = Class.forName("net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker");
        } catch (ClassNotFoundException e) {
            if (FabricLoader.getInstance().isModLoaded("lithium")) {
                LOGGER.warn("Couldn't find net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker, yet fabric claims lithium is there. Curious");
            }
            clazzChunkStatusTracker = null;
        }
        if (clazzChunkStatusTracker != null) {
            Method methodOnChunkInaccessible;
            try {
                methodOnChunkInaccessible = clazzChunkStatusTracker.getMethod("onChunkInaccessible", ServerWorld.class, ChunkPos.class);
            } catch (NoSuchMethodException e) {
                LOGGER.warn("Couldn't find net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker#onChunkInaccessible(ServerWorld, ChunkPos), yet fabric claims lithium is there. Curious");
                methodOnChunkInaccessible = null;
            }
            if (methodOnChunkInaccessible != null) {
                try {
                    mh_onChunkInaccessible = MethodHandles.lookup().unreflect(methodOnChunkInaccessible);
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Couldn't access net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker#onChunkInaccessible(ServerWorld, ChunkPos), yet fabric claims lithium is there. Curious", e);
                }
            }
        }
        MH_onChunkInaccessible = mh_onChunkInaccessible;
    }

    public static void invokeOnChunkInaccessible(ServerWorld world, ChunkPos pos) {
        if (MH_onChunkInaccessible != null) {
            try {
                MH_onChunkInaccessible.invokeExact(world, pos);
            } catch (Throwable e) {
                LOGGER.error("net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker#onChunkInaccessible(ServerWorld, ChunkPos) failed", e);
            }
        }
    }

}
