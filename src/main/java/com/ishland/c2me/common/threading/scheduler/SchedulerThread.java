package com.ishland.c2me.common.threading.scheduler;

import com.ishland.c2me.common.config.C2MEConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class SchedulerThread extends Thread implements Executor {

    public static final SchedulerThread INSTANCE = new SchedulerThread();

    private final ConcurrentLinkedQueue<Runnable> rawTasks = new ConcurrentLinkedQueue<>();
    private final PriorityBlockingQueue<SchedulingAsyncCombinedLock<?>> pendingLocks = new PriorityBlockingQueue<>();

    private final Semaphore semaphore = new Semaphore(C2MEConfig.globalExecutorParallelism);

    private long lastRebuild = System.currentTimeMillis();
    private final AtomicBoolean hasPriorityChanges = new AtomicBoolean(false);

    private SchedulerThread() {
        this.setName("C2ME scheduler");
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            boolean didWork = false;

            if (doPriorityChanges()) didWork = true;

            // try locks
            while (!pendingLocks.isEmpty() && semaphore.tryAcquire()) {
                SchedulingAsyncCombinedLock<?> lock = pendingLocks.poll();
                if (lock != null && lock.tryAcquire()) {
                    lock.doAction(semaphore::release); // locked
                    didWork = true;
                } else {
                    semaphore.release(); // not locked
                }
            }

            // run tasks
            if (!didWork) {
                Runnable runnable = rawTasks.poll();
                if (runnable != null) {
                    didWork = true;
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }

            if (!didWork) LockSupport.parkNanos("Waiting for tasks", 100_000);
        }
    }

    public void addPendingLock(SchedulingAsyncCombinedLock<?> lock) {
        this.pendingLocks.add(lock);
    }

    private boolean doPriorityChanges() {
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > lastRebuild + 500) { // at most twice a second
            lastRebuild = currentTimeMillis;
            if (this.hasPriorityChanges.compareAndSet(true, false)) {
                final long startTime = System.nanoTime();
                final int size = this.pendingLocks.size();
                ArrayList<SchedulingAsyncCombinedLock<?>> tmp = new ArrayList<>(size);
                // re-add locks to reflect priority changes
                this.pendingLocks.drainTo(tmp);
                this.pendingLocks.addAll(tmp);
                System.out.printf("Did priority changes for %d entries in %.2fms\n", size, (System.nanoTime() - startTime) / 1_000_000.0);
                return true;
            }
        }
        return false;
    }

    public void notifyPriorityChange() {
        this.hasPriorityChanges.set(true);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.rawTasks.add(command);
    }
}
