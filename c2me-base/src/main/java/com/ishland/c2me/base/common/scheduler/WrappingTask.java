package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;

import java.util.Objects;

public class WrappingTask extends AbstractPosAwarePrioritizedTask {

    private static final LockToken[] EMPTY_LOCK_TOKENS = new LockToken[0];

    private final Runnable wrapped;

    public WrappingTask(long pos, Runnable wrapped) {
        super(pos);
        this.wrapped = Objects.requireNonNull(wrapped);
    }

    @Override
    public void run(Runnable releaseLocks) {
        try {
            wrapped.run();
        } finally {
            releaseLocks.run();
            for (Runnable runnable : this.postExec) {
                try {
                    runnable.run();
                } catch (Throwable t1) {
                    t1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void propagateException(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public LockToken[] lockTokens() {
        return EMPTY_LOCK_TOKENS;
    }
}
