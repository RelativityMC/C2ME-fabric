package org.yatopiamc.c2me.mixin.threading.worldgen;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ServerWorld world;
    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    private final Executor mainInvokingExecutor = runnable -> {
        if (this.world.getServer().isOnThread()) {
            runnable.run();
        } else {
            this.mainThreadExecutor.execute(runnable);
        }
    };

    private final ThreadLocal<ChunkStatus> capturedRequiredStatus = new ThreadLocal<>();

    @Inject(method = "upgradeChunk", at = @At("HEAD"))
    private void onUpgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        capturedRequiredStatus.set(requiredStatus);
    }

    @Redirect(method = "makeChunkEntitiesTickable", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private <U, T> CompletableFuture<U> redirectMainThreadExecutor1(CompletableFuture<T> completableFuture, Function<? super T, ? extends U> fn, Executor executor) {
        return completableFuture.thenApplyAsync(fn, this.mainInvokingExecutor);
    }

    @Redirect(method = "getChunk", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenComposeAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private <T, U> CompletableFuture<U> redirectMainThreadExecutor2(CompletableFuture<T> completableFuture, Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return completableFuture.thenComposeAsync(fn, this.mainInvokingExecutor);
    }

    /**
     * @author ishland
     * @reason move to scheduler & improve chunk status transition speed
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_17259(ChunkHolder chunkHolder, Runnable runnable) { // synthetic method for worldGenExecutor scheduling in upgradeChunk
        final ChunkStatus capturedStatus = capturedRequiredStatus.get();
        capturedRequiredStatus.remove();
        if (capturedStatus != null) {
            final Chunk currentChunk = chunkHolder.getCurrentChunk();
            if (currentChunk != null && currentChunk.getStatus().isAtLeast(capturedStatus)) {
                this.mainInvokingExecutor.execute(runnable);
                return;
            }
        }
        GlobalExecutors.scheduler.execute(runnable);
    }

}
