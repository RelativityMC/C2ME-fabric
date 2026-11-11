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
