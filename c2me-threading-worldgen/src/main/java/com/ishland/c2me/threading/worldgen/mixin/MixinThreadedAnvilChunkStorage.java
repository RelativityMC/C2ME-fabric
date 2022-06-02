package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.common.scheduler.ThreadLocalWorldGenSchedulingState;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    /**
     * @author ishland
     * @reason reduce scheduling overhead
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_17259(ChunkHolder chunkHolder, Runnable runnable) { // synthetic method for worldGenExecutor scheduling in upgradeChunk
        runnable.run();
    }

    @Inject(method = "method_17225", at = @At("HEAD"))
    private void captureUpgradingChunkHolder(ChunkPos chunkPos, ChunkHolder chunkHolder, ChunkStatus chunkStatus, Executor executor, List<Chunk> chunks, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        ThreadLocalWorldGenSchedulingState.setChunkHolder(chunkHolder);
    }

    @Inject(method = "method_17225", at = @At("RETURN"))
    private void resetUpgradingChunkHolder(CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        ThreadLocalWorldGenSchedulingState.clearChunkHolder();
    }

    @Inject(method = "method_17225", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;", shift = At.Shift.BEFORE))
    private void resetUpgradingChunkHolderExceptionally(CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        ThreadLocalWorldGenSchedulingState.clearChunkHolder();
    }


}
