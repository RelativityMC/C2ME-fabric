package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.notickvd.ModuleEntryPoint;
import net.fabricmc.api.ModInitializer;

public class NoTickVDInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModuleEntryPoint.enabled && Config.enableExtRenderDistanceProtocol && ModStatuses.fabric_networking_api_v1) {
            ServerExtNetworking.registerListeners();
        }
    }
}
