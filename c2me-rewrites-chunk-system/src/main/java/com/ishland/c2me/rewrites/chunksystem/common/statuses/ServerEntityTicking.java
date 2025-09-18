package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

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
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        if (ModStatuses.fabric_lifecycle_events_v1 && LifecycleEventInvoker.needsInvokeChunkLevelTypeChange()) {
            return Completable.fromRunnable(() -> {
                        Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                            ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                            final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
                            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, chunk, ChunkLevelType.BLOCK_TICKING, ChunkLevelType.ENTITY_TICKING);
                        }
                    })
                    .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
        }
        return Completable.complete();
    }

    @Override
    public Completable downgradeFromThis(ChunkLoadingContext context) {
        if (ModStatuses.fabric_lifecycle_events_v1 && LifecycleEventInvoker.needsInvokeChunkLevelTypeChange()) {
            return Completable
                    .fromRunnable(() -> {
                        Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, false))) {
                            ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                            final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
                            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, chunk, ChunkLevelType.ENTITY_TICKING, ChunkLevelType.BLOCK_TICKING);
                        }
                    })
                    .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
        }
        return Completable.complete();
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return relativeToAbsoluteDependencies(holder, deps);
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public String toString() {
        return "Entity Ticking";
    }
}
