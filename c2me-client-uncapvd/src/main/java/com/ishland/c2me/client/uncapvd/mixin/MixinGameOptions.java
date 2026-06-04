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
