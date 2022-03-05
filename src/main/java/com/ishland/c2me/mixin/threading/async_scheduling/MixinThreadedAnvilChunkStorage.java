package com.ishland.c2me.mixin.threading.async_scheduling;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ServerWorld world;

    @Shadow protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> upgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus);

    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;

    @Inject(method = "upgradeChunk", at = @At(value = "HEAD"), cancellable = true)
    private void beforeUpgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        if (this.world.getServer().getThread() == Thread.currentThread()) {
            cir.setReturnValue(
                    CompletableFuture.supplyAsync(() -> this.upgradeChunk(holder, requiredStatus), GlobalExecutors.executor).thenCompose(Function.identity())
            );
        }
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "upgradeChunk", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getRegion(Lnet/minecraft/util/math/ChunkPos;ILjava/util/function/IntFunction;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> asyncUpgradeChunkTask(CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> value) {
        return value.thenApplyAsync(Function.identity(), runnable -> {
            if (this.world.getServer().getThread() == Thread.currentThread()) {
                GlobalExecutors.executor.execute(runnable);
            } else {
                runnable.run();
            }
        });
    }

    @Redirect(method = "getRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getCurrentChunkHolder(J)Lnet/minecraft/server/world/ChunkHolder;"))
    private ChunkHolder redirectGetChunkHolder(ThreadedAnvilChunkStorage instance, long pos) {
        return this.chunkHolders.get(pos); // thread-safe
    }

}
