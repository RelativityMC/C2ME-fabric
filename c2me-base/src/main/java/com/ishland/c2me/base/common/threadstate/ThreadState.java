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
