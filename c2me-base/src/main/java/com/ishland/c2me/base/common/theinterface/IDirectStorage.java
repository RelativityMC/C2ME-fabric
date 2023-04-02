package com.ishland.c2me.base.common.theinterface;

import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

public interface IDirectStorage {

    public CompletableFuture<Void> setRawChunkData(ChunkPos pos, byte[] data);

}
