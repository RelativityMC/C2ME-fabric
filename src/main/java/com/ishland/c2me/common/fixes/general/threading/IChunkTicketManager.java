package com.ishland.c2me.common.fixes.general.threading;

import com.ibm.asyncutil.locks.AsyncReadWriteLock;

public interface IChunkTicketManager {

    AsyncReadWriteLock getTicketLock();

}
