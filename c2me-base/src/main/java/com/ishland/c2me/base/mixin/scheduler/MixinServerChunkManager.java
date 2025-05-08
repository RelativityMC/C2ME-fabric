package com.ishland.c2me.base.mixin.scheduler;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

    @WrapOperation(method = "updateChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;update(Lnet/minecraft/server/world/ServerChunkLoadingManager;)Z"))
    private boolean consolidatePriorityUpdates(ChunkTicketManager instance, ServerChunkLoadingManager completablefuture, Operation<Boolean> original) {
        SchedulingManager schedulingManager = ((IVanillaChunkManager) this.chunkLoadingManager).c2me$getSchedulingManager();
        schedulingManager.setConsolidatingLevelUpdates(true);
        try {
            return original.call(instance, chunkLoadingManager);
        } finally {
            schedulingManager.setConsolidatingLevelUpdates(false);
        }
    }

}
