package com.ishland.c2me.base.common.metrics;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class MetricsConfig {

    public static boolean enabled;
    public static int maxHistorySize;
    public static int broadcastIntervalMs;
    public static boolean fileLoggingEnabled;

    static {
        reload();
    }

    public static void reload() {
        enabled = new ConfigSystem.ConfigAccessor()
                .key("metrics.enabled")
                .comment("""
                        Whether to enable metrics collection for chunk loading performance monitoring
                        """)
                .getBoolean(false, false);

        maxHistorySize = Math.toIntExact(
                new ConfigSystem.ConfigAccessor()
                        .key("metrics.maxHistorySize")
                        .comment("""
                                Maximum number of samples to keep in metrics history (affects memory usage)
                                """)
                        .getLong(1000, 1000, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY)
        );

        broadcastIntervalMs = Math.toIntExact(
                new ConfigSystem.ConfigAccessor()
                        .key("metrics.broadcastIntervalMs")
                        .comment("""
                                Metrics broadcast interval in milliseconds
                                """)
                        .getLong(500, 500, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY)
        );

        fileLoggingEnabled = new ConfigSystem.ConfigAccessor()
                .key("metrics.client.fileLoggingEnabled")
                .comment("""
                        Whether to log metrics to a file in the config directory
                        """)
                .getBoolean(false, false);

        ConfigSystem.flushConfig();
    }

    public static void save() {
        ConfigSystem.set("metrics.enabled", enabled);
        ConfigSystem.set("metrics.maxHistorySize", maxHistorySize);
        ConfigSystem.set("metrics.broadcastIntervalMs", broadcastIntervalMs);
        ConfigSystem.set("metrics.client.fileLoggingEnabled", fileLoggingEnabled);
        ConfigSystem.flushConfig();
    }

    public static void init() {
        // intentionally empty
    }

}
