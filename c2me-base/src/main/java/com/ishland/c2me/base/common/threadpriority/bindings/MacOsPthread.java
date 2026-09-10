package com.ishland.c2me.base.common.threadpriority.bindings;

import com.ishland.c2me.base.common.threadpriority.ThreadPriorityPresets;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public interface MacOsPthread extends Library {

    public static final MacOsPthread INSTANCE = Native.load("pthread", MacOsPthread.class);

    public long pthread_self();

    @Structure.FieldOrder({"sched_priority", "quantum"})
    public static class sched_param extends Structure {
        public int sched_priority;
        public int quantum;
    }

    public int pthread_getschedparam(long thread, IntByReference policy, sched_param param);

    public int pthread_setschedparam(long thread, int policy, sched_param param);

    public static void setCurrentThreadPriority(int priority) {
        long pthread_self;
        try {
            pthread_self = INSTANCE.pthread_self();
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_self: {}", e.toString());
            return;
        }

        IntByReference policy = new IntByReference();
        sched_param param = new sched_param();

        try {
            int ret = INSTANCE.pthread_getschedparam(pthread_self, policy, param);
            if (ret != 0) {
                ThreadPriorityPresets.LOGGER.warn("pthread_getschedparam(pthread_self, policy, param) failed: {}", ret);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_getschedparam: {}", e.toString());
            return;
        }

        param.sched_priority = priority;

        try {
            int ret = INSTANCE.pthread_setschedparam(pthread_self, policy.getValue(), param);
            if (ret != 0) {
                ThreadPriorityPresets.LOGGER.warn("pthread_setschedparam(pthread_self, policy, param) with priority {} failed: {}", priority, ret);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_setschedparam: {}", e.toString());
            return;
        }
    }
}