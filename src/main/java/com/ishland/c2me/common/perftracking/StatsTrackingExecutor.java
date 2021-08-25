package com.ishland.c2me.common.perftracking;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StatsTrackingExecutor implements Executor, Closeable, PerfTrackingObject {

    private final Executor delegate;
    private final ScheduledFuture<?> scheduledFuture;

    private final AtomicLong totalTasks = new AtomicLong(0L);
    private final AtomicLong completedTasks = new AtomicLong(0L);
    private final IntegerRollingAverage average5s = new IntegerRollingAverage(5 * 2);
    private final IntegerRollingAverage average10s = new IntegerRollingAverage(10 * 2);
    private final IntegerRollingAverage average1m = new IntegerRollingAverage(60 * 2);
    private final IntegerRollingAverage average5m = new IntegerRollingAverage(5 * 60 * 2);
    private final IntegerRollingAverage average15m = new IntegerRollingAverage(15 * 60 * 2);

    private final AtomicBoolean isOpen = new AtomicBoolean(true);

    public StatsTrackingExecutor(Executor delegate) {
        this.delegate = delegate;
        this.scheduledFuture = IntegerRollingAverage.SCHEDULER.scheduleAtFixedRate(this::submitAverage, 500, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        ensureOpen();
        totalTasks.incrementAndGet();
        delegate.execute(() -> {
            try {
                command.run();
            } finally {
                completedTasks.incrementAndGet();
            }
        });
    }

    @Override
    public double getAverage5s() {
        return average5s.average();
    }

    @Override
    public double getAverage10s() {
        return average10s.average();
    }

    @Override
    public double getAverage1m() {
        return average1m.average();
    }

    @Override
    public double getAverage5m() {
        return average5m.average();
    }

    @Override
    public double getAverage15m() {
        return average15m.average();
    }

    @Override
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            this.scheduledFuture.cancel(false);
            try {
                this.scheduledFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void ensureOpen() {
        if (!isOpen.get()) throw new IllegalArgumentException("Tried to access a closed StatsTrackingExecutor");
    }

    private void submitAverage() {
        final long totalTasks = this.totalTasks.get();
        final long completedTasks = this.completedTasks.get();
        final int executorLoad = (int) (totalTasks - completedTasks);
        average5s.submit(executorLoad);
        average10s.submit(executorLoad);
        average1m.submit(executorLoad);
        average5m.submit(executorLoad);
        average15m.submit(executorLoad);
    }
}
