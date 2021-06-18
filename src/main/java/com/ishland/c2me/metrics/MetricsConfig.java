package com.ishland.c2me.metrics;

import java.util.UUID;

public class MetricsConfig {

    final String _notice1 = "bStats (https://bStats.org) collects some basic information for plugin authors, like how";
    final String _notice2 = "many people use their plugin and their total player count. It's recommended to keep bStats";
    final String _notice3 = "enabled, but if you're not comfortable with this, you can turn this setting off. There is no";
    final String _notice4 = "performance penalty associated with having metrics enabled, and data sent to bStats is fully";
    final String _notice5 = "anonymous.";
    final String _notice6 = "C2ME-fabric bStats page: https://bstats.org/plugin/bukkit/C2ME-fabric/10514";
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
