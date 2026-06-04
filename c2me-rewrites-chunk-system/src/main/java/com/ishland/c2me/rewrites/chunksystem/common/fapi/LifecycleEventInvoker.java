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

package com.ishland.c2me.rewrites.chunksystem.common.fapi;

import com.ishland.c2me.base.mixin.access.fapi.IArrayBackedEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleEventInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleEventInvoker.class);

    public static void invokeChunkLoaded(ServerWorld world, WorldChunk chunk, boolean newChunk) {
        try {
            ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(world, chunk, newChunk);
            if (newChunk) {
                ServerChunkEvents.CHUNK_GENERATE.invoker().onChunkGenerate(world, chunk);
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to invoke chunk load event (world={}, pos={}, newChunk={})", world, chunk.getPos(), newChunk, t);
        }
    }

    public static void invokeChunkUnload(ServerWorld world, WorldChunk chunk) {
        try {
            ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(world, chunk);
        } catch (Throwable t) {
            LOGGER.error("Failed to invoke chunk unload event (world={}, pos={})", world, chunk.getPos(), t);
        }
    }

    private static boolean cachedNeedsInvokeChunkLevelTypeChange = false;

    public static boolean needsInvokeChunkLevelTypeChange() {
        if (cachedNeedsInvokeChunkLevelTypeChange) return true;
        try {
            if (ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE instanceof IArrayBackedEvent<?> accessor0) {
                IArrayBackedEvent<ServerChunkEvents.FullChunkStatusChange> accessor = (IArrayBackedEvent<ServerChunkEvents.FullChunkStatusChange>) accessor0;
                if (accessor.c2me$getHandlers().length > 0) {
                    cachedNeedsInvokeChunkLevelTypeChange = true;
                    return true;
                }
            } else {
                LOGGER.warn("Unexpected Event implementation of ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE: {}", ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.getClass().getName());
                cachedNeedsInvokeChunkLevelTypeChange = true;
                return true;
            }
            return false;
        } catch (Throwable t) {
            LOGGER.error("Failed to check if chunk level type change event is needed", t);
            cachedNeedsInvokeChunkLevelTypeChange = true;
            return true;
        }
    }

    public static void invokeChunkLevelTypeChange(ServerWorld world, WorldChunk chunk, ChunkLevelType oldLevelType, ChunkLevelType newLevelType) {
        try {
            ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.invoker().onFullChunkStatusChange(world, chunk, oldLevelType, newLevelType);
        } catch (Throwable t) {
            LOGGER.error("Failed to invoke chunk level type change event (world={}, pos={}, oldLevelType={}, newLevelType={})", world, chunk.getPos(), oldLevelType, newLevelType, t);
        }
    }

}
