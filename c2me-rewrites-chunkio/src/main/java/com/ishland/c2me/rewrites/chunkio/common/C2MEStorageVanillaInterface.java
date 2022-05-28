package com.ishland.c2me.rewrites.chunkio.common;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;
import java.util.function.LongFunction;

public class C2MEStorageVanillaInterface extends StorageIoWorker {

    private final C2MEStorageThread backend;

    public C2MEStorageVanillaInterface(Path directory, boolean dsync, String name, LongFunction<IntSupplier> priorityProvider) {
        super(null, dsync, name);
        this.backend = new C2MEStorageThread(directory, dsync, name, priorityProvider);
    }

    @Override
    public CompletableFuture<Void> setResult(ChunkPos pos, @Nullable NbtCompound nbt) {
        this.backend.setChunkData(pos.toLong(), nbt);
        return CompletableFuture.completedFuture(null);
    }

    @Nullable
    @Override
    public NbtCompound getNbt(ChunkPos pos) {
        return this.backend.getChunkData(pos.toLong(), null).join();
    }

    @Override
    public CompletableFuture<NbtCompound> readChunkData(ChunkPos pos) {
        return this.backend.getChunkData(pos.toLong(), null);
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
}
