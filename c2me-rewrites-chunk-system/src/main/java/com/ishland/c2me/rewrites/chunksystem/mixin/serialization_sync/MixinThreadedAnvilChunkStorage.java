package com.ishland.c2me.rewrites.chunksystem.mixin.serialization_sync;

import com.ishland.c2me.base.mixin.access.IChunkHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow protected abstract @Nullable ChunkHolder getCurrentChunkHolder(long pos);

    // it is the responsibility of the caller to make sure the chunk is accessible or in a safe state
    @WrapOperation(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;setNbt(Lnet/minecraft/util/math/ChunkPos;Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> submitSavingFuture(ServerChunkLoadingManager instance, ChunkPos chunkPos, Supplier<NbtCompound> supplier, Operation<CompletableFuture<Void>> original) {
        CompletableFuture<Void> ret = original.call(instance, chunkPos, supplier);
        ChunkHolder holder = this.getCurrentChunkHolder(chunkPos.toLong());
        if (holder != null) {
            ((IChunkHolder) holder).invokeCombineSavingFuture(ret);
        }
        return ret;
    }

}
