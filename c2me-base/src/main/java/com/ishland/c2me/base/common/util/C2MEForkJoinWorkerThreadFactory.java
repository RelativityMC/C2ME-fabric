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

package com.ishland.c2me.base.common.util;

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
