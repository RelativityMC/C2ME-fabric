package org.yatopiamc.c2me.common.threading.worldgen;

import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.common.util.C2MEForkJoinWorkerThreadFactory;

import java.util.concurrent.ForkJoinPool;

public class WorldGenThreadingExecutorUtils {

    public static final ForkJoinPool mainExecutor = new ForkJoinPool(
            C2MEConfig.threadedWorldGenConfig.parallelism,
            new C2MEForkJoinWorkerThreadFactory("C2ME worldgen worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

}
