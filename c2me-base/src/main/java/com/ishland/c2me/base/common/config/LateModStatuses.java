package com.ishland.c2me.base.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LateModStatuses {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModStatuses.class);
    public static final boolean fabric_lifecycle_events_v1_CHUNK_LEVEL_TYPE_CHANGE = isAvailable0_fabric_lifecycle_events_v1_CHUNK_LEVEL_TYPE_CHANGE();

    private static boolean isAvailable0_fabric_lifecycle_events_v1_CHUNK_LEVEL_TYPE_CHANGE() {
        if (!ModStatuses.fabric_lifecycle_events_v1) {
            LOGGER.info("Fabric Lifecycle Events (v1) not found, skipping compat");
            return false;
        }
        Class<?> clazzServerChunkEvents;
        try {
            clazzServerChunkEvents = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents not found, but fabric-loader claims fabric-lifecycle-events-v1 is present, outdated fabric-api?", e);
            return false;
        }
        try {
            clazzServerChunkEvents.getField("CHUNK_LEVEL_TYPE_CHANGE");
        } catch (NoSuchFieldException e) {
            LOGGER.warn("Lnet/fabricmc/fabric/api/event/lifecycle/v1/ServerChunkEvents;CHUNK_LEVEL_TYPE_CHANGE:Lnet/fabricmc/fabric/api/event/Event; not found, outdated fabric-api?", e);
            return false;
        }

        return true;
    }

}
