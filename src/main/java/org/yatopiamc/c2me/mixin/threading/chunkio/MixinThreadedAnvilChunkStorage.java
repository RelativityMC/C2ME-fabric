package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoMainThreadTaskUtils;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedChunkTickScheduler;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedServerTickScheduler;
import org.yatopiamc.c2me.common.threading.chunkio.ISerializingRegionBasedStorage;
import org.yatopiamc.c2me.common.util.SneakyThrow;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {

    public MixinThreadedAnvilChunkStorage(File file, DataFixer dataFixer, boolean bl) {
        super(file, dataFixer, bl);
    }

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow
    @Final
    private StructureManager structureManager;

    @Shadow
    @Final
    private PointOfInterestStorage pointOfInterestStorage;

    @Shadow
    protected abstract byte method_27053(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void method_27054(ChunkPos chunkPos);

    @Shadow
    @Final
    private Supplier<PersistentStateManager> persistentStateManagerFactory;

    @Shadow
    @Final
    private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow
    protected abstract boolean method_27055(ChunkPos chunkPos);

    private AsyncNamedLock<ChunkPos> chunkLock = AsyncNamedLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        chunkLock = AsyncNamedLock.createFair();
    }

    /**
     * @author ishland
     * @reason async io and deserialization
     */
    @Overwrite
    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> loadChunk(ChunkPos pos) {

        final CompletableFuture<CompoundTag> poiData = ((C2MECachedRegionStorage) this.pointOfInterestStorage.worker).getNbtAtAsync(pos);

        return getUpdatedChunkTagAtAsync(pos).thenApplyAsync(compoundTag -> {
            if (compoundTag != null) {
                try {
                    if (compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8)) {
                        return ChunkSerializer.deserialize(this.world, this.structureManager, this.pointOfInterestStorage, pos, compoundTag);
                    }

                    LOGGER.warn("Chunk file at {} is missing level data, skipping", pos);
                } catch (Throwable t) {
                    LOGGER.error("Couldn't load chunk {}, chunk data will be lost!", pos, t);
                }
            }
            return null;
        }, ChunkIoThreadingExecutorUtils.serializerExecutor).thenCombine(poiData, (protoChunk, tag) -> protoChunk).thenApplyAsync(protoChunk -> {
            ((ISerializingRegionBasedStorage) this.pointOfInterestStorage).update(pos, poiData.join());
            ChunkIoMainThreadTaskUtils.drainQueue();
            if (protoChunk != null) {
                protoChunk.setLastSaveTime(this.world.getTime());
                this.method_27053(pos, protoChunk.getStatus().getChunkType());
                return Either.left(protoChunk);
            } else {
                this.method_27054(pos);
                return Either.left(new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA));
            }
        }, this.mainThreadExecutor);

        // [VanillaCopy] - for reference
        /*
        return CompletableFuture.supplyAsync(() -> {
            try {
                CompoundTag compoundTag = this.getUpdatedChunkTag(pos);
                if (compoundTag != null) {
                    boolean bl = compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8);
                    if (bl) {
                        Chunk chunk = ChunkSerializer.deserialize(this.world, this.structureManager, this.pointOfInterestStorage, pos, compoundTag);
                        chunk.setLastSaveTime(this.world.getTime());
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

                LOGGER.error("Couldn't load chunk {}", pos, throwable);
            } catch (Exception var6) {
                LOGGER.error("Couldn't load chunk {}", pos, var6);
            }

            this.method_27054(pos);
            return Either.left(new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA));
        }, this.mainThreadExecutor);
         */
    }

    private CompletableFuture<CompoundTag> getUpdatedChunkTagAtAsync(ChunkPos pos) {
        return chunkLock.acquireLock(pos).toCompletableFuture().thenCompose(lockToken -> ((C2MECachedRegionStorage) this.worker).getNbtAtAsync(pos).thenApply(compoundTag -> {
            if (compoundTag != null)
                return this.updateChunkTag(this.world.getRegistryKey(), this.persistentStateManagerFactory, compoundTag);
            else return null;
        }).handle((tag, throwable) -> {
            lockToken.releaseLock();
            if (throwable != null)
                SneakyThrow.sneaky(throwable);
            return tag;
        }));
    }

    private ConcurrentLinkedQueue<CompletableFuture<Void>> saveFutures = new ConcurrentLinkedQueue<>();

    /**
     * @author ishland
     * @reason async serialization
     */
    @Overwrite
    private boolean save(Chunk chunk) {
        // [VanillaCopy]
        this.pointOfInterestStorage.method_20436(chunk.getPos());
        if (!chunk.needsSaving()) {
            return false;
        } else {
            chunk.setLastSaveTime(this.world.getTime());
            chunk.setShouldSave(false);
            ChunkPos chunkPos = chunk.getPos();

            try {
                ChunkStatus chunkStatus = chunk.getStatus();
                if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.field_12807) {
                    if (this.method_27055(chunkPos)) {
                        return false;
                    }

                    if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                        return false;
                    }
                }

                this.world.getProfiler().visit("chunkSave");
                // C2ME start - async serialization
                if (saveFutures == null) saveFutures = new ConcurrentLinkedQueue<>();

                saveFutures.add(chunkLock.acquireLock(chunk.getPos()).toCompletableFuture().thenCompose(lockToken ->
                        CompletableFuture.runAsync(() -> {
                            final TickScheduler<Block> chunkBlockTickScheduler = chunk.getBlockTickScheduler();
                            final ServerTickScheduler<Block> worldBlockTickScheduler = this.world.getBlockTickScheduler();
                            if (chunkBlockTickScheduler instanceof ICachedChunkTickScheduler) {
                                ((ICachedChunkTickScheduler) chunkBlockTickScheduler).prepareCachedNbt();
                                ((ICachedChunkTickScheduler) chunkBlockTickScheduler).setFallbackExecutor(this.mainThreadExecutor);
                            } else if (worldBlockTickScheduler instanceof ICachedServerTickScheduler) {
                                ((ICachedServerTickScheduler) worldBlockTickScheduler).prepareCachedNbt(chunkPos);
                            } else {
                                new IllegalStateException("Unable to cache block ticklist. Incompatible mods?").printStackTrace();
                            }

                            final TickScheduler<Fluid> chunkFluidTickScheduler = chunk.getFluidTickScheduler();
                            final ServerTickScheduler<Fluid> worldFluidTickScheduler = this.world.getFluidTickScheduler();
                            if (chunkFluidTickScheduler instanceof ICachedChunkTickScheduler) {
                                ((ICachedChunkTickScheduler) chunkFluidTickScheduler).prepareCachedNbt();
                                ((ICachedChunkTickScheduler) chunkFluidTickScheduler).setFallbackExecutor(this.mainThreadExecutor);
                            } else if (worldFluidTickScheduler instanceof ICachedServerTickScheduler) {
                                ((ICachedServerTickScheduler) worldFluidTickScheduler).prepareCachedNbt(chunkPos);
                            } else {
                                new IllegalStateException("Unable to cache fluid ticklist. Incompatible mods?").printStackTrace();
                            }
                        }, this.mainThreadExecutor)
                                .thenApplyAsync(unused -> ChunkSerializer.serialize(this.world, chunk), ChunkIoThreadingExecutorUtils.serializerExecutor)
                                .thenAcceptAsync(compoundTag -> this.setTagAt(chunkPos, compoundTag), this.mainThreadExecutor)
                                .handle((unused, throwable) -> {
                            lockToken.releaseLock();
                            if (throwable != null)
                                LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, throwable);
                            return unused;
                        })));
                this.method_27053(chunkPos, chunkStatus.getChunkType());
                // C2ME end
                return true;
            } catch (Exception var5) {
                LOGGER.error("Failed to save chunk {},{}",  chunkPos.x, chunkPos.z, var5);
                return false;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        ChunkIoThreadingExecutorUtils.serializerExecutor.execute(() -> saveFutures.removeIf(CompletableFuture::isDone));
    }

    @Override
    public void completeAll() {
        final CompletableFuture<Void> future = CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]));
        this.mainThreadExecutor.runTasks(future::isDone); // wait for serialization to complete
        super.completeAll();
    }

    /**
     * @author ishland
     * @reason prevent race condition
     */
    @Overwrite
    public CompletableFuture<Void> enableTickSchedulers(WorldChunk worldChunk) {
        return chunkLock.acquireLock(worldChunk.getPos()).toCompletableFuture().thenCompose(lockToken -> this.mainThreadExecutor.submit(() -> {
            try {
                worldChunk.enableTickSchedulers(this.world);
            } finally {
                lockToken.releaseLock();
            }
        }));
    }
}
