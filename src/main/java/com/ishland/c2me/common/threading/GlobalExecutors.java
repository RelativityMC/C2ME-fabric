package com.ishland.c2me.common.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalExecutors {

    public static final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
            1,
            new ThreadFactoryBuilder().setNameFormat("C2ME scheduler").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).setThreadFactory(r -> {
                final Thread thread = new Thread(r);
                GlobalExecutors.schedulerThread.set(thread);
                return thread;
            }).build()
    );
    private static final AtomicReference<Thread> schedulerThread = new AtomicReference<>();

    public static void ensureSchedulerThread() {
        if (Thread.currentThread() != schedulerThread.get())
            throw new IllegalStateException("Not on scheduler thread");
    }

}
