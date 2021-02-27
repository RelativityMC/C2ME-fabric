package org.yatopiamc.c2me.common.threading.chunkio;

import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.common.util.C2MEForkJoinWorkerThreadFactory;

import java.util.concurrent.ForkJoinPool;

public class ChunkIoThreadingExecutorUtils {

    public static final ForkJoinPool serializerExecutor = new ForkJoinPool(
            C2MEConfig.asyncIoConfig.serializerParallelism,
            new C2MEForkJoinWorkerThreadFactory("C2ME chunkio serializer worker #%d", Thread.NORM_PRIORITY - 1),
            null,
            true
    );

}
