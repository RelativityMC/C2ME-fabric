package com.ishland.c2me.base.common.config;

import net.fabricmc.loader.api.FabricLoader;

public class ModStatuses {

    public static final boolean fabric_networking_api_v1 = FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1");
    public static final boolean fabric_lifecycle_events_v1 = FabricLoader.getInstance().isModLoaded("fabric-lifecycle-events-v1");

}
