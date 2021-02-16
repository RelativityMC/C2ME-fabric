package org.yatopiamc.barium.common.threading.chunkio;

import org.yatopiamc.barium.common.util.BariumForkJoinWorkerThreadFactory;

import java.util.concurrent.ForkJoinPool;

public class ChunkIoThreadingExecutorUtils {

    public static final ForkJoinPool serializerExecutor = new ForkJoinPool(
            Math.min(6, Runtime.getRuntime().availableProcessors()),
            new BariumForkJoinWorkerThreadFactory("barium chunkio serializer worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

}
