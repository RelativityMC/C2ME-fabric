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
