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

package com.ishland.c2me.opts.scheduling.mixin.task_scheduling;

import com.ishland.c2me.opts.scheduling.common.DuckChunkHolder;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ServerChunkManager.MainThreadExecutor mainThreadExecutor;

    @Shadow @Final private Set<ChunkHolder> chunksToBroadcastUpdate;

    /**
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @Overwrite
    public void onLightUpdate(LightType type, ChunkSectionPos pos) {
        ChunkHolder chunkHolder = this.getChunkHolder(pos.toChunkPos().toLong()); // thread-safe
        if (chunkHolder != null) {
            ((DuckChunkHolder) chunkHolder).c2me$queueLightSectionDirty(type, pos.getSectionY());
            if (((DuckChunkHolder) chunkHolder).c2me$shouldScheduleUndirty()) {
                this.mainThreadExecutor.execute(() -> {
                    if (((DuckChunkHolder) chunkHolder).c2me$undirtyLight()) {
                        this.chunksToBroadcastUpdate.add(chunkHolder);
                    }
                });
            }
        }
    }

}
