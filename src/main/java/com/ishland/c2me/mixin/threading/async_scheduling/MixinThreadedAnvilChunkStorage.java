package com.ishland.c2me.mixin.threading.async_scheduling;

import com.ibm.asyncutil.util.StageSupport;
import com.ishland.c2me.common.GlobalExecutors;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ServerWorld world;

    @Shadow protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> upgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus);

    @Inject(method = "upgradeChunk", at = @At(value = "HEAD"), cancellable = true)
    private void beforeUpgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        if (this.world.getServer().getThread() == Thread.currentThread()) {
            cir.setReturnValue(
                    StageSupport.tryComposeWith(GlobalExecutors.schedulingLock.acquireLock(), unused ->
                            CompletableFuture.supplyAsync(() -> this.upgradeChunk(holder, requiredStatus), GlobalExecutors.executor)
                    ).thenCompose(Function.identity()).toCompletableFuture()
            );
        }
    }

}
