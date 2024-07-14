package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldChunk.class)
public interface IWorldChunk {

    @Accessor
    boolean isLoadedToWorld();

}
