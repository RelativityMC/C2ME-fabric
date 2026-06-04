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

import com.ibm.asyncutil.util.Either;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.common.registry.SerializerAccess;
import com.ishland.c2me.base.common.theinterface.IDirectStorage;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.common.util.RxJavaUtils;
import com.ishland.c2me.base.mixin.access.IServerLightingProvider;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.base.mixin.access.IWorldChunk;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.AsyncSerializationUtil;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.BlendingInfoUtil;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ProtoChunkExtension;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.IPOIUnloading;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadFromDisk extends NewChunkStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFromDisk");

    public ReadFromDisk(int ordinal) {
        super(ordinal, ChunkStatus.EMPTY);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        final Single<ProtoChunk> single = invokeVanillaLoad(context)
                .retryWhen(RxJavaUtils.retryWithExponentialBackoff(5, 100));
        return finalizeLoading(context, single);
    }

    protected @NotNull Completable finalizeLoading(ChunkLoadingContext context, Single<ProtoChunk> single) {
        return single
                .doOnError(throwable -> {
                    MinecraftServer server = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().getServer();
                    server.execute(() -> server.onChunkLoadFailure(throwable, ((IVersionedChunkStorage) context.tacs()).invokeGetStorageKey(), context.holder().getKey()));
                })
                .onErrorResumeNext(throwable -> {
                    if (Config.recoverFromErrors) {
                        return Single.just(createEmptyProtoChunk(context));
                    } else {
                        return Single.error(throwable);
                    }
                })
                .doOnSuccess(chunk -> {
                    context.holder().getItem().set(new ChunkState(chunk, chunk, ChunkStatus.EMPTY, chunk instanceof WrapperProtoChunk));
                    if (!Config.lowMemoryMode) {
                        context.holder().getUserData().get().triggerDeferredLoad(NewChunkStatus.DISK);
                    }
                })
                .ignoreElement()
                .cache();
    }

    protected @NonNull Single<ProtoChunk> invokeVanillaLoad(ChunkLoadingContext context) {
        return invokeInitialChunkRead(context)
                .observeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()))
                .map(chunkSerializer -> {
                    Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                        if (chunkSerializer.isPresent()) {
                            return chunkSerializer.get().convert(
                                    ((IThreadedAnvilChunkStorage) context.tacs()).getWorld(),
                                    ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage(),
                                    ((IVersionedChunkStorage) context.tacs()).invokeGetStorageKey(),
                                    context.holder().getKey()
                            );
                        } else {
                            return createEmptyProtoChunk(context);
                        }
                    }
                })
                .flatMap(protoChunk -> postChunkLoading(context, protoChunk).toSingleDefault(protoChunk));
    }

    protected @NotNull Completable postChunkLoading(ChunkLoadingContext context, ProtoChunk protoChunk) {
        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
            final ServerWorld world = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
            // blending
            final ChunkPos pos = context.holder().getKey();
            protoChunk = protoChunk != null ? protoChunk : new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA, world, world.getPalettesFactory(), null);
            if (protoChunk.getBelowZeroRetrogen() != null || protoChunk.getStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk finalProtoChunk = protoChunk;
                return Single.defer(() -> Single.fromCompletionStage(BlendingInfoUtil.getBlendingInfos(((IVersionedChunkStorage) context.tacs()).getWorker(), pos)))
                        .doOnSuccess(bitSets -> ((ProtoChunkExtension) finalProtoChunk).setBlendingInfo(pos, bitSets))
                        .ignoreElement();
            } else {
                return Completable.complete();
            }
        }
    }

    protected @NotNull Single<Optional<SerializedChunk>> invokeInitialChunkRead(ChunkLoadingContext context) {
        return Single.defer(() -> Single.fromCompletionStage(((IThreadedAnvilChunkStorage) context.tacs()).invokeGetUpdatedChunkNbt(context.holder().getKey())))
                .map(optional -> optional.map(nbtCompound -> {
                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                        ServerWorld world = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                        SerializedChunk chunkSerializer = SerializedChunk.fromNbt(world, world.getPalettesFactory(), nbtCompound);
                        if (chunkSerializer == null) {
                            LOGGER.error("Chunk file at {} is missing level data, skipping", context.holder().getKey());
                        }

                        return chunkSerializer;
                    }
                }))
                .zipWith(
                        Completable.defer(() -> Completable.fromCompletionStage(((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage().load(context.holder().getKey()))).toSingleDefault(ReadFromDisk.class),
                        (chunkSerializer, o) -> chunkSerializer
                );
    }

    protected @NotNull ProtoChunk createEmptyProtoChunk(ChunkLoadingContext context) {
        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
            final ServerWorld world = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
            return new ProtoChunk(context.holder().getKey(), UpgradeData.NO_UPGRADE_DATA, world, world.getPalettesFactory(), null);
        }
    }

    @Override
    public Completable postUpgradeToThis(ChunkLoadingContext context) {
        return Completable.complete();
    }

    @Override
    public Completable preDowngradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.defer(() -> Completable.fromCompletionStage(syncWithLightEngine(context)))
                .observeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()))
                .andThen((CompletableObserver observer) -> {
                    if (context.holder().getTargetStatus().ordinal() > this.ordinal()) { // saving cancelled
                        cancellable.cancel();
                        observer.onError(new CancellationException());
                    } else {
                        observer.onComplete();
                    }
                });
    }

    @Override
    public Completable downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        final AtomicBoolean loadedToWorld = new AtomicBoolean(false);
        return Completable.defer(() -> {
                    Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, false))) {
                        final ChunkState chunkState = context.holder().getItem().get();
                        Chunk chunk = chunkState.chunk();
                        if (chunk instanceof WrapperProtoChunk protoChunk) chunk = protoChunk.getWrappedChunk();

                        if (chunk instanceof WorldChunk worldChunk) {
                            loadedToWorld.set(((IWorldChunk) worldChunk).isLoadedToWorld());
                            worldChunk.setLoadedToWorld(false);
                        }

                        if (loadedToWorld.get() && ModStatuses.fabric_lifecycle_events_v1 && chunk instanceof WorldChunk worldChunk) {
                            LifecycleEventInvoker.invokeChunkUnload(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), worldChunk);
                        }

                        Completable asyncSaveFuture;
                        if ((context.holder().getFlags() & ItemHolder.FLAG_BROKEN) != 0 && chunk instanceof ProtoChunk) { // do not save broken ProtoChunks
                            LOGGER.warn("Not saving partially generated broken chunk {}", context.holder().getKey());
                            asyncSaveFuture = Completable.complete();
                        } else if (chunk instanceof WorldChunk && !chunkState.reachedStatus().isAtLeast(ChunkStatus.FULL)) {
                            // do not save WorldChunks that doesn't reach full status: Vanilla behavior
                            // If saved, block entities will be lost
                            asyncSaveFuture = Completable.complete();
                        } else {
                            asyncSaveFuture = asyncSave(context, chunk);
                        }

                        if (loadedToWorld.get() && chunk instanceof WorldChunk worldChunk) {
                            ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().unloadEntities(worldChunk);
                        }

                        ((IServerLightingProvider) ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider()).invokeUpdateChunkStatus(chunk.getPos());
                        ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider().tick();
                        ((IThreadedAnvilChunkStorage) context.tacs()).getChunkToNextSaveTimeMs().remove(chunk.getPos().toLong());

                        ((IPOIUnloading) ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage()).c2me$unloadPoi(context.holder().getKey());

                        context.holder().getItem().set(new ChunkState(null, null, null, false));

                        return asyncSaveFuture;
                    }
        });
    }

    private Completable asyncSave(ChunkLoadingContext context, Chunk chunk) {
        ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage().saveChunk(chunk.getPos());
        if (!chunk.tryMarkSaved()) {
            return Completable.complete();
        } else {
            ChunkPos chunkPos = chunk.getPos();

            SerializedChunk serializer;
            if (Config.asyncSerialization) {
                serializer = ScopedValue
                        .where(AsyncSerializationUtil.duringUnloadSerialization, null)
                        .call(() -> SerializedChunk.fromChunk(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), chunk));
            } else {
                serializer = SerializedChunk.fromChunk(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), chunk);
            }
            return Single
                    .<Either<NbtCompound, byte[]>>fromCallable(() -> {
                        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, false))) {
                            return SerializerAccess.getSerializer().serialize(serializer);
                        }
                    })
                    .subscribeOn(Schedulers.from(GlobalExecutors.prioritizedScheduler.executor(16) /* boost priority as we are serializing an unloaded chunk */))
                    .flatMapCompletable(either -> {
                        if (either.left().isPresent()) {
                            NbtCompound nbtCompound = either.left().get();
                            return Completable.fromCompletionStage(context.tacs().set(chunkPos, () -> nbtCompound));
                        } else {
                            return Completable.fromCompletionStage(((IDirectStorage) ((IVersionedChunkStorage) context.tacs()).getWorker()).setRawChunkData(chunkPos, either.right().get()));
                        }
                    });
        }
    }

    protected CompletionStage<?> syncWithLightEngine(ChunkLoadingContext context) {
        return ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider().enqueue(
                context.holder().getKey().x(),
                context.holder().getKey().z()
        );
    }

    @Override
    public String toString() {
        return "minecraft:empty";
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }
}
