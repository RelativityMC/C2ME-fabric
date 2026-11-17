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

package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.flowsched.scheduler.ObjectFactory;
import io.netty.util.internal.PlatformDependent;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import org.jctools.util.UnsafeAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class TheSpeedyObjectFactory implements ObjectFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger("TheSpeedyObjectFactory");

    public static final ObjectFactory INSTANCE;

    static {
        Object unsafe = null;
        if (PlatformDependent.hasUnsafe()) {
            try {
                unsafe = java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    // force JCTools to initialize unsafe
                    return UnsafeAccess.UNSAFE;
                });
            } catch (Throwable ignored) {
            }
        }
        final boolean hasUnsafe = unsafe != null;
        if (hasUnsafe) {
            LOGGER.info("Using TheSpeedyObjectFactory with Unsafe");
        } else {
            LOGGER.info("Using TheSpeedyObjectFactory without Unsafe");
        }
        INSTANCE = new TheSpeedyObjectFactory(hasUnsafe);
    }

    private final boolean hasUnsafe;

    private TheSpeedyObjectFactory(boolean hasUnsafe) {
        this.hasUnsafe = hasUnsafe;
    }

    @Override
    public <K, V> ConcurrentMap<K, V> createConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public <E> Set<E> createConcurrentSet() {
        return Collections.newSetFromMap(this.createConcurrentHashMap());
    }

    @Override
    public <E> Queue<E> newMPMCQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    @Override
    public <E> Queue<E> newMPSCQueue() {
        return this.hasUnsafe ? new MpscUnboundedArrayQueue<>(1024) : new MpscAtomicArrayQueue<>(1024);
    }
}
