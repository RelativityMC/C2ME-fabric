package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.IChunkSystemAccess;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;runTasksTillTickEnd()V"))
    private void onTaskWait(CallbackInfo ci, @Share("c2me:shutdownLastPrint") LocalLongRef lastPrint) {
        long now = System.nanoTime();
        if (now - lastPrint.get() > 2_000_000_000L) {
            for (ServerWorld world : this.getWorlds()) {
                final int itemCount = ((IChunkSystemAccess) world.getChunkManager().chunkLoadingManager).c2me$getTheChunkSystem().itemCount();
                if (itemCount > 0) {
                    LOGGER.info("{}/{}: waiting for {} chunks to unload", world, world.getRegistryKey().getValue(), itemCount);
                }
            }
            lastPrint.set(now);
        }
    }

}
