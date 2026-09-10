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