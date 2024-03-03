package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.network.ExtRenderDistance;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerExtNetworking {

    public static void registerListeners() {
        ExtRenderDistance.init();
        ServerPlayNetworking.registerGlobalReceiver(
                ExtRenderDistance.ID,
                (payload, context) ->
                        ((IRenderDistanceOverride) context.player().networkHandler).c2me_notickvd$setRenderDistance(payload.renderDistance())
        );
        ServerConfigurationNetworking.registerGlobalReceiver(
                ExtRenderDistance.ID,
                (payload, context) ->
                        ((IRenderDistanceOverride) context.networkHandler()).c2me_notickvd$setRenderDistance(payload.renderDistance())
        );
    }

}
