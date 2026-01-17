package com.ishland.c2me.client.metrics.common;

import com.ishland.c2me.base.common.metrics.ChunkLoadingSnapshot;
import com.ishland.c2me.base.common.metrics.MetricsConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Client-side state for chunk loading metrics
 */
public class MetricsClientState {

    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Metrics Logger");
    private static MetricsClientState INSTANCE;
    private ChunkLoadingSnapshot latestSnapshot = ChunkLoadingSnapshot.EMPTY;
    private Path logPath;
    private boolean headerWritten = false;

    private MetricsClientState() {
        try {
            Path c2meDir = FabricLoader.getInstance().getConfigDir().resolve("c2me");
            Files.createDirectories(c2meDir);
            this.logPath = c2meDir.resolve("c2me-metrics.csv");
        } catch (IOException e) {
            LOGGER.error("Failed to create C2ME log directory", e);
            // Fallback to old location
            this.logPath = FabricLoader.getInstance().getConfigDir().resolve("c2me-metrics.csv");
        }
    }

    public static MetricsClientState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetricsClientState();
        }
        return INSTANCE;
    }

    public void updateMetrics(ChunkLoadingSnapshot snapshot) {
        this.latestSnapshot = snapshot;
        if (MetricsConfig.fileLoggingEnabled) {
            logToFile(snapshot);
        }
    }

    private void logToFile(ChunkLoadingSnapshot snapshot) {
        try {
            boolean isNewFile = !Files.exists(logPath);
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(logPath, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
                
                if (isNewFile || !headerWritten) {
                    writer.println("Timestamp,AvgChunkLoadTime,MaxChunkLoadTime,IOBacklog,IOPending,SchedulerQueue,SchedulerActiveThreads");
                    headerWritten = true;
                }
                
                writer.printf("%s,%.2f,%.2f,%d,%d,%d,%d\n",
                        Instant.now().toString(),
                        snapshot.avgChunkLoadTimeMs(),
                        snapshot.maxChunkLoadTimeMs(),
                        snapshot.currentIoBacklogSize(),
                        snapshot.currentIoPendingWrites(),
                        snapshot.currentSchedulerQueueSize(),
                        snapshot.currentSchedulerActiveThreads()
                );
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write metrics to file: {}", logPath, e);
        }
    }

    public ChunkLoadingSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }

}
