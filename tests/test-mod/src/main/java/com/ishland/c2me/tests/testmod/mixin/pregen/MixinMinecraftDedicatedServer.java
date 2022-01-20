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
