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

public class FreeBsdPthread {

    public static void setCurrentThreadPriority(int priority) {
        long pthread_self;
        try {
            pthread_self = Bindings.INSTANCE.pthread_self();
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_self: {}", e.toString());
            return;
        }

        try {
            int ret = Bindings.INSTANCE.pthread_setprio(pthread_self, priority);
            if (ret != 0) {
                ThreadPriorityPresets.LOGGER.warn("pthread_setprio(pthread_self, {}) failed: {}", priority, ret);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            ThreadPriorityPresets.LOGGER.error("Unable to link to pthread_setprio: {}", e.toString());
            return;
        }
    }

    public interface Bindings extends Library {

        public static final Bindings INSTANCE = Native.load("pthread", Bindings.class);

        public long pthread_self();

        public int pthread_setprio(long pthread, int prio);

    }

}
