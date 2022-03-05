package com.ishland.c2me.threading.worldgen.common;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.util.math.ChunkPos;

public interface IWorldGenLockable {

    AsyncLock getWorldGenSingleThreadedLock();

    AsyncNamedLock<ChunkPos> getWorldGenChunkLock();

}
