package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.server.PlayerManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Shadow private int viewDistance;

    @Redirect(method = "onPlayerConnect", at = @At(value = "FIELD", target = "Lnet/minecraft/server/PlayerManager;viewDistance:I", opcode = Opcodes.GETFIELD))
    private int redirectViewDistance(PlayerManager playerManager) {
        return Math.max(this.viewDistance, C2MEConfig.noTickViewDistanceConfig.viewDistance);
    }

    @Redirect(method = "setViewDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    private void redirectSendAll(PlayerManager playerManager, Packet<?> packet) {
        if (packet instanceof ChunkLoadDistanceS2CPacket) return;
        playerManager.sendToAll(packet);
    }

}
