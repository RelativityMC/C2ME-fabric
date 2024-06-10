package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import net.minecraft.world.chunk.ChunkStatus;

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
        return null;
    }
}
