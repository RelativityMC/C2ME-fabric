package com.ishland.c2me.base.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class C2MENormalWorkerThreadFactory implements ThreadFactory {
    private final AtomicLong serial = new AtomicLong(0);
    private final String groupName;
    private final String namePattern;
    private final int priority;

    private final ThreadGroup threadGroup;

    public C2MENormalWorkerThreadFactory(String groupName, String namePattern, int priority) {
        this.groupName = groupName;
        this.namePattern = namePattern;
        this.priority = priority;

        this.threadGroup = new ThreadGroup(this.groupName);
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        final Thread thread = new Thread(this.threadGroup, r, String.format(namePattern, serial.incrementAndGet()));
        thread.setDaemon(true);
        thread.setPriority(priority);
        return thread;
    }
}
