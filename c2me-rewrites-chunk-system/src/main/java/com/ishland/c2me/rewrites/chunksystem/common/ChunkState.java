package com.ishland.c2me.rewrites.chunksystem.common;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;

public record ChunkState(Chunk chunk, ProtoChunk protoChunk, ChunkStatus reachedStatus) {
}
