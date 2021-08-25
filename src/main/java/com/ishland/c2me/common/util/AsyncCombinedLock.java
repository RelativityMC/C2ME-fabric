package com.ishland.c2me.common.util;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ishland.c2me.common.perftracking.IntegerRollingAverage;
import com.ishland.c2me.common.perftracking.PerfTrackingObject;
import com.ishland.c2me.common.perftracking.StatsTrackingExecutor;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class AsyncCombinedLock {

    private static final ForkJoinPool lockWorker0 = new ForkJoinPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() / 7),
            new C2MEForkJoinWorkerThreadFactory("C2ME lock worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );
    public static final StatsTrackingExecutor lockWorker = new StatsTrackingExecutor(lockWorker0);

    private static final AtomicLong totalLocks = new AtomicLong(0L);
    private static final AtomicLong completedLocks = new AtomicLong(0L);
    private static final IntegerRollingAverage average5s = new IntegerRollingAverage(5 * 2);
    private static final IntegerRollingAverage average10s = new IntegerRollingAverage(10 * 2);
    private static final IntegerRollingAverage average1m = new IntegerRollingAverage(60 * 2);
    private static final IntegerRollingAverage average5m = new IntegerRollingAverage(5 * 60 * 2);
    private static final IntegerRollingAverage average15m = new IntegerRollingAverage(15 * 60 * 2);

    static {
        IntegerRollingAverage.SCHEDULER.scheduleAtFixedRate(() -> {
            final long totalTasks = totalLocks.get();
            final long completedTasks = completedLocks.get();
            final int lockSystemLoad = (int) (totalTasks - completedTasks);
            average5s.submit(lockSystemLoad);
            average10s.submit(lockSystemLoad);
            average1m.submit(lockSystemLoad);
            average5m.submit(lockSystemLoad);
            average15m.submit(lockSystemLoad);
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    public static PerfTrackingObject getPerfTrackingObject() {
        return new PerfTrackingObject() {
            @Override
            public double getAverage5s() {
                return average5s.average();
            }

            @Override
            public double getAverage10s() {
                return average10s.average();
            }

            @Override
            public double getAverage1m() {
                return average1m.average();
            }

            @Override
            public double getAverage5m() {
                return average5m.average();
            }

            @Override
            public double getAverage15m() {
                return average15m.average();
            }
        };
    }

    private final AsyncNamedLock<ChunkPos> lock;
    private final ChunkPos[] names;
    private final CompletableFuture<AsyncLock.LockToken> future = new CompletableFuture<>();

    public AsyncCombinedLock(AsyncNamedLock<ChunkPos> lock, Set<ChunkPos> names) {
        this.lock = lock;
        this.names = names.toArray(ChunkPos[]::new);
        totalLocks.incrementAndGet();
        lockWorker.execute(this::tryAcquire);
    }

    private synchronized void tryAcquire() { // TODO optimize logic further
        final LockEntry[] tryLocks = new LockEntry[names.length];
        boolean allAcquired = true;
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            ChunkPos name = names[i];
            final LockEntry entry = new LockEntry(name, this.lock.tryLock(name));
            tryLocks[i] = entry;
            if (entry.lockToken.isEmpty()) {
                allAcquired = false;
                break;
            }
        }
        if (allAcquired) {
            completedLocks.incrementAndGet();
            future.complete(() -> {
                for (LockEntry entry : tryLocks) {
                    //noinspection OptionalGetWithoutIsPresent
                    entry.lockToken.get().releaseLock(); // if it isn't present then something is really wrong
                }
            });
        } else {
            boolean triedRelock = false;
            for (LockEntry entry : tryLocks) {
                if (entry == null) continue;
                entry.lockToken.ifPresent(AsyncLock.LockToken::releaseLock);
                if (!triedRelock && entry.lockToken.isEmpty()) {
                    this.lock.acquireLock(entry.name).thenCompose(lockToken -> {
                        lockToken.releaseLock();
                        return CompletableFuture.runAsync(this::tryAcquire, lockWorker);
                    });
                    triedRelock = true;
                }
            }
            if (!triedRelock) {
                // shouldn't happen at all...
                System.err.println("Some issue occurred while doing locking, retrying");
                this.tryAcquire();
            }
        }
    }

    public CompletableFuture<AsyncLock.LockToken> getFuture() {
        return future.thenApply(Function.identity());
    }

    private record LockEntry(ChunkPos name,
                             Optional<AsyncLock.LockToken> lockToken) {
    }
}
