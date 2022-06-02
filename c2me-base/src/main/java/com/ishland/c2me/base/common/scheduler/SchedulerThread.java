package com.ishland.c2me.base.common.scheduler;

import com.ishland.c2me.base.ModuleEntryPoint;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

public class SchedulerThread extends Thread implements Executor {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Scheduler");

    public static final SchedulerThread INSTANCE = new SchedulerThread();

    private final ConcurrentLinkedQueue<Runnable> rawTasks = new ConcurrentLinkedQueue<>();
    private final PriorityBlockingQueue<SchedulingAsyncCombinedLock<?>> pendingLocks = new PriorityBlockingQueue<>();

    private final Semaphore semaphore = new Semaphore((int) ModuleEntryPoint.globalExecutorParallelism);

    private long lastRebuild = System.currentTimeMillis();
    private int lastPrioritySerial = 0;

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
            int burst = 0;
            while (!pendingLocks.isEmpty()  && burst ++ < 128 && semaphore.tryAcquire()) {
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

            if (!didWork) LockSupport.parkNanos("Waiting for tasks", 10_000_000);
        }
    }

    public void addPendingLock(SchedulingAsyncCombinedLock<?> lock) {
        this.pendingLocks.add(lock);
        LockSupport.unpark(this);
    }

    private final ObjectArrayList<SchedulingAsyncCombinedLock<?>> priorityChangeTmpStorage = new ObjectArrayList<>();

    private boolean doPriorityChanges() {
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > lastRebuild + 500) { // at most twice a second
            lastRebuild = currentTimeMillis;
            final int currentPrioritySerial = PriorityUtils.priorityChangeSerial();
            if (this.lastPrioritySerial != currentPrioritySerial) {
                this.lastPrioritySerial = currentPrioritySerial;
                final long startTime = System.nanoTime();
                // re-add locks to reflect priority changes
                priorityChangeTmpStorage.clear();
                this.pendingLocks.drainTo(priorityChangeTmpStorage);
                this.pendingLocks.addAll(priorityChangeTmpStorage);
                priorityChangeTmpStorage.clear();
//                System.out.printf("Did priority changes for %d entries in %.2fms\n", size, (System.nanoTime() - startTime) / 1_000_000.0);
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.rawTasks.add(command);
        LockSupport.unpark(this);
    }
}
