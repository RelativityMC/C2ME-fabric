package org.yatopiamc.barium.common.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicLong;

public class BariumForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    private final AtomicLong serial = new AtomicLong(0);
    private final String namePattern;
    private final int priority;

    public BariumForkJoinWorkerThreadFactory(String namePattern, int priority) {
        this.namePattern = namePattern;
        this.priority = priority;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final BariumForkJoinWorkerThread bariumForkJoinWorkerThread = new BariumForkJoinWorkerThread(pool);
        bariumForkJoinWorkerThread.setName(String.format(namePattern, serial.incrementAndGet()));
        bariumForkJoinWorkerThread.setPriority(priority);
        bariumForkJoinWorkerThread.setDaemon(true);
        return bariumForkJoinWorkerThread;
    }

    private static class BariumForkJoinWorkerThread extends ForkJoinWorkerThread {

        /**
         * Creates a ForkJoinWorkerThread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @throws NullPointerException if pool is null
         */
        protected BariumForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool);
        }

    }
}
