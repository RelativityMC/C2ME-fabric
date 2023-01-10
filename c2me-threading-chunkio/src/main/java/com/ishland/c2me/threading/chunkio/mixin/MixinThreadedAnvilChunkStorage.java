package com.ishland.c2me.threading.chunkio.mixin;

import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.threading.chunkio.common.AsyncSerializationManager;
import com.ishland.c2me.threading.chunkio.common.BlendingInfoUtil;
import com.ishland.c2me.threading.chunkio.common.ChunkIoMainThreadTaskUtils;
import com.ishland.c2me.threading.chunkio.common.IAsyncChunkStorage;
import com.ishland.c2me.threading.chunkio.common.ISerializingRegionBasedStorage;
import com.ishland.c2me.threading.chunkio.common.ProtoChunkExtension;
import com.ishland.c2me.threading.chunkio.common.TaskCancellationException;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMaps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {

    public MixinThreadedAnvilChunkStorage(Path path, DataFixer dataFixer, boolean bl) {
        super(path, dataFixer, bl);
    }

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow
    @Final
    private PointOfInterestStorage pointOfInterestStorage;

    @Shadow
    protected abstract byte mark(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void markAsProtoChunk(ChunkPos chunkPos);

    @Shadow
    @Final
    private Supplier<PersistentStateManager> persistentStateManagerFactory;

    @Shadow
    @Final
    private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow
    protected abstract boolean isLevelChunk(ChunkPos chunkPos);

    @Shadow
    private ChunkGenerator chunkGenerator;

    @Shadow
    protected abstract boolean save(Chunk chunk);

    @Shadow
    protected abstract void save(boolean flush);

    @Shadow
    protected abstract CompletableFuture<Optional<NbtCompound>> getUpdatedChunkNbt(ChunkPos chunkPos);

    @Shadow
    private static boolean containsStatus(NbtCompound nbtCompound) {
        throw new AbstractMethodError();
    }

    @Shadow protected abstract Chunk getProtoChunk(ChunkPos chunkPos);

    @Mutable
    @Shadow @Final private Long2ByteMap chunkToType;
    private AsyncNamedLock<ChunkPos> chunkLock = AsyncNamedLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        chunkLock = AsyncNamedLock.createFair();
        this.chunkToType = Long2ByteMaps.synchronize(this.chunkToType);
    }

    private Set<ChunkPos> scheduledChunks = new HashSet<>();

    /**
     * @author ishland
     * @reason async io and deserialization
     */
    @Overwrite
    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> loadChunk(ChunkPos pos) {
        if (scheduledChunks == null) scheduledChunks = new HashSet<>();
        synchronized (scheduledChunks) {
            if (scheduledChunks.contains(pos)) throw new IllegalArgumentException("Already scheduled");
            scheduledChunks.add(pos);
        }

        final CompletableFuture<Optional<NbtCompound>> poiData = ((IAsyncChunkStorage) ((com.ishland.c2me.base.mixin.access.ISerializingRegionBasedStorage) this.pointOfInterestStorage).getWorker()).getNbtAtAsync(pos);

        final CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future = getUpdatedChunkNbtAtAsync(pos)
                .thenApply(optional -> optional.filter(nbtCompound -> {
                    boolean bl = containsStatus(nbtCompound);
                    if (!bl) {
                        LOGGER.error("Chunk file at {} is missing level data, skipping", pos);
                    }

                    return bl;
                }))
                .thenApplyAsync(optional -> {
                    if (optional.isPresent()) {
                        try {
                            ChunkIoMainThreadTaskUtils.push();
                            try {
                                return ChunkSerializer.deserialize(this.world, this.pointOfInterestStorage, pos, optional.get());
                            } finally {
                                ChunkIoMainThreadTaskUtils.pop();
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Couldn't load chunk {}, chunk data will be lost!", pos, t);
                        }
                    }

                    return null;
                }, GlobalExecutors.executor)
                .thenCombine(poiData, (protoChunk, tag) -> protoChunk)
//                .thenCombine(blendingInfos, (protoChunk, bitSet) -> {
//                    if (protoChunk != null) ((ProtoChunkExtension) protoChunk).setBlendingInfo(pos, bitSet);
//                    return protoChunk;
//                })
                .thenCompose(protoChunk -> {
                    ProtoChunk res = protoChunk != null ? protoChunk : (ProtoChunk) this.getProtoChunk(pos);
                    if (res.getBelowZeroRetrogen() != null || res.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
                        final CompletionStage<List<BitSet>> blendingInfos = BlendingInfoUtil.getBlendingInfos((StorageIoWorker) this.getWorker(), pos);
                        return blendingInfos.thenApply(bitSet -> {
                            ((ProtoChunkExtension) res).setBlendingInfo(pos, bitSet);
                            return res;
                        });
                    } else {
                        return CompletableFuture.completedFuture(res);
                    }
                })
                .thenApplyAsync(protoChunk -> {
                    ((ISerializingRegionBasedStorage) this.pointOfInterestStorage).update(pos, poiData.join().orElse(null));
                    ChunkIoMainThreadTaskUtils.drainQueue();
                    if (protoChunk != null) {
                        this.mark(pos, protoChunk.getStatus().getChunkType());
                        return Either.left(protoChunk);
                    } else {
                        LOGGER.error("Why is protoChunk null? Trying to recover from this case...");
                        return Either.left(this.getProtoChunk(pos));
                    }
                }, this.mainThreadExecutor);
        future.exceptionally(throwable -> null).thenRun(() -> {
            synchronized (scheduledChunks) {
                scheduledChunks.remove(pos);
            }
        });
        return future;

        // [VanillaCopy] - for reference
        /*
        return CompletableFuture.supplyAsync(() -> {
         try {
            this.world.getProfiler().visit("chunkLoad");
            CompoundTag compoundTag = this.getUpdatedChunkNbt(pos);
            if (compoundTag != null) {
               boolean bl = compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8);
               if (bl) {
                  Chunk chunk = ChunkSerializer.deserialize(this.world, this.structureManager, this.pointOfInterestStorage, pos, compoundTag);
                  this.method_27053(pos, chunk.getStatus().getChunkType());
                  return Either.left(chunk);
               }

               LOGGER.error((String)"Chunk file at {} is missing level data, skipping", (Object)pos);
            }
         } catch (CrashException var5) {
            Throwable throwable = var5.getCause();
            if (!(throwable instanceof IOException)) {
               this.method_27054(pos);
               throw var5;
            }

            LOGGER.error((String)"Couldn't load chunk {}", (Object)pos, (Object)throwable);
         } catch (Exception var6) {
            LOGGER.error((String)"Couldn't load chunk {}", (Object)pos, (Object)var6);
         }

         this.method_27054(pos);
         return Either.left(new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA, this.world));
      }, this.mainThreadExecutor);
         */
    }

    private CompletableFuture<Optional<NbtCompound>> getUpdatedChunkNbtAtAsync(ChunkPos pos) {
        return getUpdatedChunkNbt(pos);
    }

    private ConcurrentLinkedQueue<CompletableFuture<Void>> saveFutures = new ConcurrentLinkedQueue<>();

    @Dynamic
    @Redirect(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;save(Lnet/minecraft/world/chunk/Chunk;)Z"))
    // method: consumer in tryUnloadChunk
    private boolean asyncSave(ThreadedAnvilChunkStorage tacs, Chunk chunk, ChunkHolder holder) {
        // TODO [VanillaCopy] - check when updating minecraft version
        this.pointOfInterestStorage.saveChunk(chunk.getPos());
        if (!chunk.needsSaving()) {
            return false;
        } else {
            chunk.setNeedsSaving(false);
            ChunkPos chunkPos = chunk.getPos();

            try {
                ChunkStatus chunkStatus = chunk.getStatus();
                if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                    if (this.isLevelChunk(chunkPos)) {
                        return false;
                    }

                    if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                        return false;
                    }
                }

                final CompletableFuture<Chunk> originalSavingFuture = holder.getSavingFuture();
                if (!originalSavingFuture.isDone()) {
                    originalSavingFuture.handleAsync((_unused, __unused) -> asyncSave(tacs, chunk, holder), this.mainThreadExecutor);
                    return false;
                }

                this.world.getProfiler().visit("chunkSave");
                // C2ME start - async serialization
                if (saveFutures == null) saveFutures = new ConcurrentLinkedQueue<>();
                AsyncSerializationManager.Scope scope = new AsyncSerializationManager.Scope(chunk, world);

                saveFutures.add(chunkLock.acquireLock(chunk.getPos()).toCompletableFuture().thenCompose(lockToken ->
                        CompletableFuture.supplyAsync(() -> {
                                    scope.open();
                                    if (holder.getSavingFuture() != originalSavingFuture) {
                                        this.mainThreadExecutor.execute(() -> asyncSave(tacs, chunk, holder));
                                        throw new TaskCancellationException();
                                    }
                                    AsyncSerializationManager.push(scope);
                                    try {
                                        return ChunkSerializer.serialize(this.world, chunk);
                                    } finally {
                                        AsyncSerializationManager.pop(scope);
                                    }
                                }, GlobalExecutors.executor)
                                .thenAccept(compoundTag -> this.setNbt(chunkPos, compoundTag))
                                .handle((unused, throwable) -> {
                                    lockToken.releaseLock();
                                    if (throwable != null) {
                                        if (!(throwable instanceof TaskCancellationException)) {
                                            LOGGER.error("Failed to save chunk {},{} asynchronously, falling back to sync saving", chunkPos.x, chunkPos.z, throwable);
                                            final CompletableFuture<Chunk> savingFuture = holder.getSavingFuture();
                                            if (savingFuture != originalSavingFuture) {
                                                savingFuture.handleAsync((_unused, __unused) -> save(chunk), this.mainThreadExecutor);
                                            } else {
                                                this.mainThreadExecutor.execute(() -> this.save(chunk));
                                            }
                                        }
                                    }
                                    return unused;
                                })
                ));
                this.mark(chunkPos, chunkStatus.getChunkType());
                // C2ME end
                return true;
            } catch (Exception var5) {
                LOGGER.error((String) "Failed to save chunk {},{}", (Object) chunkPos.x, chunkPos.z, var5);
                return false;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        GlobalExecutors.executor.execute(() -> saveFutures.removeIf(CompletableFuture::isDone));
    }

    @Override
    public void completeAll() {
        final CompletableFuture<Void> future = CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]));
        this.mainThreadExecutor.runTasks(future::isDone); // wait for serialization to complete
        super.completeAll();
    }
}
