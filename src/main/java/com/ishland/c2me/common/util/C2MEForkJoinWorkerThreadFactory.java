package com.ishland.c2me.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicLong;

public class C2MEForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    private final AtomicLong serial = new AtomicLong(0);
    private final String groupName;
    private final String namePattern;
    private final int priority;

    private final ExecutorService threadCreator;
    private final ThreadGroup threadGroup;

    public C2MEForkJoinWorkerThreadFactory(String groupName, String namePattern, int priority) {
        this.groupName = groupName;
        this.namePattern = namePattern;
        this.priority = priority;

        this.threadGroup = new ThreadGroup(this.groupName);
        this.threadCreator = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(String.format("%s daemon", this.groupName))
                        .setPriority(Thread.NORM_PRIORITY - 1)
                        .setDaemon(true)
                        .setThreadFactory(r -> new Thread(this.threadGroup, r))
                        .build()
        );
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return CFUtil.join(CompletableFuture.supplyAsync(() -> {
            final C2MEForkJoinWorkerThread newThread = new C2MEForkJoinWorkerThread(pool);
            newThread.setName(String.format(namePattern, serial.incrementAndGet()));
            newThread.setPriority(priority);
            newThread.setDaemon(true);
            return newThread;
        }, threadCreator));
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public static class C2MEForkJoinWorkerThread extends ForkJoinWorkerThread {

        /**
         * Creates a ForkJoinWorkerThread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @throws NullPointerException if pool is null
         */
        protected C2MEForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool);
        }

    }
}
