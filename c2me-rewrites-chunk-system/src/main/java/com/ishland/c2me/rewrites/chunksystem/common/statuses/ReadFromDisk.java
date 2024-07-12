package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.mixin.access.IServerLightingProvider;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ReadFromDisk extends NewChunkStatus {
    public ReadFromDisk(int ordinal) {
        super(ordinal, ChunkStatus.EMPTY);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
        return CompletableFuture.supplyAsync(() -> {
            return ((IThreadedAnvilChunkStorage) context.tacs()).invokeLoadChunk(context.holder().getKey())
                    .thenAccept(chunk -> {
                        context.holder().getItem().set(new ChunkState(chunk));
                    });
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()).thenCompose(Function.identity());
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
        return CompletableFuture.runAsync(() -> {
            final Chunk chunk = context.holder().getItem().get().chunk();

            if (chunk instanceof WorldChunk worldChunk) {
                worldChunk.setLoadedToWorld(false);
            }

            ((IThreadedAnvilChunkStorage) context.tacs()).invokeSave(chunk);
            if (chunk instanceof WorldChunk worldChunk) {
                ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().unloadEntities(worldChunk);
            }

            ((IServerLightingProvider) ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider()).invokeUpdateChunkStatus(chunk.getPos());
            ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider().tick();
            ((IThreadedAnvilChunkStorage) context.tacs()).getWorldGenerationProgressListener().setChunkStatus(chunk.getPos(), null);
            ((IThreadedAnvilChunkStorage) context.tacs()).getChunkToNextSaveTimeMs().remove(chunk.getPos().toLong());
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }
}
