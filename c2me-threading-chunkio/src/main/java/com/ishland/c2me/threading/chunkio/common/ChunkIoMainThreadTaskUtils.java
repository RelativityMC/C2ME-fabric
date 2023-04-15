package com.ishland.c2me.threading.chunkio.common;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.slf4j.Logger;

import java.util.ArrayDeque;
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
        for (Runnable command : queue) {
            try {
                command.run();
            } catch (Throwable t) {
                LOGGER.error("Error while executing main thread task", t);
            }
        }
        queue.clear();
    }

}
