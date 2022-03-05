package com.ishland.c2me.threading.worldgen.common;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.SchedulerThread;
import com.ishland.c2me.base.common.scheduler.SchedulingAsyncCombinedLock;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils.ChunkStatusThreadingType.AS_IS;
import static com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils.ChunkStatusThreadingType.PARALLELIZED;
import static com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils.ChunkStatusThreadingType.SINGLE_THREADED;

public class ChunkStatusUtils {

    public static ChunkStatusThreadingType getThreadingType(final ChunkStatus status) {
        if (status.equals(ChunkStatus.STRUCTURE_STARTS)
                || status.equals(ChunkStatus.STRUCTURE_REFERENCES)
                || status.equals(ChunkStatus.BIOMES)
                || status.equals(ChunkStatus.NOISE)
                || status.equals(ChunkStatus.SURFACE)
                || status.equals(ChunkStatus.CARVERS)
                || status.equals(ChunkStatus.LIQUID_CARVERS)
                || status.equals(ChunkStatus.HEIGHTMAPS)) {
            return PARALLELIZED;
        } else if (status.equals(ChunkStatus.SPAWN)) {
            return SINGLE_THREADED;
        } else if (status.equals(ChunkStatus.FEATURES)) {
            return Config.allowThreadedFeatures ? PARALLELIZED : SINGLE_THREADED;
        }
        return AS_IS;
    }

    public static <T> CompletableFuture<T> runChunkGenWithLock(ChunkPos target, int radius, IntSupplier priority, AsyncNamedLock<ChunkPos> chunkLock, Supplier<CompletableFuture<T>> action) {
        Preconditions.checkNotNull(priority);
//        if (radius == 0)
//            return StageSupport.tryWith(chunkLock.acquireLock(target), unused -> action.get()).toCompletableFuture().thenCompose(Function.identity());

        ArrayList<ChunkPos> fetchedLocks = new ArrayList<>((2 * radius + 1) * (2 * radius + 1));
        for (int x = target.x - radius; x <= target.x + radius; x++)
            for (int z = target.z - radius; z <= target.z + radius; z++)
                fetchedLocks.add(new ChunkPos(x, z));

        final SchedulingAsyncCombinedLock<T> lock = new SchedulingAsyncCombinedLock<>(chunkLock, new HashSet<>(fetchedLocks), priority, SchedulerThread.INSTANCE, action);
        SchedulerThread.INSTANCE.addPendingLock(lock);
        return lock.getFuture();
    }

    public enum ChunkStatusThreadingType {

        PARALLELIZED() {
            @Override
            public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runTask(AsyncLock lock, Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture) {
                return CompletableFuture.supplyAsync(completableFuture, GlobalExecutors.executor).thenCompose(Function.identity());
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
                }, GlobalExecutors.executor);
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
}
