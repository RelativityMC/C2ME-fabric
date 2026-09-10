package com.ishland.c2me.base.common.threadpriority.bindings;

import com.ishland.c2me.base.common.threadpriority.ThreadPriorityPresets;
import com.sun.jna.Library;
import com.sun.jna.Native;

public interface FreeBsdPthread extends Library {

    public static final FreeBsdPthread INSTANCE = Native.load("pthread", FreeBsdPthread.class);

    public long pthread_self();

    public int pthread_setprio(long pthread, int prio);

    public static void setCurrentThreadPriority(int priority) {
        long pthread_self;
        try {
            pthread_self = INSTANCE.pthread_self();
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_self: {}", e.toString());
            return;
        }

        try {
            int ret = INSTANCE.pthread_setprio(pthread_self, priority);
            if (ret != 0) {
                ThreadPriorityPresets.LOGGER.warn("pthread_setprio(pthread_self, {}) failed: {}", priority, ret);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_setprio: {}", e.toString());
            return;
        }
    }

}
