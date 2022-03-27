package com.ishland.c2me.opts.chunk_serializer.mixin;

import com.ishland.c2me.opts.chunk_serializer.common.ChunkDataSerializer;
import com.ishland.c2me.opts.chunk_serializer.common.NbtWriter;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.DataOutputStream;
import java.nio.file.Path;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage implements VersionedChunkStorageAccessor {
    @Final
    @Shadow
    private static Logger LOGGER;

    @Final
    @Shadow
    private PointOfInterestStorage pointOfInterestStorage;

    @Final
    @Shadow
    ServerWorld world;

    @Shadow
    private native boolean isLevelChunk(ChunkPos chunkPos);

    @Shadow
    private native byte mark(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType);

    public MixinThreadedAnvilChunkStorage(Path directory, DataFixer dataFixer, boolean dsync) {
        super(directory, dataFixer, dsync);
    }

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
            if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                if (this.isLevelChunk(chunkPos)) {
                    return false;
                }

                if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                    return false;
                }
            }

            this.world.getProfiler().visit("chunkSave");

            //region start replaced code
            // NbtCompound nbtCompound = ChunkSerializer.serialize(this.world, chunk);
            NbtWriter nbtWriter = new NbtWriter();
            nbtWriter.start(NbtElement.COMPOUND_TYPE);
            ChunkDataSerializer.write(this.world, chunk, nbtWriter);
            nbtWriter.finishCompound();

            // this.setNbt(chunkPos, nbtCompound);
            // temp fix, idk,
            var storageWorker = (StorageIoWorkerAccessor) this.getIoWorker();
            var storage = (RegionBasedStorageAccessor) (Object) storageWorker.getStorage();
            storageWorker.invokeRun(() -> {
                try {
                    DataOutputStream chunkOutputStream = storage.invokeGetRegionFile(chunkPos).getChunkOutputStream(chunkPos);
                    chunkOutputStream.write(nbtWriter.toByteArray());
                    chunkOutputStream.close();
                    return Either.left((Void) null);
                } catch (Exception t) {
                    return Either.right(t);
                }
            });

            //endregion end replaced code

            this.mark(chunkPos, chunkStatus.getChunkType());
            return true;
        } catch (Exception var5) {
            LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, var5);
            return false;
        }
    }
}
