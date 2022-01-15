package com.ishland.c2me.common.optimization.chunkscheduling.idle_tasks;

import net.minecraft.util.math.ChunkPos;

public interface IThreadedAnvilChunkStorage {

    void enqueueDirtyChunkPosForAutoSave(ChunkPos chunkPos);

    boolean runOneChunkAutoSave();

}
