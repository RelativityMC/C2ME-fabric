package org.yatopiamc.barium.common.mixininterface;

import com.ibm.asyncutil.locks.AsyncLock;

public interface IServerWorld {

    AsyncLock getWorldGenSingleThreadedLock();

}
