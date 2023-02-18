package com.ishland.c2me.base.common.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public abstract class ScheduledTask<T extends ScheduledTask<T>> implements Comparable<T> {

    final IntSupplier priority;

    protected ScheduledTask(IntSupplier priority) {
        this.priority = priority;
    }

    abstract boolean trySchedule();

    abstract void addPostAction(Runnable postAction);

    @Override
    public int compareTo(@NotNull T o) {
        return Integer.compare(this.priority.getAsInt(), o.priority.getAsInt());
    }
}
