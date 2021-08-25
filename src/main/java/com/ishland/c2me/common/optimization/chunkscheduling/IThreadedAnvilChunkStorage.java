package com.ishland.c2me.common.optimization.chunkscheduling;

import java.util.concurrent.Executor;

public interface IThreadedAnvilChunkStorage {

    Executor getMainInvokingExecutor();

}
