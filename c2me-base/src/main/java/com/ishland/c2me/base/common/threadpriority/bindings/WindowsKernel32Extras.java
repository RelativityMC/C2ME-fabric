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
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

public interface WindowsKernel32Extras extends Library {

    public static final WindowsKernel32Extras INSTANCE = Native.load("kernel32", WindowsKernel32Extras.class, W32APIOptions.DEFAULT_OPTIONS);

    public static void setCurrentThreadPriority(int nPriority) {
        WinNT.HANDLE currentThread = Kernel32.INSTANCE.GetCurrentThread();
        if (!Kernel32.INSTANCE.SetThreadPriority(currentThread, nPriority)) {
            ThreadPriorityPresets.LOGGER.warn("SetThreadPriority(currentThread, {}) failed: {}", nPriority, Native.getLastError());
        }
    }

    public static final int THREAD_POWER_THROTTLING_CURRENT_VERSION = 1;
    public static final int THREAD_POWER_THROTTLING_EXECUTION_SPEED = 0x1;

    @Structure.FieldOrder({"Version", "ControlMask", "StateMask"})
    public static class THREAD_POWER_THROTTLING_STATE extends Structure {
        public WinDef.ULONG Version;
        public WinDef.ULONG ControlMask;
        public WinDef.ULONG StateMask;
    }

    public static final int ThreadMemoryPriority = 0;
    public static final int ThreadAbsoluteCpuPriority = 1;
    public static final int ThreadDynamicCodePolicy = 2;
    public static final int ThreadPowerThrottling = 3;
    public static final int ThreadInformationClassMax = 4;

    boolean SetThreadInformation(WinNT.HANDLE hThread, int ThreadInformationClass, THREAD_POWER_THROTTLING_STATE ThreadInformation, WinDef.DWORD ThreadInformationSize);

    public static void enableCurrentThreadPowerThrottling() {
        WinNT.HANDLE currentThread = Kernel32.INSTANCE.GetCurrentThread();
        THREAD_POWER_THROTTLING_STATE threadPowerThrottlingState = new THREAD_POWER_THROTTLING_STATE();
        threadPowerThrottlingState.Version = new WinDef.ULONG(THREAD_POWER_THROTTLING_CURRENT_VERSION);
        threadPowerThrottlingState.ControlMask = new WinDef.ULONG(THREAD_POWER_THROTTLING_EXECUTION_SPEED);
        threadPowerThrottlingState.StateMask = new WinDef.ULONG(THREAD_POWER_THROTTLING_EXECUTION_SPEED);
        try {
            if (!INSTANCE.SetThreadInformation(currentThread, ThreadPowerThrottling, threadPowerThrottlingState, new WinDef.DWORD(12))) {
                ThreadPriorityPresets.LOGGER.warn("SetThreadInformation(currentThread, ThreadPowerThrottling, threadPowerThrottlingState, 12) failed: {}", Native.getLastError());
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to SetThreadInformation: {}", e.toString());
        }
    }

}
