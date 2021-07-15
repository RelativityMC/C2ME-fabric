package com.ishland.c2me.common.util;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public class AsyncCombinedLock {

    public static final ForkJoinPool lockWorker = new ForkJoinPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() / 7),
            new C2MEForkJoinWorkerThreadFactory("C2ME lock worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

    private final AsyncNamedLock<ChunkPos> lock;
    private final ChunkPos[] names;
    private final CompletableFuture<AsyncLock.LockToken> future = new CompletableFuture<>();

    public AsyncCombinedLock(AsyncNamedLock<ChunkPos> lock, Set<ChunkPos> names) {
        this.lock = lock;
        this.names = names.toArray(ChunkPos[]::new);
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
                lockWorker.execute(this::tryAcquire);
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
