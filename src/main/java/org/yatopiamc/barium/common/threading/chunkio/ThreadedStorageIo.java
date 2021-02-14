package org.yatopiamc.barium.common.threading.chunkio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface ThreadedStorageIo {

    CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos);

}
