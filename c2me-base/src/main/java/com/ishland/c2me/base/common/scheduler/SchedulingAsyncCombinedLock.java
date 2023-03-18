package com.ishland.c2me.base.common.scheduler;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ishland.c2me.base.common.GlobalExecutors;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SchedulingAsyncCombinedLock<T> implements ScheduledTask {

    private final AsyncNamedLock<ChunkPos> lock;
    private final long center;
    private final ChunkPos[] names;
    private final BooleanSupplier isCancelled;
    private final Consumer<SchedulingAsyncCombinedLock<T>> readdForExecution;
    private final Supplier<CompletableFuture<T>> action;
    private final String desc;
    private final CompletableFuture<T> future = new CompletableFuture<>();
    private AsyncLock.LockToken acquiredToken;

    public SchedulingAsyncCombinedLock(AsyncNamedLock<ChunkPos> lock, long center, Set<ChunkPos> names, BooleanSupplier isCancelled, Consumer<SchedulingAsyncCombinedLock<T>> readdForExecution, Supplier<CompletableFuture<T>> action, String desc) {
        this.lock = lock;
        this.center = center;
        this.names = names.toArray(ChunkPos[]::new);
        this.isCancelled = isCancelled;
        this.readdForExecution = readdForExecution;
        this.action = action;
        this.desc = desc;

        this.readdForExecution.accept(this);
    }

    @Override
    public boolean trySchedule() {
        return tryAcquire();
    }

    synchronized boolean tryAcquire() {
//        if (this.isCancelled.getAsBoolean()) {
////            System.out.println(String.format("Cancelling tasks for %s", this.desc));
//            this.future.completeExceptionally(new CancellationException());
//            return false;
//        }

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
            this.acquiredToken = () -> {
                for (LockEntry entry : tryLocks) {
                    //noinspection OptionalGetWithoutIsPresent
                    entry.lockToken.get().releaseLock(); // if it isn't present then something is really wrong
                }
            };
            return true;
        } else {
            boolean triedRelock = false;
            for (LockEntry entry : tryLocks) {
                if (entry == null) continue;
                entry.lockToken.ifPresent(AsyncLock.LockToken::releaseLock);
                if (!triedRelock && entry.lockToken.isEmpty()) {
                    this.lock.acquireLock(entry.name).thenAccept(lockToken -> {
                        lockToken.releaseLock();
                        this.readdForExecution.accept(this);
                    });
                    triedRelock = true;
                }
            }
            if (!triedRelock) {
                // shouldn't happen at all...
                System.err.println("Some issue occurred while doing locking, retrying");
                return this.tryAcquire();
            }
            return false;
        }
    }

    @Override
    public void addPostAction(Runnable postAction) {
        Preconditions.checkNotNull(postAction);
        AsyncLock.LockToken token = this.acquiredToken;
        if (token == null) throw new IllegalStateException();
        final CompletableFuture<T> future = this.action.get();
        Preconditions.checkNotNull(future, "future");
        future.handleAsync((result, throwable) -> {
            try {
                token.releaseLock();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                postAction.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (throwable != null) this.future.completeExceptionally(throwable);
            else this.future.complete(result);
            return null;
        }, GlobalExecutors.invokingExecutor);
    }

    @Override
    public long centerPos() {
        return center;
    }

    public CompletableFuture<T> getFuture() {
        return this.future.thenApply(Function.identity());
    }

    private record LockEntry(ChunkPos name,
                             Optional<AsyncLock.LockToken> lockToken) {
    }
}
