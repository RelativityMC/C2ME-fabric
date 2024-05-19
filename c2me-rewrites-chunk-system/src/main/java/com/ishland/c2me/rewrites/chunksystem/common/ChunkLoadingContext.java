package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.flowsched.scheduler.ItemHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;

import java.util.List;

public record ChunkLoadingContext(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext> holder, ChunkGenerationContext context, List<Chunk> chunks) {
}
