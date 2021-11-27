package com.ishland.c2me.mixin.fixes.general.threading;

import com.ishland.c2me.mixin.access.IServerChunkManager;
import com.ishland.c2me.mixin.access.IThreadedAnvilChunkStorage;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
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
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

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
    @Shadow private CompletableFuture<Chunk> savingFuture;
    @Unique
    private ReentrantLock schedulingLock = new ReentrantLock();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.schedulingLock = new ReentrantLock();
    }

    /**
     * @author ishland
     * @reason improve handling of async chunk request
     */
    @Overwrite
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> getChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage) {
        // TODO [VanillaCopy]
        final ServerChunkManager chunkManager = ((IThreadedAnvilChunkStorage) chunkStorage).getWorld().getChunkManager();
        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
        schedulingLock.lock();
        try {
            int i = targetStatus.getIndex();
            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = this.futuresByStatus.get(i);
            if (completableFuture != null) {
                Either<Chunk, ChunkHolder.Unloaded> either = completableFuture.getNow(null);
                boolean bl = either != null && either.right().isPresent();
                if (!bl) {
                    return completableFuture;
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
                return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
            }
        } finally {
            schedulingLock.unlock();
        }
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;combineSavingFuture(Ljava/util/concurrent/CompletableFuture;Ljava/lang/String;)V"))
    private void synchronizeCombineSavingFuture(ChunkHolder holder, CompletableFuture<? extends Either<? extends Chunk, ChunkHolder.Unloaded>> then, String thenDesc) {
        synchronized (this) {
            this.combineSavingFuture(then, thenDesc);
        }
    }

    /**
     * @author ishland
     * @reason synchronize
     */
    @Overwrite
    public void method_39967(String string, CompletableFuture<?> completableFuture) {
        synchronized (this) {
            this.savingFuture = this.savingFuture.thenCombine(completableFuture, (chunk, object) -> chunk);
        }
    }

}
