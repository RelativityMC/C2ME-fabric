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
