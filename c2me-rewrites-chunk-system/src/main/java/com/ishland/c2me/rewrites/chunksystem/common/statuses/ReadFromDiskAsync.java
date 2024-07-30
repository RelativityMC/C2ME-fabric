package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.common.registry.SerializerAccess;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.theinterface.IDirectStorage;
import com.ishland.c2me.base.common.util.SneakyThrow;
import com.ishland.c2me.base.mixin.access.ISerializingRegionBasedStorage;
import com.ishland.c2me.base.mixin.access.IServerLightingProvider;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.base.mixin.access.IWorldChunk;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.AsyncSerializationManager;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.BlendingInfoUtil;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ChunkIoMainThreadTaskUtils;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.Config;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ProtoChunkExtension;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.SerializingRegionBasedStorageExtension;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ReadFromDiskAsync extends ReadFromDisk {

    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFromDiskAsync");

    public ReadFromDiskAsync(int ordinal) {
        super(ordinal);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
        final CompletableFuture<Optional<NbtCompound>> poiData =
                ((ISerializingRegionBasedStorage) ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage()).getStorageAccess().read(context.holder().getKey())
                        .exceptionally(throwable -> {
                            //noinspection IfStatementWithIdenticalBranches
                            if (Config.recoverFromErrors) {
                                LOGGER.error("Couldn't load poi data for chunk {}, poi data will be lost!", context.holder().getKey(), throwable);
                                return Optional.empty();
                            } else {
                                SneakyThrow.sneaky(throwable);
                                return Optional.empty(); // unreachable
                            }
                        });

        final ReferenceArrayList<Runnable> mainThreadQueue = new ReferenceArrayList<>();

        final CompletableFuture<Chunk> future = ((IThreadedAnvilChunkStorage) context.tacs()).invokeGetUpdatedChunkNbt(context.holder().getKey())
                .thenApply(optional -> optional.filter(nbtCompound -> {
                    boolean bl = nbtCompound.contains("Status", NbtElement.STRING_TYPE);
                    if (!bl) {
                        LOGGER.error("Chunk file at {} is missing level data, skipping", context.holder().getKey());
                    }

                    return bl;
                }))
                .thenApplyAsync(optional -> {
                    if (optional.isPresent()) {
                        ChunkIoMainThreadTaskUtils.push(mainThreadQueue);
                        try {
                            return ChunkSerializer.deserialize(
                                    ((IThreadedAnvilChunkStorage) context.tacs()).getWorld(),
                                    ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage(),
                                    ((IVersionedChunkStorage) context.tacs()).invokeGetStorageKey(),
                                    context.holder().getKey(),
                                    optional.get()
                            );
                        } finally {
                            ChunkIoMainThreadTaskUtils.pop(mainThreadQueue);
                        }
                    }

                    return null;
                }, ((IVanillaChunkManager) context.tacs()).c2me$getSchedulingManager().positionedExecutor(context.holder().getKey().toLong()))
//                .exceptionally(throwable -> {
//                    //noinspection IfStatementWithIdenticalBranches
//                    if (Config.recoverFromErrors) {
//                        LOGGER.error("Couldn't load chunk {}, chunk data will be lost!", context.holder().getKey(), throwable);
//                        return null;
//                    } else {
//                        SneakyThrow.sneaky(throwable);
//                        return null; // unreachable
//                    }
//                })
//                .thenCombine(poiData, (protoChunk, tag) -> protoChunk)
//                .thenCombine(blendingInfos, (protoChunk, bitSet) -> {
//                    if (protoChunk != null) ((ProtoChunkExtension) protoChunk).setBlendingInfo(pos, bitSet);
//                    return protoChunk;
//                })
                .thenCompose(protoChunk -> {
                    final ServerWorld world = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                    // blending
                    final ChunkPos pos = context.holder().getKey();
                    protoChunk = protoChunk != null ? protoChunk : new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA, world, world.getRegistryManager().get(RegistryKeys.BIOME), null);
                    if (protoChunk.getBelowZeroRetrogen() != null || protoChunk.getStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                        final CompletionStage<List<BitSet>> blendingInfos = BlendingInfoUtil.getBlendingInfos(((IVersionedChunkStorage) context.tacs()).getWorker(), pos);
                        ProtoChunk finalProtoChunk = protoChunk;
                        final CompletableFuture<Void> blendingFuture = blendingInfos.thenAccept(bitSet -> ((ProtoChunkExtension) finalProtoChunk).setBlendingInfo(pos, bitSet)).toCompletableFuture();
                        ProtoChunk finalProtoChunk1 = protoChunk;
                        return blendingFuture.thenApply(unused -> finalProtoChunk1);
                    } else {
                        return CompletableFuture.completedFuture(protoChunk);
                    }
                })
                .thenApply(protoChunk -> {
                    final ChunkPos pos = context.holder().getKey();
                    ((ProtoChunkExtension) protoChunk).setInitialMainThreadComputeFuture(poiData.thenAcceptAsync(poiDataNbt -> {
                        try {
                            ((SerializingRegionBasedStorageExtension) ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage()).update(pos, poiDataNbt.orElse(null));
                        } catch (Throwable t) {
                            LOGGER.error("Couldn't load poi data for chunk {}, poi data will be lost!", pos, t);
                        }
                        ChunkIoMainThreadTaskUtils.drainQueue(mainThreadQueue);
                    }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));

                    return protoChunk;
                });
        future.exceptionally(throwable -> {
            LOGGER.error("Couldn't load chunk {}", context.holder().getKey(), throwable);
            return null;
        });
        return future.thenAccept(chunk -> context.holder().getItem().set(new ChunkState(chunk, ChunkStatus.EMPTY)));
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
        final AtomicBoolean loadedToWorld = new AtomicBoolean(false);
        return CompletableFuture.supplyAsync(() -> {
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

                    if ((context.holder().getFlags() & ItemHolder.FLAG_BROKEN) != 0 && chunk instanceof ProtoChunk) { // do not save broken ProtoChunks
                        LOGGER.warn("Not saving partially generated broken chunk {}", context.holder().getKey());
                        return CompletableFuture.completedStage((Void) null);
                    } else if (chunk instanceof WorldChunk && !chunkState.reachedStatus().isAtLeast(ChunkStatus.FULL)) {
                        // do not save WorldChunks that doesn't reach full status: Vanilla behavior
                        // If saved, block entities will be lost
                        return CompletableFuture.completedStage((Void) null);
                    } else {
                        return asyncSave(context.tacs(), chunk);
                    }
                }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor())
                .thenCompose(Function.identity())
                .thenAcceptAsync(unused -> {
                    Chunk chunk = context.holder().getItem().get().chunk();
                    if (chunk instanceof WrapperProtoChunk protoChunk) chunk = protoChunk.getWrappedChunk();
                    if (loadedToWorld.get() && chunk instanceof WorldChunk worldChunk) {
                        ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().unloadEntities(worldChunk);
                    }

                    ((IServerLightingProvider) ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider()).invokeUpdateChunkStatus(chunk.getPos());
                    ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider().tick();
                    ((IThreadedAnvilChunkStorage) context.tacs()).getWorldGenerationProgressListener().setChunkStatus(chunk.getPos(), null);
                    ((IThreadedAnvilChunkStorage) context.tacs()).getChunkToNextSaveTimeMs().remove(chunk.getPos().toLong());

                    context.holder().getItem().set(new ChunkState(null, null));
                }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }

    private CompletionStage<Void> asyncSave(ServerChunkLoadingManager tacs, Chunk chunk) {
        ((IThreadedAnvilChunkStorage) tacs).getPointOfInterestStorage().saveChunk(chunk.getPos());
        if (!chunk.needsSaving()) {
            return CompletableFuture.completedStage(null);
        } else {
            chunk.setNeedsSaving(false);
            ChunkPos chunkPos = chunk.getPos();

            AsyncSerializationManager.Scope scope = new AsyncSerializationManager.Scope(chunk, ((IThreadedAnvilChunkStorage) tacs).getWorld());
            return CompletableFuture.supplyAsync(() -> {
                        scope.open();
                        AsyncSerializationManager.push(scope);
                        try {
                            return SerializerAccess.getSerializer().serialize(((IThreadedAnvilChunkStorage) tacs).getWorld(), chunk);
                        } finally {
                            AsyncSerializationManager.pop(scope);
                        }
                    }, GlobalExecutors.prioritizedScheduler.executor(16) /* boost priority as we are serializing an unloaded chunk */)
                    .thenAccept((either) -> {
                        if (either.left().isPresent()) {
                            tacs.setNbt(chunkPos, either.left().get());
                        } else {
                            ((IDirectStorage) ((IVersionedChunkStorage) tacs).getWorker()).setRawChunkData(chunkPos, either.right().get());
                        }
                    })
                    .exceptionallyCompose(throwable -> {
                        LOGGER.error("Failed to save chunk {},{} asynchronously, falling back to sync saving", chunkPos.x, chunkPos.z, throwable);
                        return CompletableFuture.runAsync(() -> {
                            chunk.setNeedsSaving(true);
                            ((IThreadedAnvilChunkStorage) tacs).invokeSave(chunk);
                        }, ((IThreadedAnvilChunkStorage) tacs).getMainThreadExecutor());
                    });
        }
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
