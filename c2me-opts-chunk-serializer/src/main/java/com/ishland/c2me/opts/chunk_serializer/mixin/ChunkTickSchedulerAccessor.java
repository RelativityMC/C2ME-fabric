package com.ishland.c2me.opts.chunk_serializer.mixin;

import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.SerializableTickScheduler;
import net.minecraft.world.tick.Tick;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Queue;

@Mixin(ChunkTickScheduler.class)
public interface ChunkTickSchedulerAccessor<T> extends SerializableTickScheduler<T> {
    @Accessor
    @Nullable List<Tick<T>> getTicks();

    @Accessor
    Queue<OrderedTick<T>> getTickQueue();

}
