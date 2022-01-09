package com.ishland.c2me.mixin.optimization.reduce_allocs.chunkserialization;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.tick.Tick")
public interface TickAccessor<T> {
    @Accessor
    T getType();

    @Accessor
    BlockPos getPos();

    @Accessor
    int getDelay();

    @Accessor
    TickPriority getPriority();
}
