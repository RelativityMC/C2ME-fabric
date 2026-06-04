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

package com.ishland.c2me.rewrites.chunk_serializer.common.utils;

import com.ishland.c2me.base.mixin.access.IChunkTickScheduler;
import it.unimi.dsi.fastutil.longs.Long2ReferenceAVLTreeMap;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;

import java.lang.reflect.Field;
import java.util.Collection;

public class LithiumUtil {
    static final Field chunkTickScheduler$TickQueuesByTimeAndPriority;


    static {
        Class<?> chunkTickSchedulerClass = ChunkTickScheduler.class;

        Field tickQueuesByTimeAndPriority = null;
        try {
            tickQueuesByTimeAndPriority = chunkTickSchedulerClass.getDeclaredField("tickQueuesByTimeAndPriority");
            tickQueuesByTimeAndPriority.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // pass
        }

        chunkTickScheduler$TickQueuesByTimeAndPriority = tickQueuesByTimeAndPriority;
    }

    public static final boolean IS_LITHIUM_TICK_QUEUE_ACTIVE = chunkTickScheduler$TickQueuesByTimeAndPriority != null;

    public static <T> Collection<Collection<OrderedTick<T>>> getTickQueueCollection(IChunkTickScheduler<T> accessor) {
        try {
            //noinspection unchecked
            Long2ReferenceAVLTreeMap<Collection<OrderedTick<T>>> tickQueuesByTimeAndPriority =
                    (Long2ReferenceAVLTreeMap<Collection<OrderedTick<T>>>)
                            chunkTickScheduler$TickQueuesByTimeAndPriority.get(accessor);
            return tickQueuesByTimeAndPriority.values();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
