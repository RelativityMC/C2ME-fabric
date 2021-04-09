package org.yatopiamc.c2me.common.util;

import com.ibm.asyncutil.locks.AsyncLock;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;

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

    private final Set<AsyncLock> lockHandles;
    private final CompletableFuture<AsyncLock.LockToken> future = new CompletableFuture<>();

    public AsyncCombinedLock(Set<AsyncLock> lockHandles) {
        this.lockHandles = Set.copyOf(lockHandles);
        lockWorker.execute(this::tryAcquire);
    }

    private synchronized void tryAcquire() { // TODO optimize logic
        final Set<LockEntry> tryLocks = new ObjectOpenHashSet<>(lockHandles.size());
        boolean allAcquired = true;
        for (AsyncLock lockHandle : lockHandles) {
            final LockEntry entry = new LockEntry(lockHandle, lockHandle.tryLock());
            tryLocks.add(entry);
            if (entry.lockToken.isEmpty()) {
                allAcquired = false;
                break;
            }
        }
        if (allAcquired) {
            future.complete(new CombinedLockToken(tryLocks.stream().flatMap(lockEntry -> lockEntry.lockToken.stream()).collect(Collectors.toUnmodifiableSet())));
        } else {
            tryLocks.stream().flatMap(lockEntry -> lockEntry.lockToken.stream()).forEach(AsyncLock.LockToken::releaseLock);
            tryLocks.stream().unordered().filter(lockEntry -> lockEntry.lockToken.isEmpty()).findFirst().ifPresentOrElse(lockEntry ->
                    lockEntry.lock.acquireLock().thenCompose(lockToken -> {
                        lockToken.releaseLock();
                        return CompletableFuture.runAsync(this::tryAcquire, lockWorker);
                    }), this::tryAcquire);
        }
    }

    public CompletableFuture<AsyncLock.LockToken> getFuture() {
        return future.thenApply(Function.identity());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class LockEntry {
        public final AsyncLock lock;
        public final Optional<AsyncLock.LockToken> lockToken;

        private LockEntry(AsyncLock lock, Optional<AsyncLock.LockToken> lockToken) {
            this.lock = lock;
            this.lockToken = lockToken;
        }
    }

    private static class CombinedLockToken implements AsyncLock.LockToken {

        private final Set<AsyncLock.LockToken> delegates;

        private CombinedLockToken(Set<AsyncLock.LockToken> delegates) {
            this.delegates = Set.copyOf(delegates);
        }

        @Override
        public void releaseLock() {
            delegates.forEach(AsyncLock.LockToken::releaseLock);
        }

        @Override
        public void close() {
            this.releaseLock();
        }
    }
}
