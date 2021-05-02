package org.yatopiamc.c2me.common.util;

import com.google.common.collect.Sets;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsyncCombinedLock {

    public static final ForkJoinPool lockWorker = new ForkJoinPool(
            2,
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
            future.complete(new CombinedLockToken(tryLocks));
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class LockEntry {
        public final ChunkPos name;
        public final Optional<AsyncLock.LockToken> lockToken;

        private LockEntry(ChunkPos name, Optional<AsyncLock.LockToken> lockToken) {
            this.name = name;
            this.lockToken = lockToken;
        }
    }

    private static class CombinedLockToken implements AsyncLock.LockToken {

        private final LockEntry[] delegates;

        private CombinedLockToken(LockEntry[] delegates) {
            this.delegates = delegates;
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent") // If it does then something went wrong
        @Override
        public void releaseLock() {
            for (LockEntry lockEntry : delegates) {
                lockEntry.lockToken.get().releaseLock();
            }

        }

        @Override
        public void close() {
            this.releaseLock();
        }
    }
}
