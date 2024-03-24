package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;
import com.ishland.flowsched.executor.Task;

import java.util.Objects;

public class SimplePrioritizedTask implements Task {

    private final Runnable task;
    private final LockToken[] lockTokens;
    private final int priority;

    public SimplePrioritizedTask(Runnable task, LockToken[] lockTokens, int priority) {
        this.task = Objects.requireNonNull(task, "task");
        this.lockTokens = Objects.requireNonNull(lockTokens, "lockTokens");
        this.priority = priority;
    }

    @Override
    public void run(Runnable releaseLocks) {
        try {
            this.task.run();
        } finally {
            releaseLocks.run();
        }
    }

    @Override
    public void propagateException(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public LockToken[] lockTokens() {
        return this.lockTokens;
    }

    @Override
    public int priority() {
        return this.priority;
    }
}
