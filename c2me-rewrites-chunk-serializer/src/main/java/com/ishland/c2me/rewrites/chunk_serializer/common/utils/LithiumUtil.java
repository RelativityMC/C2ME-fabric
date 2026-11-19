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
