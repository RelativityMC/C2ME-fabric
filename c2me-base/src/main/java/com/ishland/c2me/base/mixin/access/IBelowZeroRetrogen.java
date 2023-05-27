package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;


@Mixin(value = BelowZeroRetrogen.class)
public interface IBelowZeroRetrogen {
    @Accessor
    BitSet getMissingBedrock();

    @Invoker
    ChunkStatus invokeGetTargetStatus();
}
