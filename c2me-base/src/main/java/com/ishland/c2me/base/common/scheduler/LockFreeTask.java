package com.ishland.c2me.base.common.scheduler;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.GlobalExecutors;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class LockFreeTask<T> implements ScheduledTask {

    private final SchedulingManager schedulingManager;
    private final long center;
    private final Supplier<CompletableFuture<T>> action;
    private final boolean async;
    private final CompletableFuture<T> future = new CompletableFuture<>();

    public LockFreeTask(SchedulingManager schedulingManager, long center, Supplier<CompletableFuture<T>> action, boolean async) {
        this.schedulingManager = schedulingManager;
        this.center = center;
        this.action = action;
        this.async = async;

        this.schedulingManager.enqueue(this);
    }

    @Override
    public boolean tryPrepare() {
        return true;
    }

    @Override
    public void runTask(Runnable postAction) {
        Preconditions.checkNotNull(postAction);
        final CompletableFuture<T> future = this.action.get();
        Preconditions.checkNotNull(future, "future");
        future.handleAsync((result, throwable) -> {
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
        return this.center;
    }

    @Override
    public boolean isAsync() {
        return this.async;
    }

    public CompletableFuture<T> getFuture() {
        return this.future;
    }
}
