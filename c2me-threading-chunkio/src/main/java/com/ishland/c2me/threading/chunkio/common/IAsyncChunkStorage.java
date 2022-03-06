package com.ishland.c2me.threading.chunkio.common;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface IAsyncChunkStorage {

    CompletableFuture<NbtCompound> getNbtAtAsync(ChunkPos pos);

}
