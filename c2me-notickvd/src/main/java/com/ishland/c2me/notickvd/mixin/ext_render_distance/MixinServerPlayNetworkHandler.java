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
