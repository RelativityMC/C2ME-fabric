package com.ishland.c2me.threading.worldgen.mixin.cancellation;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Redirect(method = "getChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkStatus;getDistanceFromFull(Lnet/minecraft/world/chunk/ChunkStatus;)I"))
    private int redirectAddLightTicketDistance(ChunkStatus status) {
        return status == ChunkStatus.LIGHT ? ChunkStatus.getDistanceFromFull(ChunkStatus.STRUCTURE_STARTS) - 2 : ChunkStatus.getDistanceFromFull(status);
    }

    @Redirect(method = "method_20443", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkStatus;getDistanceFromFull(Lnet/minecraft/world/chunk/ChunkStatus;)I"))
    private int redirectRemoveLightTicketDistance(ChunkStatus status) {
        return status == ChunkStatus.LIGHT ? ChunkStatus.getDistanceFromFull(ChunkStatus.STRUCTURE_STARTS) - 2 : ChunkStatus.getDistanceFromFull(status);
    }

}
