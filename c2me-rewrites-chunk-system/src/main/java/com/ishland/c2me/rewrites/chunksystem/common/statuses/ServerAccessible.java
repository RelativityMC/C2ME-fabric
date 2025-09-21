package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.common.config.LateModStatuses;
import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IWorldChunk;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.compat.lithium.LithiumChunkStatusTrackerInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerAccessible extends NewChunkStatus {

    public ServerAccessible(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        final Chunk chunk = context.holder().getItem().get().chunk();
        Preconditions.checkState(chunk instanceof ProtoChunk, "Chunk must be a proto chunk");
        ProtoChunk protoChunk = (ProtoChunk) chunk;

        return Completable
                .fromRunnable(() -> {
                    Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());
                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                        ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                        final WorldChunk worldChunk = toFullChunk(protoChunk, serverWorld);

                        worldChunk.setLevelTypeProvider(context.holder().getUserData().get()::getLevelType);
                        context.holder().getItem().set(new ChunkState(worldChunk, new WrapperProtoChunk(worldChunk, false), ChunkStatus.FULL));
                        if (!((IWorldChunk) worldChunk).isLoadedToWorld()) {
                            worldChunk.loadEntities();
                            worldChunk.setLoadedToWorld(true);
                            worldChunk.updateAllBlockEntities();
                            worldChunk.addChunkTickSchedulers(serverWorld);
                            if (ModStatuses.fabric_lifecycle_events_v1) {
                                LifecycleEventInvoker.invokeChunkLoaded(serverWorld, worldChunk, !(protoChunk instanceof WrapperProtoChunk));
                            }
                        }

                        if (LateModStatuses.fabric_lifecycle_events_v1_CHUNK_LEVEL_TYPE_CHANGE) {
                            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, worldChunk, ChunkLevelType.INACCESSIBLE, ChunkLevelType.FULL);
                        }
                        ((IThreadedAnvilChunkStorage) context.tacs()).getCurrentChunkHolders().put(context.holder().getKey().toLong(), context.holder().getUserData().get());
                        ((IThreadedAnvilChunkStorage) context.tacs()).setChunkHolderListDirty(true);
                    }
                })
                .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
    }

    private static WorldChunk toFullChunk(ProtoChunk protoChunk, ServerWorld serverWorld) {
        WorldChunk worldChunk;
        if (protoChunk instanceof WrapperProtoChunk) {
            worldChunk = ((WrapperProtoChunk) protoChunk).getWrappedChunk();
        } else {
            worldChunk = new WorldChunk(serverWorld, protoChunk, worldChunkx -> {
                final List<NbtCompound> entities = protoChunk.getEntities();
                if (!entities.isEmpty()) {
                    serverWorld.addEntities(EntityType.streamFromNbt(entities, serverWorld));
                }
            });
        }
        return worldChunk;
    }

    @Override
    public Completable downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        ChunkState state = context.holder().getItem().get();
        final Chunk chunk = state.chunk();
        Preconditions.checkState(chunk instanceof WorldChunk, "Chunk must be a full chunk");
        return Completable
                .fromRunnable(() -> {
                    Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());
                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, false))) {
                        ((IThreadedAnvilChunkStorage) context.tacs()).getCurrentChunkHolders().remove(context.holder().getKey().toLong());
                        ((IThreadedAnvilChunkStorage) context.tacs()).setChunkHolderListDirty(true);
                        final WorldChunk worldChunk = (WorldChunk) chunk;
                        ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
        //                worldChunk.setLoadedToWorld(false);
        //                worldChunk.removeChunkTickSchedulers(((IThreadedAnvilChunkStorage) context.tacs()).getWorld());
                       if (LateModStatuses.fabric_lifecycle_events_v1_CHUNK_LEVEL_TYPE_CHANGE) {
                        LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, worldChunk, ChunkLevelType.FULL, ChunkLevelType.INACCESSIBLE);
                    }
                        LithiumChunkStatusTrackerInvoker.invokeOnChunkInaccessible(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), context.holder().getKey());worldChunk.setLevelTypeProvider(null);context.holder().getItem().set(new ChunkState(state.protoChunk(), state.protoChunk(), ChunkStatus.FULL));
                    }
                })
                .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
    }

    @Override
    public String toString() {
        return "minecraft:full, Border";
    }
}
