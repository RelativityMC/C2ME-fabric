package org.yatopiamc.c2me.common.threading.worldgen;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public enum ChunkStatusThreadingType {

    PARALLELIZED() {
        @Override
        public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture) {
            return CompletableFuture.supplyAsync(completableFuture, WorldGenThreadingExecutorUtils.mainExecutor).thenCompose(Function.identity());
        }
    },
    SINGLE_THREADED() {
        @Override
        public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture) {
            Preconditions.checkNotNull(lock);
            return lock.acquireLock().toCompletableFuture().thenComposeAsync(lockToken -> {
                try {
                    return completableFuture.get();
                } finally {
                    lockToken.releaseLock();
                }
            }, WorldGenThreadingExecutorUtils.mainExecutor);
        }
    },
    AS_IS() {
        @Override
        public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture) {
            return completableFuture.get();
        }
    };

    public abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture);

}
