package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProtoChunkExtension {

    void setBlendingComputeFuture(CompletableFuture<Void> future);

    void setBlendingInfo(ChunkPos pos, List<BitSet> bitSet);

    boolean getNeedBlending();

    void setInitialMainThreadComputeFuture(CompletableFuture<Void> future);
    CompletableFuture<Void> getInitialMainThreadComputeFuture();

}
