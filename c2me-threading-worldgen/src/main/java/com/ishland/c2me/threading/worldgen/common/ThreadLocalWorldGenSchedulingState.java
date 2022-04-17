package com.ishland.c2me.threading.worldgen.common;

import net.minecraft.server.world.ChunkHolder;

public class ThreadLocalWorldGenSchedulingState {

    private static final ThreadLocal<ChunkHolder> chunkHolder = new ThreadLocal<>();

    public static ChunkHolder getChunkHolder() {
        return chunkHolder.get();
    }

    public static void setChunkHolder(ChunkHolder holder) {
        chunkHolder.set(holder);
    }

    public static void clearChunkHolder() {
        chunkHolder.remove();
    }

}
