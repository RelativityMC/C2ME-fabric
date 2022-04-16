package com.ishland.c2me.opts.chunk_access.common;

import net.minecraft.world.ChunkRegion;

public class CurrentWorldGenState {

    private static final ThreadLocal<ChunkRegion> currentRegion = new ThreadLocal<>();

    public static ChunkRegion getCurrentRegion() {
        return currentRegion.get();
    }

    public static void setCurrentRegion(ChunkRegion region) {
        currentRegion.set(region);
    }

    public static void clearCurrentRegion() {
        currentRegion.remove();
    }

}
