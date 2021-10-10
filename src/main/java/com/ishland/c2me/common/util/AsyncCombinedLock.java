package com.ishland.c2me.common.util;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ishland.c2me.common.GlobalExecutors;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncCombinedLock {

    private final AsyncNamedLock<ChunkPos> lock;
    private final ChunkPos[] names;
    private final CompletableFuture<AsyncLock.LockToken> future = new CompletableFuture<>();

    public AsyncCombinedLock(AsyncNamedLock<ChunkPos> lock, Set<ChunkPos> names) {
        this.lock = lock;
        this.names = names.toArray(ChunkPos[]::new);
        this.tryAcquire();
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
                        return CompletableFuture.runAsync(this::tryAcquire, GlobalExecutors.executor);
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
