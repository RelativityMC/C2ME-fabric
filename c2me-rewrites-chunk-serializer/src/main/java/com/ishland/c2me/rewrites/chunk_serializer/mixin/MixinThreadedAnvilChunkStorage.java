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

package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.theinterface.IDirectStorage;
import com.ishland.c2me.base.mixin.access.IChunkHolder;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import com.ishland.c2me.rewrites.chunk_serializer.common.utils.ValidationUtils;
import com.mojang.datafixers.DataFixer;
import net.minecraft.datafixer.DataFixType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Mixin(value = ServerChunkLoadingManager.class, priority = 1099)
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

    public MixinThreadedAnvilChunkStorage(StorageKey storageKey, Path path, DataFixer dataFixer, boolean bl, DataFixType dataFixTypes) {
        super(storageKey, path, dataFixer, bl, dataFixTypes);
    }

    @Shadow
    private native boolean isLevelChunk(ChunkPos chunkPos);

    @Shadow
    private native byte mark(ChunkPos chunkPos, ChunkType chunkType);


    @Shadow protected abstract @Nullable ChunkHolder getCurrentChunkHolder(long pos);

    @Shadow @Final private AtomicInteger chunksBeingSavedCount;

    /**
     * @author Kroppeb
     * @reason Reduces allocations
     */
    @Overwrite()
    private boolean save(Chunk chunk) {
        // [VanillaCopy]
        this.pointOfInterestStorage.saveChunk(chunk.getPos());
        if (!chunk.tryMarkSaved()) {
            return false;
        }

        ChunkPos chunkPos = chunk.getPos();

        try {
            ChunkStatus chunkStatus = chunk.getStatus();
            if (chunkStatus.getChunkType() != ChunkType.LEVELCHUNK) {
                if (this.isLevelChunk(chunkPos)) {
                    return false;
                }

                if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                    return false;
                }
            }

            Profilers.get().visit("chunkSave");

            this.chunksBeingSavedCount.incrementAndGet();
            SerializedChunk chunkSerializer = SerializedChunk.fromChunk(this.world, chunk);
            //region start replaced code
            // NbtCompound nbtCompound = ChunkSerializer.serialize(this.world, chunk);
            CompletableFuture<byte[]> serializationFuture = CompletableFuture.supplyAsync(() -> {
                NbtWriter nbtWriter = new NbtWriter();
                try {
                    nbtWriter.start(NbtElement.COMPOUND_TYPE);
                    ChunkDataSerializer.write(chunkSerializer, nbtWriter);
                    nbtWriter.finishCompound();
                    byte[] byteArray = nbtWriter.toByteArray();
                    ValidationUtils.validateNbt(byteArray);
                    return byteArray;
                } finally {
                    nbtWriter.release();
                }
            }, ((IVanillaChunkManager) this).c2me$getSchedulingManager().positionedExecutor(chunk.getPos().toLong()));

            CompletableFuture<Void> saveFuture = ((IDirectStorage) ((IVersionedChunkStorage) this).getWorker()).setRawChunkData(chunkPos, serializationFuture);

            saveFuture.handle((void_, exceptionx) -> {
                if (exceptionx != null) {
                    this.world.getServer().onChunkSaveFailure(exceptionx, this.getStorageKey(), chunkPos);
                }

                this.chunksBeingSavedCount.decrementAndGet();
                return null;
            });
            //endregion end replaced code

            this.mark(chunkPos, chunkStatus.getChunkType());
            return true;
        } catch (Exception var5) {
            LOGGER.error("Failed to save chunk {},{}", chunkPos.x(), chunkPos.z(), var5);
            return false;
        }
    }
}
