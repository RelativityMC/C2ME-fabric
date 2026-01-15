package com.ishland.c2me.base;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.base.common.metrics.MetricsConfig;
import com.ishland.c2me.base.common.network.ChunkLoadingMetricsPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class C2MEBaseMod implements PreLaunchEntrypoint, ModInitializer {
    @Override
    public void onPreLaunch() {
        ConfigSystem.flushConfig();
        MetricsConfig.init();
    }

    @Override
    public void onInitialize() {
        // Register network payload
        PayloadTypeRegistry.playS2C().register(ChunkLoadingMetricsPayload.ID, ChunkLoadingMetricsPayload.CODEC);
    }
}
