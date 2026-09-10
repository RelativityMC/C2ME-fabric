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

package com.ishland.c2me.base.common.threadpriority.bindings;

import com.ishland.c2me.base.common.threadpriority.ThreadPriorityPresets;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

public interface LinuxLibc extends Library {

    public static final LinuxLibc INSTANCE = Native.load("c", LinuxLibc.class);

    public static final int SCHED_NORMAL = 0;
    public static final int SCHED_FIFO = 1;
    public static final int SCHED_RR = 2;
    public static final int SCHED_BATCH = 3;
    public static final int SCHED_IDLE = 5;
    public static final int SCHED_DEADLINE = 6;
    public static final int SCHED_EXT = 7;
    public static final int SCHED_RESET_ON_FORK = 0x40000000;

    @Structure.FieldOrder({"sched_priority"})
    public static class LinuxSchedParam extends Structure {
        public int sched_priority;
    }

    int sched_setscheduler(int pid, int policy, LinuxSchedParam param);

    public static void setCurrentThreadScheduler(int policy) {
        try {
            if (INSTANCE.sched_setscheduler(0, policy | SCHED_RESET_ON_FORK, new LinuxSchedParam()) != 0) {
                ThreadPriorityPresets.LOGGER.warn("sched_setscheduler(0, {} | SCHED_RESET_ON_FORK, ...) failed: {}", policy, Native.getLastError());
                if (INSTANCE.sched_setscheduler(0, policy, new LinuxSchedParam()) != 0) {
                    ThreadPriorityPresets.LOGGER.warn("sched_setscheduler(0, {}, ...) failed: {}", policy, Native.getLastError());
                }
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to sched_setscheduler: {}", e.toString());
        }
    }

    public static final int PRIO_MIN = -20;
    public static final int PRIO_MAX = 20;

    public static final int PRIO_PROCESS = 0;
    public static final int PRIO_PGRP = 1;
    public static final int PRIO_USER = 2;

    int setpriority(int which, int who, int prio);

    public static void setCurrentThreadPriority(int priority) {
        try {
            if (INSTANCE.setpriority(PRIO_PROCESS, 0, priority) != 0) {
                ThreadPriorityPresets.LOGGER.warn("setpriority(PRIO_PROCESS, 0, {}) failed: {}", priority, Native.getLastError());
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to setpriority: {}", e.toString());
            return;
        }
    }

}
