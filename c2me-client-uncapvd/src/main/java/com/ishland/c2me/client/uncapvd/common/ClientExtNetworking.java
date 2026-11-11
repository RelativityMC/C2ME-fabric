/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
