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
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import org.jspecify.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.concurrent.atomic.AtomicBoolean;

public class LinuxLibc {

    public static final int SCHED_NORMAL = 0;
    public static final int SCHED_FIFO = 1;
    public static final int SCHED_RR = 2;
    public static final int SCHED_BATCH = 3;
    public static final int SCHED_IDLE = 5;
    public static final int SCHED_DEADLINE = 6;
    public static final int SCHED_EXT = 7;
    public static final int SCHED_RESET_ON_FORK = 0x40000000;

    private static String stringifyLastError() {
        int errno = Native.getLastError();
        try {
            return Bindings.INSTANCE.strerror(errno);
        } catch (UnsatisfiedLinkError e) {
            return String.valueOf(errno);
        }
    }

    @Structure.FieldOrder({"sched_priority"})
    public static class LinuxSchedParam extends Structure {
        public int sched_priority;
    }

    public static void setCurrentThreadScheduler(int policy) {
        try {
            if (Bindings.INSTANCE.sched_setscheduler(0, policy | SCHED_RESET_ON_FORK, new LinuxSchedParam()) != 0) {
                ThreadPriorityPresets.LOGGER.warn("sched_setscheduler(0, {} | SCHED_RESET_ON_FORK, ...) failed: {}", policy, stringifyLastError());
                if (Bindings.INSTANCE.sched_setscheduler(0, policy, new LinuxSchedParam()) != 0) {
                    ThreadPriorityPresets.LOGGER.warn("sched_setscheduler(0, {}, ...) failed: {}", policy, stringifyLastError());
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

    public static void setCurrentThreadPriority(int priority) {
        try {
            if (Bindings.INSTANCE.setpriority(PRIO_PROCESS, 0, priority) != 0) {
                ThreadPriorityPresets.LOGGER.warn("setpriority(PRIO_PROCESS, 0, {}) failed: {}", priority, stringifyLastError());
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to setpriority: {}", e.toString());
            return;
        }
    }

    //struct sched_attr {
    //    u32 size;              /* Size of this structure */
    //    u32 sched_policy;      /* Policy (SCHED_*) */
    //    u64 sched_flags;       /* Flags */
    //    s32 sched_nice;        /* Nice value (SCHED_OTHER,
    //                                SCHED_BATCH) */
    //    u32 sched_priority;    /* Static priority (SCHED_FIFO,
    //                                SCHED_RR) */
    //    /* For SCHED_DEADLINE */
    //    u64 sched_runtime;
    //    u64 sched_deadline;
    //    u64 sched_period;
    //
    //    /* Utilization hints */
    //    u32 sched_util_min;
    //    u32 sched_util_max;
    //};

    @Structure.FieldOrder({"size", "sched_policy", "sched_flags", "sched_nice", "sched_priority", "sched_runtime", "sched_deadline", "sched_period", "sched_util_min", "sched_util_max"})
    public static class LinuxSchedAttr extends Structure {
        public int size;
        public int sched_policy;
        public long sched_flags;
        public int sched_nice;
        public int sched_priority;
        public long sched_runtime;
        public long sched_deadline;
        public long sched_period;
        public int sched_util_min;
        public int sched_util_max;
    }

    private static final int SCHED_FLAG_RESET_ON_FORK = 0x01;
    private static final int SCHED_FLAG_RECLAIM = 0x02;
    private static final int SCHED_FLAG_DL_OVERRUN = 0x04;
    private static final int SCHED_FLAG_KEEP_POLICY = 0x08;
    private static final int SCHED_FLAG_KEEP_PARAMS = 0x10;
    private static final int SCHED_FLAG_UTIL_CLAMP_MIN = 0x20;
    private static final int SCHED_FLAG_UTIL_CLAMP_MAX = 0x40;

    public static int SYS_sched_setattr() {
        if (Platform.isIntel()) {
            return Platform.is64Bit() ? 314 : 351;
        } else if (Platform.isARM()) {
            return 380;
        }

        return -1;
    }

    public static int SYS_sched_getattr() {
        if (Platform.isIntel()) {
            return Platform.is64Bit() ? 315 : 352;
        } else if (Platform.isARM()) {
            return 381;
        }

        return -1;
    }

    private static final int efficiencyCoreCapacity;

    static {
        int capacity = 0;
        try {
            SystemInfo systemInfo = new SystemInfo();
            int[] knownCapacities = systemInfo.getHardware().getProcessor().getPhysicalProcessors().stream().mapToInt(CentralProcessor.PhysicalProcessor::getEfficiency).sorted().distinct().toArray();
            // sorted ascending
            if (knownCapacities.length > 1) {
                for (int knownCapacity : knownCapacities) {
                    if (knownCapacity < 950) {
                        capacity = knownCapacity;
                    }
                }
            }
            if (capacity != 0) {
                ThreadPriorityPresets.LOGGER.info("Detected E core capacity: {}", capacity);
            }
        } catch (Throwable t) {
            ThreadPriorityPresets.LOGGER.warn("Failed to fetch cpu core cpu_capacity");
        }
        efficiencyCoreCapacity = capacity;
    }

    private static final AtomicBoolean SCHED_ATTR_FAILS = new AtomicBoolean();

    private static @Nullable LinuxSchedAttr getCurrentLinuxSchedAttr(int sys_sched_getattr) {
        LinuxSchedAttr schedAttr = new LinuxSchedAttr();
        schedAttr.size = schedAttr.size();
        NativeLong ret;
        ret = oshi.jna.platform.linux.LinuxLibc.INSTANCE.syscall(new NativeLong(sys_sched_getattr), 0, schedAttr, schedAttr.size(), 0);
        if (ret.intValue() != 0) {
            ThreadPriorityPresets.LOGGER.warn("syscall sched_getattr(0, schedAttr, sizeof(schedAttr), 0) failed: {}", stringifyLastError());
            SCHED_ATTR_FAILS.set(true);
            return null;
        }
        return schedAttr;
    }

    public static void setCurrentThreadPreferEfficiencyCores() {
        if (SCHED_ATTR_FAILS.getOpaque()) return;
        if (efficiencyCoreCapacity <= 0) return;
        int sys_sched_getattr = SYS_sched_getattr();
        int sys_sched_setattr = SYS_sched_setattr();
        if (sys_sched_getattr == -1 || sys_sched_setattr == -1) return;

        LinuxSchedAttr schedAttr = getCurrentLinuxSchedAttr(sys_sched_getattr);
        if (schedAttr == null) return;
        NativeLong ret;
        schedAttr.sched_flags |= SCHED_FLAG_UTIL_CLAMP_MAX | SCHED_FLAG_UTIL_CLAMP_MIN;
        schedAttr.sched_util_min = 0;
        schedAttr.sched_util_max = efficiencyCoreCapacity;
        ret = oshi.jna.platform.linux.LinuxLibc.INSTANCE.syscall(new NativeLong(sys_sched_setattr), 0, schedAttr, 0);
        if (ret.intValue() != 0) {
            ThreadPriorityPresets.LOGGER.warn("setCurrentThreadPreferEfficiencyCores: syscall sys_sched_setattr(0, schedAttr, 0) failed: {}", stringifyLastError());
            SCHED_ATTR_FAILS.set(true);
            return;
        }
    }

    public static void unsetCurrentThreadUclamp() {
        if (SCHED_ATTR_FAILS.getOpaque()) return;
        int sys_sched_getattr = SYS_sched_getattr();
        int sys_sched_setattr = SYS_sched_setattr();
        if (sys_sched_getattr == -1 || sys_sched_setattr == -1) return;

        LinuxSchedAttr schedAttr = getCurrentLinuxSchedAttr(sys_sched_getattr);
        if (schedAttr == null) return;
        NativeLong ret;
        schedAttr.sched_flags |= SCHED_FLAG_UTIL_CLAMP_MAX | SCHED_FLAG_UTIL_CLAMP_MIN;
        schedAttr.sched_util_min = -1;
        schedAttr.sched_util_max = -1;
        ret = oshi.jna.platform.linux.LinuxLibc.INSTANCE.syscall(new NativeLong(sys_sched_setattr), 0, schedAttr, 0);
        if (ret.intValue() != 0) {
            ThreadPriorityPresets.LOGGER.warn("unsetCurrentThreadUclamp: syscall sys_sched_setattr(0, schedAttr, 0) failed: {}", stringifyLastError());
            return;
        }
    }

    public static void setCurrentThreadPreferMaximumPerformance() {
        if (SCHED_ATTR_FAILS.getOpaque()) return;
        int sys_sched_getattr = SYS_sched_getattr();
        int sys_sched_setattr = SYS_sched_setattr();
        if (sys_sched_getattr == -1 || sys_sched_setattr == -1) return;

        LinuxSchedAttr schedAttr = getCurrentLinuxSchedAttr(sys_sched_getattr);
        if (schedAttr == null) return;
        NativeLong ret;
        schedAttr.sched_flags |= SCHED_FLAG_UTIL_CLAMP_MAX | SCHED_FLAG_UTIL_CLAMP_MIN;
        schedAttr.sched_util_min = 1024;
        schedAttr.sched_util_max = 1024;
        ret = oshi.jna.platform.linux.LinuxLibc.INSTANCE.syscall(new NativeLong(sys_sched_setattr), 0, schedAttr, 0);
        if (ret.intValue() != 0) {
            ThreadPriorityPresets.LOGGER.warn("setCurrentThreadPreferMaximumPerformance: syscall sys_sched_setattr(0, schedAttr, 0) failed: {}", stringifyLastError());
            SCHED_ATTR_FAILS.set(true);
            return;
        }
    }

    public interface Bindings extends Library {

        public static final Bindings INSTANCE = Native.load("c", Bindings.class);

        int sched_setscheduler(int pid, int policy, LinuxSchedParam param);

        int setpriority(int which, int who, int prio);

        String strerror(int errnum);

    }

}
