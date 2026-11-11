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

package com.ishland.c2me.base.common.threadpriority;

import com.ishland.c2me.base.common.threadpriority.bindings.FreeBsdPthread;
import com.ishland.c2me.base.common.threadpriority.bindings.LinuxLibc;
import com.ishland.c2me.base.common.threadpriority.bindings.MacOsPthread;
import com.ishland.c2me.base.common.threadpriority.bindings.WindowsKernel32Extras;
import com.sun.jna.platform.win32.Kernel32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;

public enum ThreadPriorityPresets {

    // mapping range used by JVM:
    // Linux mapping (nice 4 -> -5): https://github.com/openjdk/jdk25u/blob/1fac148c2dcc5c2f065999ff3562a76e3c699d0f/src/hotspot/os/linux/os_linux.cpp#L4388
    // Windows mapping (LOWEST -> HIGHEST): https://github.com/openjdk/jdk25u/blob/1fac148c2dcc5c2f065999ff3562a76e3c699d0f/src/hotspot/os/windows/os_windows.cpp#L3821
    // AIX mapping (prio 55 -> 60): https://github.com/openjdk/jdk25u/blob/1fac148c2dcc5c2f065999ff3562a76e3c699d0f/src/hotspot/os/aix/os_aix.cpp#L2087
    // BSD mapping (prio 0 -> 31): https://github.com/openjdk/jdk25u/blob/1fac148c2dcc5c2f065999ff3562a76e3c699d0f/src/hotspot/os/bsd/os_bsd.cpp#L1955
    // Apple mapping (prio 27 -> 36): https://github.com/openjdk/jdk25u/blob/1fac148c2dcc5c2f065999ff3562a76e3c699d0f/src/hotspot/os/bsd/os_bsd.cpp#L1976

    UNSET {
        @Override
        public void applyToCurrentThread() {
            // no-op
        }
    },
    BELOW_NORMAL {
        @Override
        public void applyToCurrentThread() {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
            switch (SystemInfo.getCurrentPlatform()) {
                case LINUX, ANDROID -> {
                    LinuxLibc.setCurrentThreadScheduler(LinuxLibc.SCHED_BATCH);
                }
                case WINDOWS -> {
                    WindowsKernel32Extras.setCurrentThreadPriority(Kernel32.THREAD_PRIORITY_BELOW_NORMAL);
                }
                case MACOS -> {
                    MacOsPthread.setCurrentThreadPriority(30); // normal prio is 31
                }
                case FREEBSD -> {
                    FreeBsdPthread.setCurrentThreadPriority(12); // normal prio is 15
                }
                case AIX -> {
                }
            }
        }
    },
    LOW {
        @Override
        public void applyToCurrentThread() {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 2);
            switch (SystemInfo.getCurrentPlatform()) {
                case LINUX, ANDROID -> {
                    LinuxLibc.setCurrentThreadScheduler(LinuxLibc.SCHED_BATCH);
                    LinuxLibc.setCurrentThreadPriority(2);
                }
                case WINDOWS -> {
                    WindowsKernel32Extras.setCurrentThreadPriority(Kernel32.THREAD_PRIORITY_LOWEST);
                }
                case MACOS -> {
                    MacOsPthread.setCurrentThreadPriority(28);
                }
                case FREEBSD -> {
                    FreeBsdPthread.setCurrentThreadPriority(6);
                }
                case AIX -> {
                }
            }
        }
    },
    LOWER {
        @Override
        public void applyToCurrentThread() {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 3);
            switch (SystemInfo.getCurrentPlatform()) {
                case LINUX, ANDROID -> {
                    LinuxLibc.setCurrentThreadScheduler(LinuxLibc.SCHED_BATCH);
                    LinuxLibc.setCurrentThreadPriority(5);
                    LinuxLibc.setCurrentThreadPreferEfficiencyCores();
                }
                case WINDOWS -> {
                    WindowsKernel32Extras.setCurrentThreadPriority(Kernel32.THREAD_PRIORITY_LOWEST);
                    WindowsKernel32Extras.enableCurrentThreadPowerThrottling();
                }
                case MACOS -> {
                    MacOsPthread.setCurrentThreadPriority(23);
                }
                case FREEBSD -> {
                    FreeBsdPthread.setCurrentThreadPriority(3);
                }
                case AIX -> {
                }
            }
        }
    },
    LOWEST {
        @Override
        public void applyToCurrentThread() {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 4);
            switch (SystemInfo.getCurrentPlatform()) {
                case LINUX, ANDROID -> {
                    LinuxLibc.setCurrentThreadScheduler(LinuxLibc.SCHED_BATCH);
                    LinuxLibc.setCurrentThreadPriority(12);
                    LinuxLibc.setCurrentThreadPreferEfficiencyCores();
                }
                case WINDOWS -> {
                    WindowsKernel32Extras.setCurrentThreadPriority(Kernel32.THREAD_PRIORITY_IDLE);
                    WindowsKernel32Extras.enableCurrentThreadPowerThrottling();
                }
                case MACOS -> {
                    MacOsPthread.setCurrentThreadPriority(15);
                }
                case FREEBSD -> {
                    FreeBsdPthread.setCurrentThreadPriority(0);
                }
                case AIX -> {
                }
            }
        }
    },
    ;

    public static final Logger LOGGER = LoggerFactory.getLogger(ThreadPriorityPresets.class);

    public abstract void applyToCurrentThread();

}
