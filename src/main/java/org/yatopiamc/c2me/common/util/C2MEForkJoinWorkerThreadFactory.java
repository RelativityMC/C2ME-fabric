package org.yatopiamc.c2me.common.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicLong;

public class C2MEForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    private final AtomicLong serial = new AtomicLong(0);
    private final String namePattern;
    private final int priority;

    public C2MEForkJoinWorkerThreadFactory(String namePattern, int priority) {
        this.namePattern = namePattern;
        this.priority = priority;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final C2MEForkJoinWorkerThread C2MEForkJoinWorkerThread = new C2MEForkJoinWorkerThread(pool);
        C2MEForkJoinWorkerThread.setName(String.format(namePattern, serial.incrementAndGet()));
        C2MEForkJoinWorkerThread.setPriority(priority);
        C2MEForkJoinWorkerThread.setDaemon(true);
        return C2MEForkJoinWorkerThread;
    }

    private static class C2MEForkJoinWorkerThread extends ForkJoinWorkerThread {

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
