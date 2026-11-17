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

import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ChunkIoThreadingExecutorUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.scanner.SelectiveNbtCollector;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker {

    @Shadow public abstract CompletableFuture<Optional<NbtCompound>> readChunkData(ChunkPos pos);

    @Shadow protected abstract boolean needsBlending(NbtCompound nbt);

    @Shadow public abstract CompletableFuture<Void> scanChunk(ChunkPos pos, NbtScanner scanner);

    @Shadow @Final private static Logger LOGGER;
    private ExecutorService threadExecutor;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/PrioritizedConsecutiveExecutor;<init>(ILjava/util/concurrent/Executor;Ljava/lang/String;)V"))
    private Executor redirectIoWorkerExecutor(Executor executor) {
        return threadExecutor = Executors.newSingleThreadExecutor(ChunkIoThreadingExecutorUtils.ioWorkerFactory);
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/PrioritizedConsecutiveExecutor;close()V", shift = At.Shift.AFTER))
    private void onClose(CallbackInfo ci) {
        threadExecutor.shutdown();
        while (!threadExecutor.isTerminated()) {
            LockSupport.parkNanos("Waiting for thread executor termination", 100_000);
        }
    }

    /**
     * @author ishland
     * @reason use async instead of flooding worker thread
     */
    @Overwrite
    private CompletableFuture<BitSet> computeBlendingStatus(int chunkX, int chunkZ) {
        ChunkPos chunkPos = ChunkPos.fromRegionMin(chunkX, chunkZ);
        ChunkPos chunkPos2 = ChunkPos.fromRegionMax(chunkX, chunkZ);
        BitSet bitSet = new BitSet();
        final CompletableFuture[] futures = ChunkPos.stream(chunkPos, chunkPos2)
                .map(chunkPosx -> {
                    SelectiveNbtCollector selectiveNbtCollector = new SelectiveNbtCollector(new NbtScanQuery(NbtInt.TYPE, "DataVersion"), new NbtScanQuery(NbtCompound.TYPE, "blending_data"));

                    return this.scanChunk(chunkPosx, selectiveNbtCollector)
                            .thenRun(() -> {
                                NbtElement nbtElement = selectiveNbtCollector.getRoot();
                                if (nbtElement instanceof NbtCompound nbtCompound) {
                                    if (this.needsBlending(nbtCompound)) {
                                        int i = chunkPosx.getRegionRelativeZ() * 32 + chunkPosx.getRegionRelativeX();
                                        synchronized (bitSet) {
                                            bitSet.set(i);
                                        }
                                    }
                                }
                            })
                            .exceptionally(throwable -> {
                                LOGGER.warn("Failed to scan chunk {}", chunkPosx, throwable);
                                return null;
                            });
                }).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures)
                .thenApply(unused -> bitSet);
    }
}
