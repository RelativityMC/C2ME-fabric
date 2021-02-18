package org.yatopiamc.C2ME.common.threading.chunkio;

import org.yatopiamc.C2ME.common.util.C2MEForkJoinWorkerThreadFactory;

import java.util.concurrent.ForkJoinPool;

public class ChunkIoThreadingExecutorUtils {

    public static final ForkJoinPool serializerExecutor = new ForkJoinPool(
            Math.min(6, Runtime.getRuntime().availableProcessors()),
            new C2MEForkJoinWorkerThreadFactory("C2ME chunkio serializer worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

}
