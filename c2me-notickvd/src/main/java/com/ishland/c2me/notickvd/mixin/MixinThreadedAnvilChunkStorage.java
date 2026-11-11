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

package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.notickvd.common.Config;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow public abstract List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge);

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow @Final private PlayerChunkWatchingManager playerChunkWatchingManager;

    @Shadow protected abstract void sendToPlayers(ChunkHolder holder, WorldChunk chunk);

    @ModifyArg(method = "setViewDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"), index = 2)
    private int modifyMaxVD(int max) {
        return Config.maxViewDistance;
    }

    @Redirect(method = "getPostProcessedChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getPostProcessedChunk()Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk redirectSendWatchPacketsGetWorldChunk(ChunkHolder chunkHolder) {
        return chunkHolder.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).orElse(null);
    }

}
