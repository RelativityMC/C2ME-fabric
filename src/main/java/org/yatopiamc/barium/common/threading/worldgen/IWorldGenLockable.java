package org.yatopiamc.barium.common.threading.worldgen;

import com.ibm.asyncutil.locks.AsyncLock;

public interface IWorldGenLockable {

    AsyncLock getWorldGenSingleThreadedLock();

}
