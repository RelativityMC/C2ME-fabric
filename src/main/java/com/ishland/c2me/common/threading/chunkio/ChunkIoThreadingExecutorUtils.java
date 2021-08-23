package com.ishland.c2me.common.threading.chunkio;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.common.util.StatsTrackingExecutor;
import org.threadly.concurrent.UnfairExecutor;
import com.ishland.c2me.common.config.C2MEConfig;

import java.util.concurrent.ThreadFactory;

public class ChunkIoThreadingExecutorUtils {

    private static final UnfairExecutor serializerExecutor0 = new UnfairExecutor(
            C2MEConfig.asyncIoConfig.serializerParallelism,
            new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).setNameFormat("C2ME serializer worker #%d").build()
    );

    public static final StatsTrackingExecutor serializerExecutor = new StatsTrackingExecutor(serializerExecutor0);

    public static final ThreadFactory ioWorkerFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("IOWorker-%d").setPriority(Thread.NORM_PRIORITY - 1).build();

}
