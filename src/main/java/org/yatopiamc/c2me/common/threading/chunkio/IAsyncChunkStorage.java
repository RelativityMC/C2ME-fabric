package org.yatopiamc.c2me.common.threading.chunkio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface IAsyncChunkStorage {

    CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos);

}
