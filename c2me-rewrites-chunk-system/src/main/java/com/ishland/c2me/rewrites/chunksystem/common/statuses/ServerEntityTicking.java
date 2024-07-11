package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ServerEntityTicking extends NewChunkStatus {

    private static final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] deps;

    static {
        deps = new KeyStatusPair[] {
                new KeyStatusPair<>(new ChunkPos(-1, -1), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(-1, 0), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(-1, 1), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(0, -1), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(0, 1), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(1, -1), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(1, 0), NewChunkStatus.BLOCK_TICKING),
                new KeyStatusPair<>(new ChunkPos(1, 1), NewChunkStatus.BLOCK_TICKING),
        };
    }

    public ServerEntityTicking(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
        return CompletableFuture.completedStage(null);
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
        return CompletableFuture.completedStage(null);
    }

    @Override
    protected KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getRelativeDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return deps;
    }
}
