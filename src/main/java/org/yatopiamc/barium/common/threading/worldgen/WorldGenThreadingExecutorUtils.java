package org.yatopiamc.barium.common.threading.worldgen;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.yatopiamc.barium.common.util.BariumForkJoinWorkerThreadFactory;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorldGenThreadingExecutorUtils {

    public static final ForkJoinPool mainExecutor = new ForkJoinPool(
            Math.min(8, Runtime.getRuntime().availableProcessors()),
            new BariumForkJoinWorkerThreadFactory("barium worldgen worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );
    public static final ThreadPoolExecutor scheduler = new ThreadPoolExecutor(
            1,
            1,
            0, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("barium worldgen scheduler").setDaemon(true).setPriority(Thread.NORM_PRIORITY).build()
    );

}
