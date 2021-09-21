package com.ishland.c2me.common.fixes.general.threading;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface IChunkTicketManager {

    ReentrantReadWriteLock getTicketLock();

}
