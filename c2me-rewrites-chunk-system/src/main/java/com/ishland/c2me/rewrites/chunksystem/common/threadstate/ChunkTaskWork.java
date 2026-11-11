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

package com.ishland.c2me.rewrites.chunksystem.common.threadstate;

import com.ishland.c2me.base.common.threadstate.RunningWork;
import com.ishland.c2me.base.common.util.TimeUtil;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public record ChunkTaskWork(ServerWorld world, ChunkPos chunkPos, NewChunkStatus status, boolean isUpgrade, long startTime) implements RunningWork {

    public ChunkTaskWork(ChunkLoadingContext context, NewChunkStatus status, boolean isUpgrade) {
        this(
                ((IThreadedAnvilChunkStorage) context.tacs()).getWorld(),
                context.holder().getKey(),
                status,
                isUpgrade,
                System.nanoTime()
        );
    }

    @Override
    public String toString() {
        if (isUpgrade) {
            return String.format(
                    "Upgrading chunk %s to %s in world %s (%s elapsed)",
                    chunkPos,
                    status,
                    world.getRegistryKey().getValue(),
                    TimeUtil.formatElapsedTime(System.nanoTime() - startTime)
            );
        } else {
            return String.format(
                    "Downgrading chunk %s from %s in world %s (%s elapsed)",
                    chunkPos,
                    status,
                    world.getRegistryKey().getValue(),
                    TimeUtil.formatElapsedTime(System.nanoTime() - startTime)
            );
        }
    }
}
