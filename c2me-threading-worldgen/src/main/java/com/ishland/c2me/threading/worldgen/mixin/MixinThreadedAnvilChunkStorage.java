package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.common.scheduler.ThreadLocalWorldGenSchedulingState;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow
    protected abstract CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> getRegion(ChunkPos centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus);

    @Shadow
    @Nullable
    protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

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

    @Redirect(method = "upgradeChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getRegion(Lnet/minecraft/util/math/ChunkPos;ILjava/util/function/IntFunction;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> redirectGetRegion(ThreadedAnvilChunkStorage instance, ChunkPos centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus) {
        if (instance != (Object) this) throw new IllegalStateException();
        return this.getChunkHolder(centerChunk.toLong()).getChunkAt(distanceToStatus.apply(0), (ThreadedAnvilChunkStorage) (Object) this)
                .thenComposeAsync(unused -> this.getRegion(centerChunk, margin, distanceToStatus), this.mainThreadExecutor);
    }

}
