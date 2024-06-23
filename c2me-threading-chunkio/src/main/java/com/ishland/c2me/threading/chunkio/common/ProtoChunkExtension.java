package com.ishland.c2me.threading.chunkio.common;

import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProtoChunkExtension {

    void setBlendingInfo(ChunkPos pos, List<BitSet> bitSet);

    boolean getNeedBlending();

    void setInitialMainThreadComputeFuture(CompletableFuture<Void> future);
    CompletableFuture<Void> getInitialMainThreadComputeFuture();

}
