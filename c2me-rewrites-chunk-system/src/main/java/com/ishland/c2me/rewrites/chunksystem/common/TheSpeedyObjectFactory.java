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
