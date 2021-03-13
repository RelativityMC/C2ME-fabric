package org.yatopiamc.c2me.mixin.chunkscheduling.mid_tick_chunk_tasks;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.chunkscheduling.ServerMidTickTask;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Shadow @Final private ServerWorld world;

    @Dynamic
    @Inject(method = "method_20801", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V"))
    private void onPostTickChunk(CallbackInfo ci) { // TODO synthetic method - in tickChunks()
        ((ServerMidTickTask) this.world.getServer()).executeTasksMidTick();
    }

}
