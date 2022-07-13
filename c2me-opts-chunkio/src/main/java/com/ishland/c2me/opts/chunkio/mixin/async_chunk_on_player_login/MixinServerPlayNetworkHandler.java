package com.ishland.c2me.opts.chunkio.mixin.async_chunk_on_player_login;

import com.ishland.c2me.opts.chunkio.common.async_chunk_on_player_login.IAsyncChunkPlayer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V"))
    private void suppressUpdatePositionDuringChunkLoad(ServerPlayerEntity instance, double x, double y, double z, float yaw, float pitch) {
        if (((IAsyncChunkPlayer) instance).isChunkLoadCompleted()) {
            instance.updatePositionAndAngles(x, y, z, yaw, pitch);
        }
    }

}
