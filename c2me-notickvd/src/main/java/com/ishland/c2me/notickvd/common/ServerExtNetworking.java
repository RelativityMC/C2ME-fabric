package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.network.ExtRenderDistance;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerExtNetworking {

    public static void registerListeners() {
        ServerPlayNetworking.registerGlobalReceiver(
                ExtRenderDistance.TYPE,
                (packet, player, responseSender) ->
                        ((IRenderDistanceOverride) player.networkHandler).c2me_notickvd$setRenderDistance(packet.renderDistance())
        );
        ServerConfigurationNetworking.registerGlobalReceiver(
                ExtRenderDistance.TYPE,
                (packet, networkHandler, responseSender) ->
                        ((IRenderDistanceOverride) networkHandler).c2me_notickvd$setRenderDistance(packet.renderDistance())
        );
    }

}
