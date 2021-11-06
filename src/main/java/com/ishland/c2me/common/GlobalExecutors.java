package com.ishland.c2me.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.util.C2MEForkJoinWorkerThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class GlobalExecutors {

    private static final C2MEForkJoinWorkerThreadFactory factory = new C2MEForkJoinWorkerThreadFactory("c2me", "C2ME worker #%d", Thread.NORM_PRIORITY - 1);
    public static final ForkJoinPool executor = new ForkJoinPool(
            C2MEConfig.globalExecutorParallelism,
            factory,
            null,
            true
    );
    public static final Executor invokingExecutor = r -> {
        if (Thread.currentThread().getThreadGroup() == factory.getThreadGroup()) {
            r.run();
        } else {
            executor.execute(r);
        }
    };

    public static final ExecutorService asyncScheduler = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("C2ME scheduler").build());

}
