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

package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProtoChunkExtension {

    void setBlendingComputeFuture(CompletableFuture<Void> future);

    void setBlendingInfo(ChunkPos pos, List<BitSet> bitSet);

    boolean getNeedBlending();

    void setInitialMainThreadComputeFuture(CompletableFuture<Void> future);
    CompletableFuture<Void> getInitialMainThreadComputeFuture();

}
