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

package com.ishland.c2me.notickvd.mixin.ext_render_distance;

import com.ishland.c2me.base.mixin.access.ISyncedClientOptions;
import com.ishland.c2me.notickvd.common.IRenderDistanceOverride;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler implements IRenderDistanceOverride {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private boolean c2me_notickvd$hasRenderDistanceOverride = false;

    @Override
    public void c2me_notickvd$setRenderDistance(int renderDistance) {
        this.c2me_notickvd$hasRenderDistanceOverride = true;
        final SyncedClientOptions clientOptions = this.player.getClientOptions();
        if (clientOptions != null) {
            ((ISyncedClientOptions) (Object) clientOptions).setViewDistance(renderDistance);
            this.player.setClientOptions(clientOptions);
        }
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            System.out.println(String.format("%s render distance has changed to %d", this.player.getName().getString(), clientOptions.viewDistance()));
        }
    }

    @WrapOperation(method = "onClientOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setClientOptions(Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;)V"))
    private void interceptClientOptions(ServerPlayerEntity instance, SyncedClientOptions clientOptions, Operation<Void> original) {
        if (c2me_notickvd$hasRenderDistanceOverride) {
            ((ISyncedClientOptions) (Object) clientOptions).setViewDistance(instance.getClientOptions().viewDistance()); // keep the original view distance
        }
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            System.out.println(String.format("%s render distance has changed to %d", instance.getName().getString(), clientOptions.viewDistance()));
        }
        original.call(instance, clientOptions);
    }

}
