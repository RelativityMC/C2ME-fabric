package com.ishland.c2me.threading.chunkio.common;

import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;
import java.util.List;

public interface ProtoChunkExtension {

    void setBlendingInfo(ChunkPos pos, List<BitSet> bitSet);

    boolean getNeedBlending();

}
