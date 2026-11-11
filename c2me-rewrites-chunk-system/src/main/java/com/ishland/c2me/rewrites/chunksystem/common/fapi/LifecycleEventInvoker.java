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

package com.ishland.c2me.rewrites.chunksystem.common.fapi;

import com.ishland.c2me.base.mixin.access.fapi.IArrayBackedEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.jspecify.annotations.Nullable;
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

    public static void invokeChunkTicketLevelUpdated(ServerWorld world, long pos, int oldTicketLevel, int newTicketLevel, @Nullable ChunkHolder chunkHolder) {
        // stub for neoforge
    }

}
