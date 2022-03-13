package com.ishland.c2me.notickvd.common;

import it.unimi.dsi.fastutil.longs.LongSet;

public interface IChunkTicketManager {

    LongSet getNoTickOnlyChunks();

    int getNoTickPendingTicketUpdates();

}
