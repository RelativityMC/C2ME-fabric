package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.PlayerNoTickDistanceMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager {

    private PlayerNoTickDistanceMap playerNoTickDistanceMap;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        playerNoTickDistanceMap = new PlayerNoTickDistanceMap((ChunkTicketManager) (Object) this, C2MEConfig.noTickViewDistanceConfig.viewDistance);
    }

    @Inject(method = "handleChunkEnter", at = @At("HEAD"))
    private void onHandleChunkEnter(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        playerNoTickDistanceMap.addSource(pos.toChunkPos());
    }

    @Inject(method = "handleChunkLeave", at = @At("HEAD"))
    private void onHandleChunkLeave(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        playerNoTickDistanceMap.removeSource(pos.toChunkPos());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(ThreadedAnvilChunkStorage threadedAnvilChunkStorage, CallbackInfoReturnable<Boolean> info) {
        playerNoTickDistanceMap.update(threadedAnvilChunkStorage);
    }

}
