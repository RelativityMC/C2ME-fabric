package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class ChunkIoThreadingExecutorUtils {

    public static final ThreadFactory ioWorkerFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("IOWorker-%d").setPriority(Thread.NORM_PRIORITY - 1).build();

}
