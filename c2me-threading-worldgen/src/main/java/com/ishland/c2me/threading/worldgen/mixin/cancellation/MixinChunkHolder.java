package com.ishland.c2me.threading.worldgen.mixin.cancellation;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow
    @Final
    private AtomicReferenceArray<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> futuresByStatus;

    @Shadow public abstract void flushUpdates(WorldChunk chunk);

    @Shadow @Final public static Either<Chunk, ChunkHolder.Unloaded> UNLOADED_CHUNK;

//    @Redirect(method = "updateChunks", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicReferenceArray;get(I)Ljava/lang/Object;"))
//    private <E> E captureWorldGenCancellation(AtomicReferenceArray<E> instance, int i, ThreadedAnvilChunkStorage chunkStorage, Executor executor) {
//        if (instance == this.futuresByStatus) {
//            final CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future = this.futuresByStatus.get(i);
//            if (future == null) return null;
//            future.complete(UNLOADED_CHUNK);
//            return (E) future;
//        } else return instance.get(i);
//    }

}
