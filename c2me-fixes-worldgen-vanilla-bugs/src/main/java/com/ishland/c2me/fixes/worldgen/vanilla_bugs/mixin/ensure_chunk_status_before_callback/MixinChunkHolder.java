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

package com.ishland.c2me.fixes.worldgen.vanilla_bugs.mixin.ensure_chunk_status_before_callback;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow public abstract boolean isAccessible();

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getTickingFuture();

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getAccessibleFuture();

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getEntityTickingFuture();

    @WrapWithCondition(method = "method_31412", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;onChunkStatusChange(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/server/world/ChunkLevelType;)V"))
    private boolean ensureChunkStatusBeforeCallback(ServerChunkLoadingManager instance, ChunkPos chunkPos, ChunkLevelType levelType) {
        return switch (levelType) {
            case INACCESSIBLE -> true;
            case FULL -> this.c2me$isStatusReached(this.getAccessibleFuture());
            case BLOCK_TICKING -> this.c2me$isStatusReached(this.getTickingFuture());
            case ENTITY_TICKING -> this.c2me$isStatusReached(this.getEntityTickingFuture());
        };
    }

    @Unique
    private boolean c2me$isStatusReached(CompletableFuture<OptionalChunk<WorldChunk>> future) {
        return future.isDone() && !future.isCompletedExceptionally() && future.join().isPresent();
    }

}
