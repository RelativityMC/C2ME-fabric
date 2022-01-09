package com.ishland.c2me.mixin.optimization.reduce_allocs.chunkserialization;

import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;


@Mixin(value = BelowZeroRetrogen.class)
public interface BelowZeroRetrogenAccessor {
    @Accessor
    BitSet getMissingBedrock();

    @Invoker
    ChunkStatus invokeGetTargetStatus();
}
