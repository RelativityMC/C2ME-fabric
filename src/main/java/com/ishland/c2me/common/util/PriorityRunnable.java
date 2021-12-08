package com.ishland.c2me.common.util;

import java.util.concurrent.CompletableFuture;

public class PriorityRunnable<V> implements PriorityInterface, Runnable {

    private final Runnable runnable;
    private final int priority;

    public PriorityRunnable(Runnable runnable, int priority) {
        this.runnable = runnable;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
