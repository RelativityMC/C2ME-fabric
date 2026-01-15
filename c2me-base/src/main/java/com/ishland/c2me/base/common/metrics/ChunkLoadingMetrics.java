package com.ishland.c2me.base.common.metrics;

/**
 * Metrics collector for chunk loading performance
 */
public class ChunkLoadingMetrics {

    // Chunk system metrics
    public final RingBuffer chunkLoadTimeMs = new RingBuffer(MetricsConfig.maxHistorySize); // Time to load chunk to SERVER_ACCESSIBLE
    public final RingBuffer chunkQueueSize = new RingBuffer(MetricsConfig.maxHistorySize); // Current chunk queue size

    // Worldgen metrics
    public final RingBuffer worldgenTimeMs = new RingBuffer(MetricsConfig.maxHistorySize); // Worldgen stage time
    public final RingBuffer worldgenLoadTimeMs = new RingBuffer(MetricsConfig.maxHistorySize); // Worldgen load stage time

    // IO metrics
    public final RingBuffer ioReadTimeMs = new RingBuffer(MetricsConfig.maxHistorySize); // IO read latency
    public final RingBuffer ioWriteTimeMs = new RingBuffer(MetricsConfig.maxHistorySize); // IO write latency
    public final RingBuffer ioBacklogSize = new RingBuffer(MetricsConfig.maxHistorySize); // IO backlog size
    public final RingBuffer ioPendingWrites = new RingBuffer(MetricsConfig.maxHistorySize); // Pending write futures

    // Scheduler metrics
    public final RingBuffer schedulerQueueSize = new RingBuffer(MetricsConfig.maxHistorySize); // Global work queue size
    public final RingBuffer schedulerActiveThreads = new RingBuffer(MetricsConfig.maxHistorySize); // Active worker threads

    private static ChunkLoadingMetrics INSTANCE;

    private ChunkLoadingMetrics() {
    }

    public static ChunkLoadingMetrics getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChunkLoadingMetrics();
        }
        return INSTANCE;
    }

    public void recordChunkLoadTime(double timeMs) {
        if (MetricsConfig.enabled) {
            chunkLoadTimeMs.add(timeMs);
        }
    }

    public void recordChunkQueueSize(int size) {
        if (MetricsConfig.enabled) {
            chunkQueueSize.add(size);
        }
    }

    public void recordWorldgenTime(double timeMs) {
        if (MetricsConfig.enabled) {
            worldgenTimeMs.add(timeMs);
        }
    }

    public void recordWorldgenLoadTime(double timeMs) {
        if (MetricsConfig.enabled) {
            worldgenLoadTimeMs.add(timeMs);
        }
    }

    public void recordIoReadTime(double timeMs) {
        if (MetricsConfig.enabled) {
            ioReadTimeMs.add(timeMs);
        }
    }

    public void recordIoWriteTime(double timeMs) {
        if (MetricsConfig.enabled) {
            ioWriteTimeMs.add(timeMs);
        }
    }

    public void recordIoBacklogSize(int size) {
        if (MetricsConfig.enabled) {
            ioBacklogSize.add(size);
        }
    }

    public void recordIoPendingWrites(int count) {
        if (MetricsConfig.enabled) {
            ioPendingWrites.add(count);
        }
    }

    public void recordSchedulerQueueSize(int size) {
        if (MetricsConfig.enabled) {
            schedulerQueueSize.add(size);
        }
    }

    public void recordSchedulerActiveThreads(int count) {
        if (MetricsConfig.enabled) {
            schedulerActiveThreads.add(count);
        }
    }

    public ChunkLoadingSnapshot getSnapshot() {
        return new ChunkLoadingSnapshot(
            chunkLoadTimeMs.getAverage(),
            chunkLoadTimeMs.getMin(),
            chunkLoadTimeMs.getMax(),
            (int) chunkQueueSize.getLast(),

            worldgenTimeMs.getAverage(),
            worldgenLoadTimeMs.getAverage(),

            ioReadTimeMs.getAverage(),
            ioWriteTimeMs.getAverage(),
            (int) ioBacklogSize.getLast(),
            (int) ioPendingWrites.getLast(),

            (int) schedulerQueueSize.getLast(),
            (int) schedulerActiveThreads.getLast()
        );
    }

}