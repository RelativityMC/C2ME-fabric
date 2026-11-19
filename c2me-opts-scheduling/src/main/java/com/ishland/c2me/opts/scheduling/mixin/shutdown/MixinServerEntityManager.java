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

package com.ishland.c2me.opts.scheduling.mixin.shutdown;

import com.ishland.c2me.opts.scheduling.common.ITryFlushable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityTrackingStatus;
import net.minecraft.world.storage.ChunkDataAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(ServerEntityManager.class)
public abstract class MixinServerEntityManager<T> implements ITryFlushable {

    @Shadow protected abstract LongSet getLoadedChunks();

    @Shadow @Final private ChunkDataAccess<T> dataAccess;

    @Shadow protected abstract void loadChunks();

    @Shadow @Final private Long2ObjectMap<EntityTrackingStatus> trackingStatuses;

    @Shadow protected abstract boolean unload(long chunkPos);

    @Shadow protected abstract boolean trySave(long chunkPos, Consumer<T> action);

    public boolean c2me$tryFlush() {
        LongSet longSet = this.getLoadedChunks();

        if(!longSet.isEmpty()) {
            this.dataAccess.awaitAll(false);
            this.loadChunks();
            longSet.removeIf((pos) -> {
                boolean bl = this.trackingStatuses.get(pos) == EntityTrackingStatus.HIDDEN;
                return bl ? this.unload(pos) : this.trySave(pos, (entity) -> {
                });
            });
        }

        this.dataAccess.awaitAll(true);
        return longSet.isEmpty();
    }

}
