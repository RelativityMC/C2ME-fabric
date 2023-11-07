package com.ishland.c2me.base.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.base.ModuleEntryPoint;
import com.ishland.flowsched.executor.ExecutorManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalExecutors {

//    private static final C2MEForkJoinWorkerThreadFactory factory = new C2MEForkJoinWorkerThreadFactory("c2me", "C2ME worker #%d", Thread.NORM_PRIORITY - 1);
    public static final int GLOBAL_EXECUTOR_PARALLELISM = (int) ModuleEntryPoint.globalExecutorParallelism;
    private static final AtomicInteger prioritizedSchedulerCounter = new AtomicInteger(0);
    public static final ExecutorManager prioritizedScheduler = new ExecutorManager(GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM, thread -> {
        thread.setDaemon(true);
        thread.setName("c2me-prioritized-%d".formatted(prioritizedSchedulerCounter.getAndIncrement()));
    });

    public static final ExecutorService asyncScheduler = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("c2me-sched").build());

//    public static final TaskExecutor<Runnable> asyncSchedulerTaskExecutor = TaskExecutor.create(asyncScheduler, "c2me-sched");

}
