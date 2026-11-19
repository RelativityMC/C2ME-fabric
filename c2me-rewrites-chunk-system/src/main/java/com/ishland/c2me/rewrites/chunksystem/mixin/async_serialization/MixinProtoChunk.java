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

package com.ishland.c2me.rewrites.chunksystem.mixin.async_serialization;

import com.ishland.c2me.base.mixin.access.IBlender;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ProtoChunkExtension;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ProtoChunk.class)
public class MixinProtoChunk implements ProtoChunkExtension {

    @Unique
    private CompletableFuture<Void> blendingComputeFuture = CompletableFuture.completedFuture(null);

    @Unique
    private CompletableFuture<Void> initialMainThreadComputeFuture = CompletableFuture.completedFuture(null);

    @Unique
    private boolean needBlending = false;

    @Override
    public void setBlendingInfo(ChunkPos pos, List<BitSet> bitSets) {
        final int radius = IBlender.getBLENDING_CHUNK_DISTANCE_THRESHOLD();
        final int width = (radius * 2 + 1);
        ChunkPos chunkPos2 = new ChunkPos(pos.x() - radius, pos.z() - radius);
        ChunkPos chunkPos3 = new ChunkPos(pos.x() + radius, pos.z() + radius);

        int index = 0;
        for(int i = chunkPos2.getRegionX(); i <= chunkPos3.getRegionX(); ++i) {
            for(int j = chunkPos2.getRegionZ(); j <= chunkPos3.getRegionZ(); ++j) {
                BitSet bitSet = bitSets.get(index ++);
                if (!bitSet.isEmpty()) {
                    ChunkPos chunkPos4 = ChunkPos.fromRegionMin(i, j);
                    int k = Math.max(chunkPos2.x() - chunkPos4.x(), 0);
                    int l = Math.max(chunkPos2.z() - chunkPos4.z(), 0);
                    int m = Math.min(chunkPos3.x() - chunkPos4.x(), 31);
                    int n = Math.min(chunkPos3.z() - chunkPos4.z(), 31);

                    for(int o = k; o <= m; ++o) {
                        for(int p = l; p <= n; ++p) {
                            int q = p * 32 + o;
                            if (bitSet.get(q)) {
                                this.needBlending = true;
                                return;
                            }
                        }
                    }
                }
            }
        }

        this.needBlending = false;
    }

    @Override
    public void setBlendingComputeFuture(CompletableFuture<Void> future) {
        this.blendingComputeFuture = future;
    }

    @Override
    public boolean getNeedBlending() {
        if (!blendingComputeFuture.isDone()) {
            blendingComputeFuture.join();
        }
        return needBlending;
    }

    @Override
    public void setInitialMainThreadComputeFuture(CompletableFuture<Void> future) {
        this.initialMainThreadComputeFuture = future;
    }

    @Override
    public CompletableFuture<Void> getInitialMainThreadComputeFuture() {
        return this.initialMainThreadComputeFuture;
    }

}
