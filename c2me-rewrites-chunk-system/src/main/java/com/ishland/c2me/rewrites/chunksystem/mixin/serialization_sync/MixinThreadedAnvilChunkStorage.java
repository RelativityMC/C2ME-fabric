package com.ishland.c2me.rewrites.chunksystem.mixin.serialization_sync;

import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow protected abstract @Nullable ChunkHolder getCurrentChunkHolder(long pos);

    // it is the responsibility of the caller to make sure the chunk is accessible or in a safe state
    @ModifyVariable(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;setNbt(Lnet/minecraft/util/math/ChunkPos;Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> submitSavingFuture(CompletableFuture<Void> future, Chunk chunk) {
        ChunkHolder holder = this.getCurrentChunkHolder(chunk.getPos().toLong());
        if (holder instanceof NewChunkHolderVanillaInterface vif) {
            vif.getBackingHolder().submitOp(future);
        }
        return future;
    }

}
