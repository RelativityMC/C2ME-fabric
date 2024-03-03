package com.ishland.c2me.base.common.config;

import net.fabricmc.loader.api.FabricLoader;

public class ModStatuses {

    public static final boolean fabric_networking_api_v1 = FabricLoader.getInstance().isModLoaded("fabric-networking-api-v1");

}
