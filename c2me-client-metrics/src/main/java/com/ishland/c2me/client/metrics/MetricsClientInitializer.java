package com.ishland.c2me.client.metrics;

import com.ishland.c2me.base.common.metrics.MetricsConfig;
import com.ishland.c2me.base.common.network.ChunkLoadingMetricsPayload;
import com.ishland.c2me.client.metrics.common.MetricsClientState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsClientInitializer implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Client Metrics");

    @Override
    public void onInitializeClient() {
        MetricsConfig.reload();
        if (MetricsConfig.enabled) {
            LOGGER.info("Initializing C2ME client metrics");
        }

        // Register network receiver
        ClientPlayNetworking.registerGlobalReceiver(ChunkLoadingMetricsPayload.ID, (payload, context) -> {
            if (!MetricsConfig.enabled) {
                return;
            }
            context.client().execute(() -> {
                MetricsClientState.getInstance().updateMetrics(payload.snapshot());
            });
        });
    }

}
