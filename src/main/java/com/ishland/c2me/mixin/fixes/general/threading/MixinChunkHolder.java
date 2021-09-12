package com.ishland.c2me.mixin.fixes.general.threading;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.util.StageSupport;
import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.threading.worldgen.WorldGenThreadingExecutorUtils;
import com.ishland.c2me.mixin.access.IThreadedAnvilChunkStorage;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow
    @Final
    public static CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> UNLOADED_CHUNK_FUTURE;

    @Shadow
    public static ChunkStatus getTargetStatusForLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    private int level;

    @Shadow
    protected abstract void combineSavingFuture(CompletableFuture<? extends Either<? extends Chunk, ChunkHolder.Unloaded>> then, String thenDesc);

    @Shadow
    @Final
    private AtomicReferenceArray<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> futuresByStatus;
    @Unique
    private AsyncLock schedulingLock = AsyncLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.schedulingLock = AsyncLock.createFair();
    }

    /**
     * @author ishland
     * @reason improve handling of async chunk request
     */
    @Overwrite
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> getChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage) {
        // TODO kind of [VanillaCopy]
        assert this.schedulingLock != null;
        int i = targetStatus.getIndex();
        CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = this.futuresByStatus.get(i);
        if (completableFuture != null) {
            Either<Chunk, ChunkHolder.Unloaded> either = completableFuture.getNow(null);
            boolean bl = either != null && either.right().isPresent();
            if (!bl) {
                return completableFuture;
            }
        }
        final boolean isOnThread = Thread.currentThread() == ((IThreadedAnvilChunkStorage) chunkStorage).getWorld().getServer().getThread();
        final Executor offThreadExecutor = command -> {
            if (isOnThread) {
                if (C2MEConfig.threadedWorldGenConfig.enabled) {
                    WorldGenThreadingExecutorUtils.mainExecutor.execute(command);
                    return;
                }
            }
            command.run();
        };
        return StageSupport.tryWith(this.schedulingLock.acquireLock().thenComposeAsync(CompletableFuture::completedFuture, offThreadExecutor), ignored -> {
            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture1 = this.futuresByStatus.get(i);
            if (completableFuture1 != null) {
                Either<Chunk, ChunkHolder.Unloaded> either = completableFuture1.getNow(null);
                boolean bl = either != null && either.right().isPresent();
                if (!bl) {
                    return completableFuture1;
                }
            }

            if (getTargetStatusForLevel(this.level).isAtLeast(targetStatus)) {
                CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture2 = chunkStorage.getChunk((ChunkHolder) (Object) this, targetStatus);
                // synchronization: see below
                synchronized (this) {
                    this.combineSavingFuture(completableFuture2, "schedule " + targetStatus);
                }
                this.futuresByStatus.set(i, completableFuture2);
                return completableFuture2;
            } else {
                return completableFuture1 == null ? UNLOADED_CHUNK_FUTURE : completableFuture1;
            }
        }).toCompletableFuture().thenCompose(Function.identity());
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;combineSavingFuture(Ljava/util/concurrent/CompletableFuture;Ljava/lang/String;)V"))
    private void synchronizeCombineSavingFuture(ChunkHolder holder, CompletableFuture<? extends Either<? extends Chunk, ChunkHolder.Unloaded>> then, String thenDesc) {
        synchronized (this) {
            this.combineSavingFuture(then, thenDesc);
        }
    }

}
