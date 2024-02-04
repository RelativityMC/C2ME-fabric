package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ScheduledTask<T> extends AbstractPosAwarePrioritizedTask {

    private final Supplier<CompletableFuture<T>> action;
    private final LockToken[] lockTokens;
    private final CompletableFuture<T> future = new CompletableFuture<>();

    public ScheduledTask(long pos, Supplier<CompletableFuture<T>> action, LockToken[] lockTokens) {
        super(pos);
        this.action = action;
        this.lockTokens = lockTokens;
    }

    @Override
    public void run(Runnable releaseLocks) {
        action.get().whenComplete((t, throwable) -> {
            releaseLocks.run();
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(t);
            }
            for (Runnable runnable : this.postExec) {
                try {
                    runnable.run();
                } catch (Throwable t1) {
                    t1.printStackTrace();
                }
            }
        });
    }

    @Override
    public void propagateException(Throwable t) {
        future.completeExceptionally(t);
    }

    @Override
    public LockToken[] lockTokens() {
        return this.lockTokens;
    }

    public CompletableFuture<T> getFuture() {
        return this.future;
    }
}
