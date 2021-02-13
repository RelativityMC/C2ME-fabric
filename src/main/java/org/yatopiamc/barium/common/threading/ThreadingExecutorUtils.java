package org.yatopiamc.barium.common.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadingExecutorUtils {

    private static final ThreadPoolExecutor mainExecutor = new ThreadPoolExecutor(
            Math.min(4, Runtime.getRuntime().availableProcessors()),
            Runtime.getRuntime().availableProcessors(),
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("barium task thread #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build()
    );

    public static void execute(Runnable command) {
        mainExecutor.execute(command);
    }

}
