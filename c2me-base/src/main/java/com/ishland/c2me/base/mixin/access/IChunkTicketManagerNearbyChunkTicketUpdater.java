package com.ishland.c2me.base.mixin.access;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkTicketManager.NearbyChunkTicketUpdater.class)
public interface IChunkTicketManagerNearbyChunkTicketUpdater {

    @Accessor
    Long2IntMap getDistances();

}
