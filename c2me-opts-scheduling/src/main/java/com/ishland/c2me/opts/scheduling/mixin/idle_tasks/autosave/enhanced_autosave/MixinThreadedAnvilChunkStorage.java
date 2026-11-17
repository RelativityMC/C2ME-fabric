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

package com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.enhanced_autosave;

import com.ishland.c2me.opts.scheduling.common.idle_tasks.IThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinThreadedAnvilChunkStorage implements IThreadedAnvilChunkStorage {

    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;

    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow @Final private ServerWorld world;

    @Shadow protected abstract boolean save(ChunkHolder chunkHolder, long currentTime);

    @Shadow @Final private AtomicInteger chunksBeingSavedCount;
    @Shadow @Final private LongSet chunksToSave;

    @Shadow protected abstract @Nullable ChunkHolder getChunkHolder(long pos);

    @Unique
    private static final int c2me$maxSearchPerCall = 256;

    @Unique
    private static final int c2me$maxConcurrentSaving = 256;

    @Unique
    @Override
    public boolean c2me$runOneChunkAutoSave() {
        if (!this.mainThreadExecutor.isOnThread()) {
            throw new ConcurrentModificationException("runOneChunkAutoSave called async");
        }

        if (this.world.isSavingDisabled()) {
            return false;
        }

        LongIterator iterator = this.chunksToSave.iterator();
        long measuringTimeMs = Util.getMeasuringTimeMs();
        int i = 0;
        while (iterator.hasNext() && (i ++) < c2me$maxSearchPerCall && this.chunksBeingSavedCount.get() < c2me$maxConcurrentSaving) {
            final long pos = iterator.nextLong();
            final ChunkHolder chunkHolder = this.currentChunkHolders.get(pos);
            Chunk latestChunk = chunkHolder != null ? chunkHolder.getLatest() : null;
            if (latestChunk == null || !latestChunk.needsSaving()) {
                iterator.remove();
            } else if (this.save(chunkHolder, measuringTimeMs)) {
                iterator.remove();
                return true;
            }
        }

        return false;
    }
}
