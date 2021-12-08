package com.ishland.c2me.common.util;

import java.util.concurrent.CompletableFuture;

public record PriorityCompletableFuture<V>(CompletableFuture<V> future, int priority,
                                           CompletableFuture<V> result) implements PriorityInterface, Runnable {

    @Override
    public void run() {
        try {
            if (!result.isDone()) result.complete(future.get());
        } catch (Throwable t) {
            result.completeExceptionally(t);
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }
}