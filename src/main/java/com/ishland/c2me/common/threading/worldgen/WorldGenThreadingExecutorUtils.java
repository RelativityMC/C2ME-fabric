package com.ishland.c2me.common.threading.worldgen;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.util.C2MEForkJoinWorkerThreadFactory;
import com.ishland.c2me.common.util.StatsTrackingExecutor;

import java.util.concurrent.ForkJoinPool;

public class WorldGenThreadingExecutorUtils {

    private static final ForkJoinPool mainExecutor0 = new ForkJoinPool(
            C2MEConfig.threadedWorldGenConfig.parallelism,
            new C2MEForkJoinWorkerThreadFactory("C2ME worldgen worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

    public static final StatsTrackingExecutor mainExecutor = new StatsTrackingExecutor(mainExecutor0);

}
