package com.ishland.c2me.opts.scheduling.common;

import java.util.concurrent.Executor;

public interface IThreadedAnvilChunkStorage {

    Executor getMainInvokingExecutor();

}
