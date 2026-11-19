/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    public static final ExecutorManager prioritizedScheduler = new ExecutorManager(
            GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM,
            thread -> {
                thread.setDaemon(true);
                thread.setName("c2me-worker-%d".formatted(prioritizedSchedulerCounter.getAndIncrement()));
            },
            () -> ModuleEntryPoint.threadPoolPriorityPreset.applyToCurrentThread(),
            64
    );

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
