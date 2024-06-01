package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.AbstractChunkHolder;

public record ChunkLoadingContext(
        ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder,
        ServerChunkLoadingManager tacs, SchedulingManager schedulingManager,
        BoundedRegionArray<AbstractChunkHolder> chunks,
        KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies) {
}
