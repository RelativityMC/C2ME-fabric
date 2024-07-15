package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import com.ishland.c2me.base.common.theinterface.IDirectStorage;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage {
    @Final
    @Shadow
    private static Logger LOGGER;

    @Final
    @Shadow
    private PointOfInterestStorage pointOfInterestStorage;

    @Final
    @Shadow
    ServerWorld world;

    public MixinThreadedAnvilChunkStorage(StorageKey arg, Path path, DataFixer dataFixer, boolean bl) {
        super(arg, path, dataFixer, bl);
    }

    @Shadow
    private native boolean isLevelChunk(ChunkPos chunkPos);

    @Shadow
    private native byte mark(ChunkPos chunkPos, ChunkType chunkType);


    /**
     * @author Kroppeb
     * @reason Reduces allocations
     */
    @Overwrite()
    private boolean save(Chunk chunk) {
        // [VanillaCopy]
        this.pointOfInterestStorage.saveChunk(chunk.getPos());
        if (!chunk.needsSaving()) {
            return false;
        }

        chunk.setNeedsSaving(false);
        ChunkPos chunkPos = chunk.getPos();

        try {
            ChunkStatus chunkStatus = chunk.getStatus();
//            if (chunkStatus.getChunkType() != ChunkType.LEVELCHUNK) {
//                if (this.isLevelChunk(chunkPos)) {
//                    return false;
//                }
//
//                if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
//                    return false;
//                }
//            }

            this.world.getProfiler().visit("chunkSave");

            //region start replaced code
            // NbtCompound nbtCompound = ChunkSerializer.serialize(this.world, chunk);
            NbtWriter nbtWriter = new NbtWriter();
            nbtWriter.start(NbtElement.COMPOUND_TYPE);
            ChunkDataSerializer.write(this.world, chunk, nbtWriter);
            nbtWriter.finishCompound();

            // this.setNbt(chunkPos, nbtCompound);
            // temp fix, idk,
//            var storageWorker = (StorageIoWorkerAccessor) this.getIoWorker();
//            var storage = (RegionBasedStorageAccessor) (Object) storageWorker.getStorage();
//            storageWorker.invokeRun(() -> {
//                try {
//                    DataOutputStream chunkOutputStream = storage.invokeGetRegionFile(chunkPos).getChunkOutputStream(chunkPos);
//                    chunkOutputStream.write(nbtWriter.toByteArray());
//                    chunkOutputStream.close();
//                    nbtWriter.release();
//                    return Either.left((Void) null);
//                } catch (Exception t) {
//                    return Either.right(t);
//                }
//            });
            ((IDirectStorage) ((IVersionedChunkStorage) this).getWorker()).setRawChunkData(chunkPos, nbtWriter.toByteArray());
            nbtWriter.release();

            //endregion end replaced code

            this.mark(chunkPos, chunkStatus.getChunkType());
            return true;
        } catch (Exception var5) {
            LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, var5);
            return false;
        }
    }
}
