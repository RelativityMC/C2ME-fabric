package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ServerBlockTicking extends NewChunkStatus {

    public ServerBlockTicking(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
        return CompletableFuture.runAsync(() -> {
            final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
            chunk.runPostProcessing();
            ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().disableTickSchedulers(chunk);
            CompletableFuture<?> completableFuturexx = context.holder().getUserData().get().getPostProcessingFuture();
            if (completableFuturexx.isDone()) {
                ((IThreadedAnvilChunkStorage) context.tacs()).invokeSendToPlayers(chunk);
            } else {
                completableFuturexx.thenAcceptAsync(v -> ((IThreadedAnvilChunkStorage) context.tacs()).invokeSendToPlayers(chunk), ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
            }
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
        return CompletableFuture.completedStage(null);
    }
}
