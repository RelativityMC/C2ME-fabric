/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IWorldChunk;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.compat.lithium.LithiumChunkStatusTrackerInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.WorldChunkExtension;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerAccessible extends NewChunkStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerAccessible.class);

    public ServerAccessible(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        final Chunk chunk = context.holder().getItem().get().chunk();
        Preconditions.checkState(chunk instanceof ProtoChunk, "Chunk must be a proto chunk");
        ProtoChunk protoChunk = (ProtoChunk) chunk;

        if (Config.asyncSerialization) {
            ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
            final WorldChunk worldChunk = toFullChunk(protoChunk, serverWorld);
            final WrapperProtoChunk wrapperProtoChunk = new WrapperProtoChunk(worldChunk, false);
            return Completable
                    .fromRunnable(() -> upgrade0(context, protoChunk, worldChunk, wrapperProtoChunk))
                    .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
        } else {
            return Completable
                    .fromRunnable(() -> {
                        ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                        final WorldChunk worldChunk = toFullChunk(protoChunk, serverWorld);
                        final WrapperProtoChunk wrapperProtoChunk = new WrapperProtoChunk(worldChunk, false);
                        upgrade0(context, protoChunk, worldChunk, wrapperProtoChunk);
                    })
                    .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
        }
    }

    private void upgrade0(ChunkLoadingContext context, ProtoChunk protoChunk, WorldChunk worldChunk, WrapperProtoChunk newProtoChunk) {
        Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
            worldChunk.setLevelTypeProvider(context.holder().getUserData().get()::getLevelType);
            worldChunk.setUnsavedListener(((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext().unsavedListener());
            ((WorldChunkExtension) worldChunk).c2me$setBlockTicking(false); // not necessary, but just in case
            boolean wasFullChunk = protoChunk instanceof WrapperProtoChunk;

            context.holder().getItem().set(new ChunkState(worldChunk, newProtoChunk, ChunkStatus.FULL, wasFullChunk));
            if (!Config.delayFullChunkEvents) {
                invokeFullChunkEvents(context, worldChunk, wasFullChunk);
            }
        }
    }

    @Override
    public Completable postUpgradeToThis(ChunkLoadingContext context) {
        if (!Config.delayFullChunkEvents) return Completable.complete();

        return Completable.fromRunnable(() -> {
            Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());
            ChunkState chunkState = context.holder().getItem().get();
            WorldChunk worldChunk = (WorldChunk) chunkState.chunk();
            invokeFullChunkEvents(context, worldChunk, chunkState.wasFullChunk());
        });
    }

    private static void invokeFullChunkEvents(ChunkLoadingContext context, WorldChunk worldChunk, boolean wasFullChunk) {
        ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
        if (!((IWorldChunk) worldChunk).isLoadedToWorld()) {
            worldChunk.loadEntities();
            worldChunk.setLoadedToWorld(true);
            worldChunk.updateAllBlockEntities();
            worldChunk.addChunkTickSchedulers(serverWorld);
            if (ModStatuses.fabric_lifecycle_events_v1) {
                LifecycleEventInvoker.invokeChunkLoaded(serverWorld, worldChunk, !wasFullChunk);
            }
        }

        if (ModStatuses.fabric_lifecycle_events_v1) {
            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, worldChunk, ChunkLevelType.INACCESSIBLE, ChunkLevelType.FULL);
        }

        ((IThreadedAnvilChunkStorage) context.tacs()).getCurrentChunkHolders().put(context.holder().getKey().toLong(), context.holder().getUserData().get());
        ((IThreadedAnvilChunkStorage) context.tacs()).setChunkHolderListDirty(true);
    }

    private static WorldChunk toFullChunk(ProtoChunk protoChunk, ServerWorld serverWorld) {
        WorldChunk worldChunk;
        if (protoChunk instanceof WrapperProtoChunk) {
            worldChunk = ((WrapperProtoChunk) protoChunk).getWrappedChunk();
        } else {
            worldChunk = new WorldChunk(serverWorld, protoChunk, worldChunkx -> {
                try (ErrorReporter.Logging lv = new ErrorReporter.Logging(protoChunk.getErrorReporterContext(), LOGGER)) {
                    ReadView.ListReadView arg = NbtReadView.createList(lv, serverWorld.getRegistryManager(), protoChunk.getEntities());
                    if (!arg.isEmpty()) {
                        serverWorld.addEntities(EntityType.streamFromData(arg, serverWorld, SpawnReason.LOAD));
                    }
                }
            });
        }
        return worldChunk;
    }

    @Override
    public Completable preDowngradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
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

                        if (ModStatuses.fabric_lifecycle_events_v1) {
                            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, worldChunk, ChunkLevelType.FULL, ChunkLevelType.INACCESSIBLE);
                        }
                        LithiumChunkStatusTrackerInvoker.invokeOnChunkInaccessible(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), context.holder().getKey());

                        worldChunk.setLevelTypeProvider(null);
                        worldChunk.setUnsavedListener(pos -> {
                        });
                        context.holder().getItem().set(new ChunkState(state.protoChunk(), state.protoChunk(), ChunkStatus.FULL, true));
                    }
                })
                .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
    }

    @Override
    public String toString() {
        return "minecraft:full, Border";
    }
}
