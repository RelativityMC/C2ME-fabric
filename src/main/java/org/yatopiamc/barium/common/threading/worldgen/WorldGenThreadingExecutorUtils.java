package org.yatopiamc.barium.common.threading.worldgen;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorldGenThreadingExecutorUtils {

    private static final ThreadPoolExecutor mainExecutor = new ThreadPoolExecutor(
            Math.min(4, Runtime.getRuntime().availableProcessors()),
            Runtime.getRuntime().availableProcessors(),
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("barium worldgen worker #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build()
    );
    private static final ThreadPoolExecutor scheduler = new ThreadPoolExecutor(
            1,
            1,
            0, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("barium worldgen scheduler").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build()
    );

    public static void execute(Runnable command) {
        mainExecutor.execute(command);
    }

    public static void schedule(Runnable command) {
        scheduler.execute(command);
    }

}
