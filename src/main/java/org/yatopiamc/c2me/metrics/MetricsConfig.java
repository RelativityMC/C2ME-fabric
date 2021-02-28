package org.yatopiamc.c2me.metrics;

import java.util.UUID;

public class MetricsConfig {

    String _notice = "bStats (https://bStats.org) collects some basic information for plugin authors, like how\n"
            + "many people use their plugin and their total player count. It's recommended to keep bStats\n"
            + "enabled, but if you're not comfortable with this, you can turn this setting off. There is no\n"
            + "performance penalty associated with having metrics enabled, and data sent to bStats is fully\n"
            + "anonymous.";
    String serverUuid;
    boolean enabled;
    boolean logFailedRequests;
    boolean logSentData;
    boolean logResponseStatusText;

    public MetricsConfig() {
        serverUuid = UUID.randomUUID().toString();
        enabled = true;
        logFailedRequests = false;
        logSentData = false;
        logResponseStatusText = false;
    }

    public MetricsConfig(String serverUuid, boolean enabled, boolean logFailedRequests, boolean logSentData, boolean logResponseStatusText) {
        this.serverUuid = serverUuid;
        this.enabled = enabled;
        this.logFailedRequests = logFailedRequests;
        this.logSentData = logSentData;
        this.logResponseStatusText = logResponseStatusText;
    }
}
