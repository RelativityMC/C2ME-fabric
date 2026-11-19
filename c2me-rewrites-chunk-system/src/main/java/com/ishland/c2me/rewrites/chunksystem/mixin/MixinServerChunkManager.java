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

package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.IChunkSystemAccess;
import com.ishland.c2me.rewrites.chunksystem.common.structs.ChunkSystemExecutors;
import com.ishland.flowsched.structs.SimpleObjectPool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    @Shadow @Final private Thread serverThread;

    @Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

    @Unique
    private long c2me$lastHolderUpdate = System.nanoTime();

    @Shadow
    protected abstract @Nullable ChunkHolder getChunkHolder(long pos);

    @Shadow public abstract int getLoadedChunkCount();

    private final SimpleObjectPool<ArrayDeque<Runnable>> c2me$mainThreadConsolidatorPool = new SimpleObjectPool<>(
            pool -> new ArrayDeque<>(),
            ArrayDeque::clear,
            ArrayDeque::clear,
            4
    );

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"), cancellable = true)
    private void shortcutGetChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) {
            final ChunkHolder holder = this.getChunkHolder(ChunkPos.toLong(x, z));
            if (holder != null) {
                final CompletableFuture<OptionalChunk<Chunk>> future = holder.load(leastStatus, this.chunkLoadingManager); // thread-safe in new system
                Chunk chunk = future.getNow(ChunkHolder.UNLOADED).orElse(null);
                if (chunk instanceof WrapperProtoChunk readOnlyChunk) chunk = readOnlyChunk.getWrappedChunk();
                if (chunk != null) {
                    cir.setReturnValue(chunk); // also cancels
                    return;
                }
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;updateChunks()Z", shift = At.Shift.AFTER))
    private void updateHolderMapAfterTick(CallbackInfo ci) {
        ((IThreadedAnvilChunkStorage) this.chunkLoadingManager).invokeUpdateHolderMap();
    }

    @WrapOperation(method = "updateChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;updateHolderMap()Z"))
    private boolean disableUpdateHolderMapOnTask(ServerChunkLoadingManager instance, Operation<Boolean> original) { // holder map only used for compatibility layer
        if (System.nanoTime() - c2me$lastHolderUpdate > 50_000_000L) { // 50ms
            c2me$lastHolderUpdate = System.nanoTime();
            return original.call(instance);
        }
        return false;
    }

    @Redirect(method = "isMissingForLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getLevel()I"))
    private int replaceLevel(ChunkHolder instance) {
        return ((IChunkSystemAccess) this.chunkLoadingManager).c2me$getTheChunkSystem().vanillaIf$getManagedLevel(instance.getPos().toLong());
    }

    /**
     * @author ishland
     * @reason add debug string
     */
    @Overwrite
    public String getDebugString() {
        return Integer.toString(((IChunkSystemAccess) this.chunkLoadingManager).c2me$getTheChunkSystem().itemCount()) + ", " + Integer.toString(this.getLoadedChunkCount());
    }

    @WrapOperation(method = "updateChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkLevelManager;update(Lnet/minecraft/server/world/ServerChunkLoadingManager;)Z"))
    private boolean consolidateSchedules(ChunkLevelManager instance, ServerChunkLoadingManager chunkLoadingManager, Operation<Boolean> original) {
        if (Thread.currentThread() != this.serverThread) {
            ConcurrentModificationException e = new ConcurrentModificationException("Async update chunks");
            e.printStackTrace();
            throw e;
        }

        ArrayDeque<Runnable> runnables;
        return ScopedValue
                .where(ChunkSystemExecutors.CONSOLIDATING_QUEUE, runnables = this.c2me$mainThreadConsolidatorPool.alloc())
                .call(() -> {
                    try {
                        return original.call(instance, chunkLoadingManager);
                    } finally {
                        if (!runnables.isEmpty()) {
                            ChunkSystemExecutors.consolidatingRoot0(runnables);
                            this.c2me$mainThreadConsolidatorPool.release(new ArrayDeque<>());
                        } else {
                            this.c2me$mainThreadConsolidatorPool.release(runnables);
                        }
                    }
                });
    }

}
