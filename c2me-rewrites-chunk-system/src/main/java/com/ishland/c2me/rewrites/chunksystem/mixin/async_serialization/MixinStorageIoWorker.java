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
