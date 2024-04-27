package com.ishland.c2me.base.mixin.access;

import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkTicketManager.DistanceFromNearestPlayerTracker.class)
public interface IChunkTicketManagerDistanceFromNearestPlayerTracker {

    @Accessor
    int getMaxDistance();

}
