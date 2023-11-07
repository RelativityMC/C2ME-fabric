package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.scheduler.ThreadLocalWorldGenSchedulingState;
import com.ishland.c2me.threading.worldgen.common.Config;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
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
    @Nullable
    protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;

    @Shadow protected abstract CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> getRegion(ChunkHolder chunkHolder, int margin, IntFunction<ChunkStatus> distanceToStatus);

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

    @Redirect(method = "upgradeChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getRegion(Lnet/minecraft/server/world/ChunkHolder;ILjava/util/function/IntFunction;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> redirectGetRegion(ThreadedAnvilChunkStorage instance, ChunkHolder chunkHolder, int margin, IntFunction<ChunkStatus> distanceToStatus) {
        if (instance != (Object) this) throw new IllegalStateException();
        return chunkHolder.getChunkAt(distanceToStatus.apply(0), (ThreadedAnvilChunkStorage) (Object) this)
                .thenComposeAsync(unused -> this.getRegion(chunkHolder, margin, distanceToStatus), r -> {
                    if (Config.asyncScheduling) {
                        if (this.mainThreadExecutor.isOnThread()) {
                            ((IVanillaChunkManager) this).c2me$getSchedulingManager().enqueue(chunkHolder.getPos().toLong(), r);
                        } else {
                            r.run();
                        }
                    } else {
                        this.mainThreadExecutor.execute(r);
                    }
                });
    }

    @Redirect(method = "getRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getCurrentChunkHolder(J)Lnet/minecraft/server/world/ChunkHolder;"))
    private ChunkHolder redirectGetChunkHolder(ThreadedAnvilChunkStorage instance, long pos) {
        return this.chunkHolders.get(pos); // thread-safe
    }

}
