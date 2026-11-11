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

package com.ishland.c2me.base.common.threadpriority.bindings;

import com.ishland.c2me.base.common.threadpriority.ThreadPriorityPresets;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public class MacOsPthread {

    @Structure.FieldOrder({"sched_priority", "quantum"})
    public static class sched_param extends Structure {
        public int sched_priority;
        public int quantum;
    }

    public static void setCurrentThreadPriority(int priority) {
        long pthread_self;
        try {
            pthread_self = Bindings.INSTANCE.pthread_self();
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_self: {}", e.toString());
            return;
        }

        IntByReference policy = new IntByReference();
        sched_param param = new sched_param();

        try {
            int ret = Bindings.INSTANCE.pthread_getschedparam(pthread_self, policy, param);
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
            int ret = Bindings.INSTANCE.pthread_setschedparam(pthread_self, policy.getValue(), param);
            if (ret != 0) {
                ThreadPriorityPresets.LOGGER.warn("pthread_setschedparam(pthread_self, policy, param) with priority {} failed: {}", priority, ret);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_setschedparam: {}", e.toString());
            return;
        }
    }

    public interface Bindings extends Library {

        public static final Bindings INSTANCE = Native.load("pthread", Bindings.class);

        public long pthread_self();

        public int pthread_getschedparam(long thread, IntByReference policy, sched_param param);

        public int pthread_setschedparam(long thread, int policy, sched_param param);

    }

}