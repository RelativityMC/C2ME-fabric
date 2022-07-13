package com.ishland.c2me.opts.scheduling.mixin.ordering.player_move;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Shadow public ServerPlayerEntity player;

//    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.BEFORE))
//    private void movePacketBeforePlayerMove(CallbackInfo ci) {
//        this.player.getWorld().getChunkManager().updatePosition(this.player);
//    }
//
//    @Redirect(
//            method = "onPlayerMove",
//            slice = @Slice(
//                    from = @At(
//                            value = "INVOKE",
//                            target = "Lnet/minecraft/server/network/ServerPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
//                    )
//            ),
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/server/world/ServerChunkManager;updatePosition(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
//            )
//    )
//    private void movePacketAvoidDoubleUpdate(ServerChunkManager instance, ServerPlayerEntity player) {
//        // no-op
//    }

}
