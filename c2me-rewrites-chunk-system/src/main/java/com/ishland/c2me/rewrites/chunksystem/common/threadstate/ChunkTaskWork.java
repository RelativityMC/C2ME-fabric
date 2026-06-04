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
