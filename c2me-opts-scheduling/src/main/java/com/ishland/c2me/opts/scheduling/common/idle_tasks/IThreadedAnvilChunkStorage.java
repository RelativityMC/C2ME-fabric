package com.ishland.c2me.opts.scheduling.common.idle_tasks;

import net.minecraft.util.math.ChunkPos;

public interface IThreadedAnvilChunkStorage {

    void enqueueDirtyChunkPosForAutoSave(ChunkPos chunkPos);

    boolean runOneChunkAutoSave();

}
