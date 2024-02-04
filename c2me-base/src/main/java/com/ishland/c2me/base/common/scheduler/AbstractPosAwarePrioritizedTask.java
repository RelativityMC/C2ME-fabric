package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.Task;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Objects;

public abstract class AbstractPosAwarePrioritizedTask implements Task {

    protected final ReferenceArrayList<Runnable> postExec = new ReferenceArrayList<>(4);
    private final long pos;
    private int priority = Integer.MAX_VALUE;

    public AbstractPosAwarePrioritizedTask(long pos) {
        this.pos = pos;
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

    public void addPostExec(Runnable runnable) {
        synchronized (this.postExec) {
            postExec.add(Objects.requireNonNull(runnable));
        }
    }
}
