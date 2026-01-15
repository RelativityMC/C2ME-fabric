package com.ishland.c2me.client.metrics.common;

import com.ishland.c2me.base.common.metrics.ChunkLoadingSnapshot;
import com.ishland.c2me.base.common.metrics.MetricsConfig;
import com.ishland.c2me.base.common.metrics.RingBuffer;

/**
 * Client-side state for chunk loading metrics
 */
public class MetricsClientState {

    private static MetricsClientState INSTANCE;

    private boolean enabled = false;
    private RingBuffer historyBuffer;
    private int historySize;
    private ChunkLoadingSnapshot latestSnapshot = ChunkLoadingSnapshot.EMPTY;

    private MetricsClientState() {
        applyConfig();
    }

    public static MetricsClientState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetricsClientState();
        }
        return INSTANCE;
    }

    public void updateMetrics(ChunkLoadingSnapshot snapshot) {
        this.latestSnapshot = snapshot;
        // Store a simple metric for history (avg chunk load time)
        this.historyBuffer.add(snapshot.avgChunkLoadTimeMs());
    }

    public ChunkLoadingSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }

    public RingBuffer getHistoryBuffer() {
        return historyBuffer;
    }

    public void applyConfig() {
        int newSize = Math.max(1, MetricsConfig.clientHistorySize);
        if (historyBuffer == null || historySize != newSize) {
            historyBuffer = new RingBuffer(newSize);
            historySize = newSize;
        }
        this.enabled = MetricsConfig.clientHudEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}