package com.ishland.c2me.base.common.metrics;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class MetricsConfig {

    public static boolean enabled;
    public static int maxHistorySize;
    public static int broadcastIntervalMs;
    public static boolean clientHudEnabled;
    public static int clientHistorySize;

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

        clientHudEnabled = new ConfigSystem.ConfigAccessor()
                .key("metrics.client.hudEnabled")
                .comment("""
                        Whether to show the client-side metrics HUD
                        """)
                .getBoolean(true, true);

        clientHistorySize = Math.toIntExact(
                new ConfigSystem.ConfigAccessor()
                        .key("metrics.client.historySize")
                        .comment("""
                                Number of samples to keep in the client HUD history
                                """)
                        .getLong(120, 120, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY)
        );

        ConfigSystem.flushConfig();
    }

    public static void save() {
        ConfigSystem.set("metrics.enabled", enabled);
        ConfigSystem.set("metrics.maxHistorySize", maxHistorySize);
        ConfigSystem.set("metrics.broadcastIntervalMs", broadcastIntervalMs);
        ConfigSystem.set("metrics.client.hudEnabled", clientHudEnabled);
        ConfigSystem.set("metrics.client.historySize", clientHistorySize);
        ConfigSystem.flushConfig();
    }

    public static void init() {
        // intentionally empty
    }

}
