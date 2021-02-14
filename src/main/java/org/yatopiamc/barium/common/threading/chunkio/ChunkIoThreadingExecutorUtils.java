package org.yatopiamc.barium.common.threading.chunkio;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChunkIoThreadingExecutorUtils {

    private static final ThreadPoolExecutor serializerExecutor = new ThreadPoolExecutor(
            Math.min(6, Runtime.getRuntime().availableProcessors()),
            Runtime.getRuntime().availableProcessors(),
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("barium chunkio serializer worker #%d").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build()
    );

    public static void executeSerializing(Runnable command) {
        serializerExecutor.execute(command);
    }

}
