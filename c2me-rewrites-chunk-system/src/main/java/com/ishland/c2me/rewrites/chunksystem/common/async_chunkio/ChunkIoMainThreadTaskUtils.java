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

package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkIoMainThreadTaskUtils {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<ArrayDeque<ReferenceArrayList<Runnable>>> deserializeStack = ThreadLocal.withInitial(ArrayDeque::new);
    private static final LinkedBlockingQueue<Runnable> mainThreadQueue = new LinkedBlockingQueue<>();

    public static void push(ReferenceArrayList<Runnable> queue) {
        if (queue == null) {
            throw new IllegalArgumentException("Queue cannot be null");
        }
        deserializeStack.get().push(queue);
    }

    public static void pop(ReferenceArrayList<Runnable> queue) {
        if (queue == null) {
            throw new IllegalArgumentException("Queue cannot be null");
        }
        final ArrayDeque<ReferenceArrayList<Runnable>> stack = deserializeStack.get();
        if (stack.peek() != queue) throw new IllegalStateException("Unexpected queue");
        stack.pop();
    }

    public static void executeMain(Runnable command) {
        final ArrayDeque<ReferenceArrayList<Runnable>> stack = deserializeStack.get();
        if (stack.isEmpty()) command.run();
        else stack.peek().add(command);
    }

    public static void drainQueue(ReferenceArrayList<Runnable> queue) {
        List<Throwable> throwableList = new ReferenceArrayList<>();
        for (Runnable command : queue) {
            try {
                command.run();
            } catch (Throwable t) {
                throwableList.add(t);
            }
        }
        queue.clear();
        if (!throwableList.isEmpty()) {
            final RuntimeException exception = new RuntimeException("Exception occurred while executing main thread tasks");
            throwableList.forEach(exception::addSuppressed);
            throw exception;
        }
    }

}
