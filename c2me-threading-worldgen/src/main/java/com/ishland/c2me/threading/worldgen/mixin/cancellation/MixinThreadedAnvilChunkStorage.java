package com.ishland.c2me.threading.worldgen.mixin.cancellation;

import com.ishland.c2me.base.common.util.SneakyThrow;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow public abstract CompletableFuture<OptionalChunk<Chunk>> getChunk(ChunkHolder holder, ChunkStatus requiredStatus);

    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "getChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkLevels;getLevelFromStatus(Lnet/minecraft/world/chunk/ChunkStatus;)I"))
    private int redirectAddLightTicketDistance(ChunkStatus status) {
        return status == ChunkStatus.LIGHT ? ChunkLevels.getLevelFromStatus(ChunkStatus.STRUCTURE_STARTS) - 2 : ChunkLevels.getLevelFromStatus(status);
    }

    @Redirect(method = "method_20443", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkLevels;getLevelFromStatus(Lnet/minecraft/world/chunk/ChunkStatus;)I"))
    private int redirectRemoveLightTicketDistance(ChunkStatus status) {
        return status == ChunkStatus.LIGHT ? ChunkLevels.getLevelFromStatus(ChunkStatus.STRUCTURE_STARTS) - 2 : ChunkLevels.getLevelFromStatus(status);
    }

    @ModifyReturnValue(method = "getChunk", at = @At("RETURN"))
    private CompletableFuture<OptionalChunk<Chunk>> injectCancellationHook(CompletableFuture<OptionalChunk<Chunk>> originalReturn, ChunkHolder holder, ChunkStatus requiredStatus) {
        return originalReturn.thenCompose(either -> {
            if (!either.isPresent()) {
                return CompletableFuture.supplyAsync(() -> {
                    if (ChunkLevels.getStatus(holder.getLevel()).isAtLeast(requiredStatus)) {
//                        LOGGER.info("Chunk load {} raced, recovering", holder.getPos());
                        return this.getChunk(holder, requiredStatus); // recover from cancellation
                    } else {
                        return CompletableFuture.completedFuture(either);
                    }
                }, this.mainThreadExecutor).thenCompose(Function.identity());
            } else {
                return CompletableFuture.completedFuture(either);
            }
        });
    }

    @ModifyReturnValue(method = "method_17224", at = @At("RETURN"))
    private CompletionStage<OptionalChunk<Chunk>> handleGenCancellation(CompletionStage<OptionalChunk<Chunk>> original) {
        return original.exceptionally(throwable -> {
            Throwable actual = throwable;
            while (actual instanceof CompletionException) actual = actual.getCause();
            if (actual instanceof CancellationException) {
                return ChunkHolder.UNLOADED_CHUNK;
            } else {
                SneakyThrow.sneaky(throwable);
                return null; // unreachable
            }
        });
    }

}
