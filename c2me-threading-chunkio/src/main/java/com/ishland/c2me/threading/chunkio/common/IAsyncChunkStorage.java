package com.ishland.c2me.threading.chunkio.common;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IAsyncChunkStorage {

    CompletableFuture<Optional<NbtCompound>> getNbtAtAsync(ChunkPos pos);

}
