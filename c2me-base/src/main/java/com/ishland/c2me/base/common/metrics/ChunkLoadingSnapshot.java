package com.ishland.c2me.base.common.metrics;

/**
 * Immutable snapshot of chunk loading metrics for network transmission
 */
public record ChunkLoadingSnapshot(
    double avgChunkLoadTimeMs,
    double minChunkLoadTimeMs,
    double maxChunkLoadTimeMs,
    int currentChunkQueueSize,

    double avgWorldgenTimeMs,
    double avgWorldgenLoadTimeMs,

    double avgIoReadTimeMs,
    double avgIoWriteTimeMs,
    int currentIoBacklogSize,
    int currentIoPendingWrites,

    int currentSchedulerQueueSize,
    int currentSchedulerActiveThreads
) {

    public static ChunkLoadingSnapshot EMPTY = new ChunkLoadingSnapshot(
        0.0, 0.0, 0.0, 0,
        0.0, 0.0,
        0.0, 0.0, 0, 0,
        0, 0
    );

}