package com.ishland.c2me.client.uncapvd.common;

import com.ishland.c2me.base.common.network.ExtRenderDistance;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BooleanSupplier;

public class ClientExtNetworking {

    public static final Logger LOGGER = LoggerFactory.getLogger("C2ME ClientExtNetworking");

    public static void sendViewDistance(int viewDistance) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            new IllegalStateException("Tried to send packet not on client thread!").printStackTrace();
            return;
        }
        if (catchExceptions(() -> ClientPlayNetworking.canSend(ExtRenderDistance.TYPE))) {
            LOGGER.info("Changing view distance to {} (play stage)", viewDistance);
            ClientPlayNetworking.send(new ExtRenderDistance(viewDistance));
            return;
        }
        if (catchExceptions(() -> ClientConfigurationNetworking.canSend(ExtRenderDistance.TYPE))) {
            LOGGER.info("Changing view distance to {} (config stage)", viewDistance);
            ClientConfigurationNetworking.send(new ExtRenderDistance(viewDistance));
            return;
        }
    }

    private static boolean catchExceptions(BooleanSupplier supplier) {
        try {
            return supplier.getAsBoolean();
        } catch (IllegalStateException e) {
            return false; // not connected
        }
    }

    public static void registerListeners() {
        C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            if (channels.contains(ExtRenderDistance.TYPE.getId())) {
                if (Config.enableExtRenderDistanceProtocol) {
                    LOGGER.info("Joined server with {} support", ExtRenderDistance.TYPE.getId());
                    MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().options.sendClientSettings());
                } else {
                    LOGGER.info("Server supports {} but it is disabled in config", ExtRenderDistance.TYPE.getId());
                }
            }
        });
    }

}
