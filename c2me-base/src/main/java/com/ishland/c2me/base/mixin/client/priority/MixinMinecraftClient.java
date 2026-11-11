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

package com.ishland.c2me.base.mixin.client.priority;

import com.ishland.c2me.base.common.threadpriority.bindings.LinuxLibc;
import com.ishland.c2me.base.common.threadpriority.bindings.WindowsKernel32Extras;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.SystemInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "run()V", at = @At("HEAD"))
    private void onRun(CallbackInfo ci) {
        switch (SystemInfo.getCurrentPlatform()) {
            case LINUX, ANDROID -> {
                LinuxLibc.setCurrentThreadPreferMaximumPerformance();
            }
            case WINDOWS -> {
                WindowsKernel32Extras.disableCurrentThreadPowerThrottling();
            }
        }
    }

}
