/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.base.common;

import com.ishland.c2me.base.ModuleEntryPoint;
import com.ishland.c2me.base.common.scheduler.SingleThreadExecutor;
import com.ishland.flowsched.executor.ExecutorManager;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalExecutors {

//    private static final C2MEForkJoinWorkerThreadFactory factory = new C2MEForkJoinWorkerThreadFactory("c2me", "C2ME worker #%d", Thread.NORM_PRIORITY - 1);
    public static final int GLOBAL_EXECUTOR_PARALLELISM = (int) ModuleEntryPoint.globalExecutorParallelism;
    private static final AtomicInteger prioritizedSchedulerCounter = new AtomicInteger(0);
    public static final ExecutorManager prioritizedScheduler = new ExecutorManager(GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM, thread -> {
        thread.setDaemon(true);
        thread.setPriority(Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, (int) ModuleEntryPoint.threadPoolPriority)));
        thread.setName("c2me-worker-%d".formatted(prioritizedSchedulerCounter.getAndIncrement()));
    });

    public static final Executor asyncScheduler;

    static {
        final SingleThreadExecutor thread = new SingleThreadExecutor();
        thread.setDaemon(true);
        thread.setName("c2me-sched");
        thread.start();
        asyncScheduler = thread;
    }

//    public static final TaskExecutor<Runnable> asyncSchedulerTaskExecutor = TaskExecutor.create(asyncScheduler, "c2me-sched");

}
