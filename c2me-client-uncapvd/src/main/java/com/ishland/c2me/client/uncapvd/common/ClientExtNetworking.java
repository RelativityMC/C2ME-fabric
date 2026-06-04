/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.client.uncapvd.common;

import com.ishland.c2me.base.common.network.ExtRenderDistance;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ServerboundPlayChannelEvents;
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
        if (catchExceptions(() -> ClientPlayNetworking.canSend(ExtRenderDistance.ID))) {
            LOGGER.info("Changing view distance to {} (play stage)", viewDistance);
            ClientPlayNetworking.send(new ExtRenderDistance(viewDistance));
            return;
        }
        if (catchExceptions(() -> ClientConfigurationNetworking.canSend(ExtRenderDistance.ID))) {
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
        ExtRenderDistance.init();
        ServerboundPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            if (channels.contains(ExtRenderDistance.ID.id())) {
                if (Config.enableExtRenderDistanceProtocol) {
                    LOGGER.info("Joined server with {} support", ExtRenderDistance.ID.id());
                    MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().options.sendClientSettings());
                } else {
                    LOGGER.info("Server supports {} but it is disabled in config", ExtRenderDistance.ID.id());
                }
            }
        });
    }

}
