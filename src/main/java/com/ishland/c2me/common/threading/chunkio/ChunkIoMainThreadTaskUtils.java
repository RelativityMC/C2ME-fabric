package com.ishland.c2me.common.threading.chunkio;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkIoMainThreadTaskUtils {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<ArrayDeque<Object>> deserializeStack = ThreadLocal.withInitial(ArrayDeque::new);
    private static final LinkedBlockingQueue<Runnable> mainThreadQueue = new LinkedBlockingQueue<>();

    public static void push() {
        deserializeStack.get().push(new Object());
    }

    public static void pop() {
        deserializeStack.get().pop();
    }

    public static void executeMain(Runnable command) {
        if (deserializeStack.get().isEmpty()) command.run();
        else mainThreadQueue.add(command);
    }

    public static void drainQueue() {
        Runnable command;
        while ((command = mainThreadQueue.poll()) != null) {
            try {
                command.run();
            } catch (Throwable t) {
                LOGGER.error("Error while executing main thread task", t);
            }
        }
    }

}
