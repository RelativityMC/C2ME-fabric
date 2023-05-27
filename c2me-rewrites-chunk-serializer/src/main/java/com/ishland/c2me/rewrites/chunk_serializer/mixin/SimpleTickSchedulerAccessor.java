package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import net.minecraft.world.tick.SerializableTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import net.minecraft.world.tick.Tick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SimpleTickScheduler.class)
public interface SimpleTickSchedulerAccessor<T> extends SerializableTickScheduler<T> {
    @Accessor
    List<Tick<T>> getScheduledTicks();
}
