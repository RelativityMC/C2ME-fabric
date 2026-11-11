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

package com.ishland.c2me.base.common.threadstate;

import com.ishland.c2me.base.common.util.TimeUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

public record SyncLoadWork(ServerWorld world, ChunkPos chunkPos, ChunkStatus targetStatus, boolean create, long startTime) implements RunningWork {

    public SyncLoadWork(ServerWorld world, ChunkPos chunkPos, ChunkStatus targetStatus, boolean create) {
        this(world, chunkPos, targetStatus, create, System.nanoTime());
    }

    @Override
    public String toString() {
        return String.format("Sync load chunk %s to status %s in world %s (create=%s) (%s elapsed)",
                chunkPos, targetStatus, world.getRegistryKey().getValue(), create, TimeUtil.formatElapsedTime(System.nanoTime() - startTime));
    }

}
