package com.ishland.c2me.common.notickvd;

import net.minecraft.util.math.ChunkPos;

import java.util.Set;

public interface IChunkTicketManager {

    Set<ChunkPos> getNoTickOnlyChunks();

    int getNoTickPendingTicketUpdates();

}
