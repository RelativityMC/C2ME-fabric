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

package com.ishland.c2me.client.uncapvd.mixin;

import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.client.uncapvd.common.ClientExtNetworking;
import com.ishland.c2me.client.uncapvd.common.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions {

    @Shadow @Final private SimpleOption<Integer> viewDistance;

    @Shadow protected MinecraftClient client;

    @Shadow public abstract SyncedClientOptions getSyncedOptions();

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;load()V", shift = At.Shift.BEFORE))
    private void onInit(CallbackInfo ci) {
        final SimpleOption.ValidatingIntSliderCallbacks callbacks = new SimpleOption.ValidatingIntSliderCallbacks(2, Config.maxViewDistance);
        ((ISimpleOption<Integer>) this.viewDistance).setCallbacks(callbacks);
        ((ISimpleOption<Integer>) this.viewDistance).setCodec(callbacks.codec());
    }

    @Inject(method = "sendClientSettings", at = @At("HEAD"))
    private void beforeSendSettings(CallbackInfo ci) {
        if (!Config.enableExtRenderDistanceProtocol) return;
        if (!ModStatuses.fabric_networking_api_v1) return;

        ClientExtNetworking.sendViewDistance(this.getSyncedOptions().viewDistance());

    }

}
