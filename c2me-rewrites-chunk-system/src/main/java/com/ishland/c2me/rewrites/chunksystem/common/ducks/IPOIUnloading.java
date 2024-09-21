package com.ishland.c2me.rewrites.chunksystem.common.ducks;

import net.minecraft.util.math.ChunkPos;

public interface IPOIUnloading {

    void c2me$unloadPoi(ChunkPos pos);

    default boolean c2me$shouldUnloadPoi(ChunkPos pos) {
        return false;
    }

}
