package com.ishland.c2me.base.common.scheduler;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeighborLockingUtils {

    public static final BooleanSupplier FALSE_SUPPLIER = () -> false;

    public static <T> CompletableFuture<T> runChunkGenWithLock(ChunkPos target, ChunkStatus status, ChunkHolder holder, int radius, SchedulingManager schedulingManager, boolean async, Supplier<CompletableFuture<T>> action) {
//        Preconditions.checkNotNull(status);
//        if (radius == 0)
//            return StageSupport.tryWith(chunkLock.acquireLock(target), unused -> action.get()).toCompletableFuture().thenCompose(Function.identity());

        if (status == ChunkStatus.LIGHT) {
            return new LockFreeTask<>(
                    schedulingManager,
                    target.toLong(),
                    action,
                    async
            ).getFuture();
        }

        BooleanSupplier isCancelled;

        if (holder != null && status != null) {
            isCancelled = () -> isCancelled(holder, status);
        } else {
            isCancelled = FALSE_SUPPLIER;
        }

//        ArrayList<ChunkPos> fetchedLocks = new ArrayList<>((2 * radius + 1) * (2 * radius + 1));
//        for (int x = target.x - radius; x <= target.x + radius; x++)
//            for (int z = target.z - radius; z <= target.z + radius; z++)
//                fetchedLocks.add(new ChunkPos(x, z));
//
//        final SchedulingAsyncCombinedLock<T> task = new SchedulingAsyncCombinedLock<>(
//                chunkLock,
//                target.toLong(),
//                new HashSet<>(fetchedLocks),
//                isCancelled,
//                schedulingManager::enqueue,
//                action,
//                target.toString(),
//                async);

        LongArrayList lockTargets = new LongArrayList(Math.max((2 * radius + 1) * (2 * radius + 1), 0));
        for (int x = target.x - radius; x <= target.x + radius; x++)
            for (int z = target.z - radius; z <= target.z + radius; z++)
                lockTargets.add(ChunkPos.toLong(x, z));

        final NeighborLockingTask<T> task = new NeighborLockingTask<>(
                schedulingManager,
                target.toLong(),
                lockTargets.toLongArray(),
                isCancelled,
                action,
                "%s %s".formatted(target.toString(), status),
                async
        );
        return task.getFuture();
    }

    public static boolean isCancelled(ChunkHolder holder, ChunkStatus targetStatus) {
        return ChunkLevels.getStatus(holder.getLevel()).getIndex() < targetStatus.getIndex();
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
