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

package com.ishland.c2me.base.mixin.exp_warning;

import net.minecraft.server.Main;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinDedicatedServerMain {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;<init>(Ljava/nio/file/Path;)V"))
    private static void earlyInit(String[] args, CallbackInfo ci) {
        LOGGER.warn("Be careful! You are running a experimental version of C2ME");
        LOGGER.warn("This version include features that are still under development. Your world might crash, break. Use at your own risk!");
        if (!Boolean.getBoolean("com.ishland.c2me.base.skipWarningWait")) {
            while (true) {
                LOGGER.warn("The server will start in 10 seconds...");
                try {
                    Thread.sleep(10000);
                    break;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
    }

}
