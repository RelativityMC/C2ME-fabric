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

package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetAddress;

@Mixin(MinecraftDedicatedServer.class)
public class MixinMinecraftDedicatedServer {

    @Shadow
    @Final
    static Logger LOGGER;

    /**
     * @author ishland
     * @reason stop watchdog
     */
    @Overwrite
    public long getMaxTickTime() {
        return 0;
    }

    @Redirect(method = "setupServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerNetworkIo;bind(Ljava/net/InetAddress;I)V"))
    private void redirectNetworkBind(ServerNetworkIo serverNetworkIo, InetAddress address, int port) {
        LOGGER.info("Not actually binding ports");
    }

}
