package com.ishland.c2me.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.util.PriorityInterface;
import com.ishland.c2me.common.util.PriorityRunnable;

import java.util.Comparator;
import java.util.concurrent.*;

public class GlobalExecutors {

    private static final Comparator<Runnable> compare = (o1, o2) -> {
        if (o1 instanceof PriorityRunnable && o2 instanceof PriorityRunnable) {
            return ((PriorityInterface) o1).getPriority() - ((PriorityInterface) o2).getPriority();
        } else if (o1 instanceof PriorityRunnable) {
            return 1;
        } else if (o2 instanceof PriorityRunnable) {
            return -1;
        }
        return 0;
    };
    static PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(C2MEConfig.globalExecutorParallelism, compare);
    public static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("C2ME worker #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build();
    public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(C2MEConfig.globalExecutorParallelism, Runtime.getRuntime().availableProcessors(),  60L, TimeUnit.MILLISECONDS, queue, threadFactory);

    public static final Executor invokingExecutor = r -> {
        if (Thread.currentThread().getName().startsWith("C2ME worker #")) {
            r.run();
        } else {
            executor.execute(r);
        }
    };

    public static final ExecutorService asyncScheduler = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("C2ME scheduler").build());

}
