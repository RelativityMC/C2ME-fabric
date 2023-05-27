package com.ishland.c2me.base.mixin.access;

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
public interface IChunkTickScheduler<T> extends SerializableTickScheduler<T> {
    @Accessor
    @Nullable List<Tick<T>> getTicks();

    @Accessor
    Queue<OrderedTick<T>> getTickQueue();

}
