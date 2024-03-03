package com.ishland.c2me.fixes.general.threading_issues.mixin;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow
    @Final
    public static CompletableFuture<OptionalChunk<Chunk>> UNLOADED_CHUNK_FUTURE;
    @Shadow
    private int level;

    @Shadow
    @Final
    private AtomicReferenceArray<CompletableFuture<OptionalChunk<Chunk>>> futuresByStatus;
    @Shadow private CompletableFuture<Chunk> savingFuture;
    @Unique
    private Object schedulingMutex = new Object();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.schedulingMutex = new Object();
    }

    /**
     * @author ishland
     * @reason improve handling of async chunk request
     */
    @Overwrite
    public CompletableFuture<OptionalChunk<Chunk>> getChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage) {
        // TODO [VanillaCopy]
        int i = targetStatus.getIndex();
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.futuresByStatus.get(i);
        if (completableFuture != null) {
            OptionalChunk<Chunk> either = completableFuture.getNow(null);
            boolean bl = either != null && !either.isPresent();
            if (!bl) {
                return completableFuture;
            }
        }

        CompletableFuture<OptionalChunk<Chunk>> future;

        synchronized (this.schedulingMutex) {
            // copied from above
            completableFuture = this.futuresByStatus.get(i);
            if (completableFuture != null) {
                OptionalChunk<Chunk> either = completableFuture.getNow(null);
                boolean bl = either != null && !either.isPresent();
                if (!bl) {
                    return completableFuture;
                }
            }
            if (ChunkLevels.getStatus(this.level).isAtLeast(targetStatus)) {
                future = new CompletableFuture<>();
                this.futuresByStatus.set(i, future);
                // C2ME - moved down to prevent deadlock
            } else {
                return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
            }
        }

        CompletableFuture<OptionalChunk<Chunk>> completableFuture2 = chunkStorage.getChunk((ChunkHolder) (Object) this, targetStatus);
        // synchronization: see below
        synchronized (this) {
            this.combineSavingFuture(completableFuture2, "schedule " + targetStatus);
        }
        completableFuture2.whenComplete((either, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(either);
        });
        this.futuresByStatus.set(i, completableFuture2);
        return completableFuture2;
    }

    /**
     * @author ishland
     * @reason synchronize
     */
    @Overwrite
    public void combineSavingFuture(String thenDesc, CompletableFuture<?> then) {
        synchronized (this) {
            this.savingFuture = this.savingFuture.thenCombine(then, (result, thenResult) -> result);
        }
    }

    /**
     * @author ishland
     * @reason synchronize
     */
    @Overwrite
    public void combineSavingFuture(CompletableFuture<? extends OptionalChunk<? extends Chunk>> then, String thenDesc) {
        synchronized (this) {
            this.savingFuture = this.savingFuture.thenCombine(then, (chunk, otherChunk) -> OptionalChunk.orElse(otherChunk, chunk));
        }
    }

}
