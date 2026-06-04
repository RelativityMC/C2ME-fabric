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

package com.ishland.c2me.rewrites.chunkio.common;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.theinterface.IDirectStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class C2MEStorageVanillaInterface extends StorageIoWorker implements IDirectStorage {

    private final C2MEStorageThread backend;

    public C2MEStorageVanillaInterface(StorageKey arg, Path path, boolean dsync, LongFunction<Executor> backgroundExecutorSupplier) {
        super(arg, path, dsync);
        this.backend = new C2MEStorageThread(arg, path, dsync, backgroundExecutorSupplier);
    }

    @Override
    public CompletableFuture<Void> setResult(ChunkPos pos, @Nullable NbtCompound nbt) {
        return this.backend.setChunkData(pos.toLong(), nbt).thenApply(Function.identity());
    }

    @Override
    public CompletableFuture<Void> setResult(ChunkPos pos, Supplier<NbtCompound> nbtSupplier) {
        return this.backend.setChunkData(pos.toLong(), CompletableFuture.supplyAsync(nbtSupplier, Thread::startVirtualThread)); // nonblocking write
    }

    @Override
    public CompletableFuture<Optional<NbtCompound>> readChunkData(ChunkPos pos) {
        return this.backend.getChunkData(pos.toLong(), null).thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<Void> setRawChunkData(ChunkPos pos, CompletableFuture<byte[]> data) {
        return this.backend.setChunkDataRaw(pos.toLong(), data);
    }

    @Override
    public CompletableFuture<Void> completeAll(boolean sync) {
        return this.backend.flush(true);
    }

    @Override
    public CompletableFuture<Void> scanChunk(ChunkPos pos, NbtScanner scanner) {
        Preconditions.checkNotNull(scanner, "scanner");
        return this.backend.getChunkData(pos.toLong(), scanner).thenApply(unused -> null);
    }

    @Override
    public void close() {
        this.backend.close().join();
    }

    @Override
    public boolean needsBlending(ChunkPos chunkPos, int i) {
        return super.needsBlending(chunkPos, i);
    }


    @Override
    public CompletableFuture<Void> setRawChunkData(ChunkPos pos, byte[] data) {
        this.backend.setChunkData(pos.toLong(), data);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public StorageKey getStorageKey() {
        return this.backend.getStorageKey();
    }
}
