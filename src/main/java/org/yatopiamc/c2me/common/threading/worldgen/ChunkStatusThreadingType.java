package org.yatopiamc.c2me.common.threading.worldgen;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.Chunk;
import org.threadly.concurrent.TaskPriority;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public enum ChunkStatusThreadingType {

    PARALLELIZED() {
        @Override
        public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture) {
            return PriorityCompletableFuture.supplyAsync(completableFuture, WorldGenThreadingExecutorUtils.mainExecutor, TaskPriority.Low).thenComposeAsync(Function.identity());
        }
    },
    SINGLE_THREADED() {
        @Override
        public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture) {
            Preconditions.checkNotNull(lock);
            return lock.acquireLock().thenComposeAsync(lockToken -> {
                try {
                    return PriorityCompletableFuture.supplyAsync(completableFuture, WorldGenThreadingExecutorUtils.mainExecutor, TaskPriority.High).thenComposeAsync(Function.identity());
                } finally {
                    lockToken.releaseLock();
                }
            }, GlobalExecutors.scheduler).toCompletableFuture();
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
