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

package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import com.ibm.asyncutil.util.Combinators;
import com.ishland.c2me.base.mixin.access.IBlender;
import com.ishland.c2me.base.mixin.access.IStorageIoWorker;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class BlendingInfoUtil {

    public static CompletionStage<List<BitSet>> getBlendingInfos(StorageIoWorker worker, ChunkPos pos) {
        final int radius = IBlender.getBLENDING_CHUNK_DISTANCE_THRESHOLD();
        List<CompletableFuture<BitSet>> futures = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
        ChunkPos chunkPos2 = new ChunkPos(pos.x() - radius, pos.z() - radius);
        ChunkPos chunkPos3 = new ChunkPos(pos.x() + radius, pos.z() + radius);
        for(int i = chunkPos2.getRegionX(); i <= chunkPos3.getRegionX(); ++i) {
            for(int j = chunkPos2.getRegionZ(); j <= chunkPos3.getRegionZ(); ++j) {
                final CompletableFuture<BitSet> future = ((IStorageIoWorker) worker).invokeGetOrComputeBlendingStatus(i, j);
                futures.add(future);
            }
        }
        return Combinators.collect(futures, Collectors.toList());
    }

}
