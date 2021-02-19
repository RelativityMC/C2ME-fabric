package org.yatopiamc.c2me.common.threading.worldgen;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.yatopiamc.c2me.common.util.C2MEForkJoinWorkerThreadFactory;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorldGenThreadingExecutorUtils {

    public static final ForkJoinPool mainExecutor = new ForkJoinPool(
            Math.min(6, Runtime.getRuntime().availableProcessors()),
            new C2MEForkJoinWorkerThreadFactory("C2ME worldgen worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

}
