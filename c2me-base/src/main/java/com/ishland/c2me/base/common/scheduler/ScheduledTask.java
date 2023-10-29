package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;
import com.ishland.flowsched.executor.Task;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ScheduledTask<T> implements Task {

    private final long pos;
    private final Supplier<CompletableFuture<T>> action;
    private final LockToken[] lockTokens;
    private final CompletableFuture<T> future = new CompletableFuture<>();
    private int priority = Integer.MAX_VALUE;

    public ScheduledTask(long pos, Supplier<CompletableFuture<T>> action, LockToken[] lockTokens) {
        this.pos = pos;
        this.action = action;
        this.lockTokens = lockTokens;
    }

    @Override
    public void run() {
        action.get().whenComplete((t, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(t);
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

    @Override
    public int priority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getPos() {
        return this.pos;
    }

    public CompletableFuture<T> getFuture() {
        return this.future;
    }
}
