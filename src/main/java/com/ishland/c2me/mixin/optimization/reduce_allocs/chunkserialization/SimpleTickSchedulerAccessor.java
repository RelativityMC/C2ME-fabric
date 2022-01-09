package com.ishland.c2me.mixin.optimization.reduce_allocs.chunkserialization;

import net.minecraft.world.tick.SimpleTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SimpleTickScheduler.class)
public interface SimpleTickSchedulerAccessor<T> {
    @Accessor
    List<TickAccessor<T>> getScheduledTicks();
}
