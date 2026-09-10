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
                    LinuxLibc.setCurrentThreadPriority(3);
                }
                case WINDOWS -> {
                    WindowsKernel32Extras.setCurrentThreadPriority(Kernel32.THREAD_PRIORITY_BELOW_NORMAL);
                    WindowsKernel32Extras.enableCurrentThreadPowerThrottling();
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
                    LinuxLibc.setCurrentThreadPriority(8);
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
                    LinuxLibc.setCurrentThreadPriority(16);
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
