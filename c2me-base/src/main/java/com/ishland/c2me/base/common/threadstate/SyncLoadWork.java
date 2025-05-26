package com.ishland.c2me.base.common.threadstate;

import com.ishland.c2me.base.common.util.TimeUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

public record SyncLoadWork(ServerWorld world, ChunkPos chunkPos, ChunkStatus targetStatus, boolean create, long startTime) implements RunningWork {

    public SyncLoadWork(ServerWorld world, ChunkPos chunkPos, ChunkStatus targetStatus, boolean create) {
        this(world, chunkPos, targetStatus, create, System.nanoTime());
    }

    @Override
    public String toString() {
        return String.format("Sync load chunk %s to status %s in world %s (create=%s) (%s elapsed)",
                chunkPos, targetStatus, world.getRegistryKey().getValue(), create, TimeUtil.formatElapsedTime(System.nanoTime() - startTime));
    }

}
