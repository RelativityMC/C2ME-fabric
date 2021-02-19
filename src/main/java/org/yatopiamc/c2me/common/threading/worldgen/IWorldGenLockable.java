package org.yatopiamc.c2me.common.threading.worldgen;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.util.math.ChunkPos;

public interface IWorldGenLockable {

    AsyncLock getWorldGenSingleThreadedLock();

    AsyncNamedLock<ChunkPos> getWorldGenChunkLock();

}
