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

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadState {

    private final ArrayDeque<RunningWork> runningWorks = new ArrayDeque<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void push(RunningWork runningWork) {
        lock.writeLock().lock();
        try {
            runningWorks.push(runningWork);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void pop(RunningWork runningWork) {
        lock.writeLock().lock();
        try {
            RunningWork popped = runningWorks.peek();
            if (popped != runningWork) {
                IllegalArgumentException exception = new IllegalArgumentException("Corrupt ThreadState");
                exception.printStackTrace();
                throw exception;
            }
            runningWorks.pop();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public WorkClosable begin(RunningWork runningWork) {
        lock.writeLock().lock();
        try {
            runningWorks.push(runningWork);
            return new WorkClosable(this, runningWork);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public RunningWork[] toArray() {
        lock.readLock().lock();
        try {
            return runningWorks.toArray(RunningWork[]::new);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static record WorkClosable(ThreadState state, RunningWork work) implements Closeable {
        @Override
        public void close() {
            state.pop(work);
        }
    }

}
