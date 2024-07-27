package com.ishland.c2me.rewrites.chunksystem.common;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public record ChunkState(Chunk chunk, ChunkStatus reachedStatus) {
}
