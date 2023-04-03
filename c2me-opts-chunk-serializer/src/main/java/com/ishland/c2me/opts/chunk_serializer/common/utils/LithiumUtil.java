package com.ishland.c2me.opts.chunk_serializer.common.utils;

import com.ishland.c2me.opts.chunk_serializer.mixin.ChunkTickSchedulerAccessor;
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

    public static <T> Collection<Collection<OrderedTick<T>>> getTickQueueCollection(ChunkTickSchedulerAccessor<T> accessor) {
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
