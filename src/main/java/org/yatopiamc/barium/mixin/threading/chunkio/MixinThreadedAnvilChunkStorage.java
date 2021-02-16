package org.yatopiamc.barium.mixin.threading.chunkio;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.yatopiamc.barium.common.threading.chunkio.ChunkIoMainThreadTaskUtils;
import org.yatopiamc.barium.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import org.yatopiamc.barium.common.threading.chunkio.ISerializingRegionBasedStorage;
import org.yatopiamc.barium.common.threading.chunkio.ThreadedStorageIo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {

    public MixinThreadedAnvilChunkStorage(File file, DataFixer dataFixer, boolean bl) {
        super(file, dataFixer, bl);
    }

    @Shadow
    @Nullable
    protected abstract CompoundTag getUpdatedChunkTag(ChunkPos pos) throws IOException;

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

    /**
     * @author ishland
     * @reason async io and deserialization
     */
    @Overwrite
    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> loadChunk(ChunkPos pos) {

        final CompletableFuture<CompoundTag> poiData = ((ThreadedStorageIo) this.pointOfInterestStorage.worker).getNbtAtAsync(pos);

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
        return ((ThreadedStorageIo) this.worker).getNbtAtAsync(pos).thenApply(compoundTag -> {
            if (compoundTag != null)
                return this.updateChunkTag(this.world.getRegistryKey(), this.persistentStateManagerFactory, compoundTag);
            else return null;
        });
    }

}
