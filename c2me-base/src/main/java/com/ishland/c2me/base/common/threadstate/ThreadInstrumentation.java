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

package com.ishland.c2me.base.common.threadstate;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.management.ThreadInfo;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadInstrumentation {

    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("ThreadStateHolderCleaner")
                    .setDaemon(true)
                    .build()
    );

    private static final ConcurrentHashMap<Thread, ThreadState> threadStateMap = new ConcurrentHashMap<>();

    private static final ThreadLocal<ThreadState> threadStateThreadLocal = ThreadLocal.withInitial(() -> getOrCreate(Thread.currentThread()));

    static {
        CLEANER.scheduleAtFixedRate(
                () -> threadStateMap.entrySet().removeIf(entry -> !entry.getKey().isAlive()),
                30,
                30,
                TimeUnit.SECONDS
        );
    }

    public static ThreadState getOrCreate(Thread thread) {
        return threadStateMap.computeIfAbsent(thread, unused -> new ThreadState());
    }

    public static ThreadState get(Thread thread) {
        return threadStateMap.get(thread);
    }

    public static ThreadState getCurrent() {
        return threadStateThreadLocal.get();
    }

    public static String printState(ThreadInfo threadInfo) {
        return printState(threadInfo.getThreadName(), threadInfo.getThreadId(), findFromTid(threadInfo.getThreadId()));
    }

    public static Set<Map.Entry<Thread, ThreadState>> entrySet() {
        return Collections.unmodifiableSet(threadStateMap.entrySet());
    }

    public static String printState(String name, long tid, ThreadState state) {
        if (state != null) {
            RunningWork[] runningWorks = state.toArray();
            if (runningWorks.length != 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("Task trace for thread \"").append(name).append("\" Id=").append(tid).append(" (obtained on a best-effort basis)\n");
                for (RunningWork runningWork : runningWorks) {
                    builder.append(runningWork.toString().indent(4)); // newline included in indent()
                }
                return builder.toString();
            }
        }
        return null;
    }

    private static ThreadState findFromTid(long tid) {
        for (Map.Entry<Thread, ThreadState> entry : threadStateMap.entrySet()) {
            if (entry.getKey().threadId() == tid) {
                return entry.getValue();
            }
        }
        return null;
    }

}
