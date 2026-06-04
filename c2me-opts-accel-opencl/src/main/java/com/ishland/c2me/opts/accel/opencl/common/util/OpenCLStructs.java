/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.util;

import com.ishland.c2me.base.mixin.access.IAquiferSamplerImpl;
import com.ishland.c2me.base.mixin.access.ICheckedRandomSplitter;
import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.base.mixin.access.IStructureWeightSampler;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomSplitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.CLBlockStateMappings;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.function.Function;

import static com.ishland.c2me.base.common.util.MemoryUtil.roundUp;

public class OpenCLStructs {

    public static final int GLOBAL_OFFSET_TABLE_START = 128;
    public static final int LOCAL_OFFSET_TABLE_START = 64;

    public static final int SIZE_estimateSurfaceHeight = 9 * 4; // TODO sync with cl source code

    public static final StructLayout worldgen_params = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("startBiomeX"),
            ValueLayout.JAVA_INT.withName("startBiomeZ"),
            ValueLayout.JAVA_INT.withName("sizeBiomeX"),
            ValueLayout.JAVA_INT.withName("sizeBiomeZ"),

            ValueLayout.JAVA_INT.withName("startCellX"),
            ValueLayout.JAVA_INT.withName("startCellY"),
            ValueLayout.JAVA_INT.withName("startCellZ"),
            ValueLayout.JAVA_INT.withName("sizeCellX"),
            ValueLayout.JAVA_INT.withName("sizeCellY"),
            ValueLayout.JAVA_INT.withName("sizeCellZ"),

            ValueLayout.JAVA_INT.withName("estimateSurfaceHeight_startBiomeX"),
            ValueLayout.JAVA_INT.withName("estimateSurfaceHeight_startBiomeZ"),
            ValueLayout.JAVA_INT.withName("estimateSurfaceHeight_sizeBiomeX"),
            ValueLayout.JAVA_INT.withName("estimateSurfaceHeight_sizeBiomeZ"),

            ValueLayout.JAVA_INT.withName("cache2d_startX"),
            ValueLayout.JAVA_INT.withName("cache2d_startZ"),
            ValueLayout.JAVA_INT.withName("cache2d_sizeX"),
            ValueLayout.JAVA_INT.withName("cache2d_sizeZ"),

            ValueLayout.JAVA_INT.withName("offset_estimateSurfaceHeight"), // int[28x28]
            ValueLayout.JAVA_INT.withName("genConfig_defaultBlock"),
            ValueLayout.JAVA_INT.withName("genConfig_defaultFluid"),
            ValueLayout.JAVA_INT.withName("offset_aquifer"),
            ValueLayout.JAVA_INT.withName("offset_fluidLevelSampler"), // aquifer_fluidlevel_t[] minimumY -> height
            ValueLayout.JAVA_INT.withName("offset_oreVeinRandom")
    ).withByteAlignment(4);
    public static final VarHandle worldgen_params$startBiomeX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("startBiomeX"));
    public static final VarHandle worldgen_params$startBiomeZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("startBiomeZ"));
    public static final VarHandle worldgen_params$sizeBiomeX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("sizeBiomeX"));
    public static final VarHandle worldgen_params$sizeBiomeZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("sizeBiomeZ"));

    public static final VarHandle worldgen_params$startCellX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("startCellX"));
    public static final VarHandle worldgen_params$startCellY = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("startCellY"));
    public static final VarHandle worldgen_params$startCellZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("startCellZ"));
    public static final VarHandle worldgen_params$sizeCellX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("sizeCellX"));
    public static final VarHandle worldgen_params$sizeCellY = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("sizeCellY"));
    public static final VarHandle worldgen_params$sizeCellZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("sizeCellZ"));

    public static final VarHandle worldgen_params$estimateSurfaceHeight_startBiomeX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("estimateSurfaceHeight_startBiomeX"));
    public static final VarHandle worldgen_params$estimateSurfaceHeight_startBiomeZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("estimateSurfaceHeight_startBiomeZ"));
    public static final VarHandle worldgen_params$estimateSurfaceHeight_sizeBiomeX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("estimateSurfaceHeight_sizeBiomeX"));
    public static final VarHandle worldgen_params$estimateSurfaceHeight_sizeBiomeZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("estimateSurfaceHeight_sizeBiomeZ"));

    public static final VarHandle worldgen_params$cache2d_startX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("cache2d_startX"));
    public static final VarHandle worldgen_params$cache2d_startZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("cache2d_startZ"));
    public static final VarHandle worldgen_params$cache2d_sizeX = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("cache2d_sizeX"));
    public static final VarHandle worldgen_params$cache2d_sizeZ = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("cache2d_sizeZ"));

    public static final VarHandle worldgen_params$offset_estimateSurfaceHeight = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("offset_estimateSurfaceHeight"));
    public static final VarHandle worldgen_params$genConfig_defaultBlock = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("genConfig_defaultBlock"));
    public static final VarHandle worldgen_params$genConfig_defaultFluid = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("genConfig_defaultFluid"));
    public static final VarHandle worldgen_params$offset_aquifer = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("offset_aquifer"));
    public static final VarHandle worldgen_params$offset_fluidLevelSampler = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("offset_fluidLevelSampler"));
    public static final VarHandle worldgen_params$offset_oreVeinRandom = worldgen_params.varHandle(MemoryLayout.PathElement.groupElement("offset_oreVeinRandom"));

    public static final StructLayout sws_data = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("pieceLength"),
            ValueLayout.JAVA_INT.withName("boxStartX"),
            ValueLayout.JAVA_INT.withName("boxStartY"),
            ValueLayout.JAVA_INT.withName("boxStartZ"),
            ValueLayout.JAVA_INT.withName("boxEndX"),
            ValueLayout.JAVA_INT.withName("boxEndY"),
            ValueLayout.JAVA_INT.withName("boxEndZ"),
            ValueLayout.JAVA_INT.withName("groundLevelDelta"),
            ValueLayout.JAVA_INT.withName("terrainAdjustment"),
            ValueLayout.JAVA_INT.withName("funcLength"),
            ValueLayout.JAVA_INT.withName("sourceX"),
            ValueLayout.JAVA_INT.withName("sourceGroundY"),
            ValueLayout.JAVA_INT.withName("sourceZ"),
            ValueLayout.JAVA_INT.withName("affectedBox_startX"),
            ValueLayout.JAVA_INT.withName("affectedBox_startY"),
            ValueLayout.JAVA_INT.withName("affectedBox_startZ"),
            ValueLayout.JAVA_INT.withName("affectedBox_endX"),
            ValueLayout.JAVA_INT.withName("affectedBox_endY"),
            ValueLayout.JAVA_INT.withName("affectedBox_endZ")
    ).withByteAlignment(4);
    public static final VarHandle sws_data$pieceLength = sws_data.varHandle(MemoryLayout.PathElement.groupElement("pieceLength"));
    public static final VarHandle sws_data$boxStartX = sws_data.varHandle(MemoryLayout.PathElement.groupElement("boxStartX"));
    public static final VarHandle sws_data$boxStartY = sws_data.varHandle(MemoryLayout.PathElement.groupElement("boxStartY"));
    public static final VarHandle sws_data$boxStartZ = sws_data.varHandle(MemoryLayout.PathElement.groupElement("boxStartZ"));
    public static final VarHandle sws_data$boxEndX = sws_data.varHandle(MemoryLayout.PathElement.groupElement("boxEndX"));
    public static final VarHandle sws_data$boxEndY = sws_data.varHandle(MemoryLayout.PathElement.groupElement("boxEndY"));
    public static final VarHandle sws_data$boxEndZ = sws_data.varHandle(MemoryLayout.PathElement.groupElement("boxEndZ"));
    public static final VarHandle sws_data$groundLevelDelta = sws_data.varHandle(MemoryLayout.PathElement.groupElement("groundLevelDelta"));
    public static final VarHandle sws_data$terrainAdjustment = sws_data.varHandle(MemoryLayout.PathElement.groupElement("terrainAdjustment"));
    public static final VarHandle sws_data$funcLength = sws_data.varHandle(MemoryLayout.PathElement.groupElement("funcLength"));
    public static final VarHandle sws_data$sourceX = sws_data.varHandle(MemoryLayout.PathElement.groupElement("sourceX"));
    public static final VarHandle sws_data$sourceGroundY = sws_data.varHandle(MemoryLayout.PathElement.groupElement("sourceGroundY"));
    public static final VarHandle sws_data$sourceZ = sws_data.varHandle(MemoryLayout.PathElement.groupElement("sourceZ"));
    public static final VarHandle sws_data$affectedBox_startX = sws_data.varHandle(MemoryLayout.PathElement.groupElement("affectedBox_startX"));
    public static final VarHandle sws_data$affectedBox_startY = sws_data.varHandle(MemoryLayout.PathElement.groupElement("affectedBox_startY"));
    public static final VarHandle sws_data$affectedBox_startZ = sws_data.varHandle(MemoryLayout.PathElement.groupElement("affectedBox_startZ"));
    public static final VarHandle sws_data$affectedBox_endX = sws_data.varHandle(MemoryLayout.PathElement.groupElement("affectedBox_endX"));
    public static final VarHandle sws_data$affectedBox_endY = sws_data.varHandle(MemoryLayout.PathElement.groupElement("affectedBox_endY"));
    public static final VarHandle sws_data$affectedBox_endZ = sws_data.varHandle(MemoryLayout.PathElement.groupElement("affectedBox_endZ"));

    public static MemorySegment sws_data$create(Arena arena, StructureWeightSampler sampler) {
        List<StructureWeightSampler.Piece> piecesList = ((IStructureWeightSampler) sampler).getPiecesList();
        List<JigsawJunction> junctionsList = ((IStructureWeightSampler) sampler).getJunctionsList();
        StructureWeightSampler.Piece[] pieceArray = piecesList.toArray(StructureWeightSampler.Piece[]::new);
        JigsawJunction[] junctionArray = junctionsList.toArray(JigsawJunction[]::new);

        int offset_boxStartX = roundUp((int) sws_data.byteSize(), 4);
        int offset_boxStartY = roundUp(offset_boxStartX + pieceArray.length * 4, 4);
        int offset_boxStartZ = roundUp(offset_boxStartY + pieceArray.length * 4, 4);
        int offset_boxEndX = roundUp(offset_boxStartZ + pieceArray.length * 4, 4);
        int offset_boxEndY = roundUp(offset_boxEndX + pieceArray.length * 4, 4);
        int offset_boxEndZ = roundUp(offset_boxEndY + pieceArray.length * 4, 4);
        int offset_groundLevelDelta = roundUp(offset_boxEndZ + pieceArray.length * 4, 4);
        int offset_terrainAdjustment = roundUp(offset_groundLevelDelta + pieceArray.length * 4, 4);
        int offset_sourceX = roundUp(offset_terrainAdjustment + pieceArray.length * 4, 4);
        int offset_sourceGroundY = roundUp(offset_sourceX + junctionArray.length * 4, 4);
        int offset_sourceZ = roundUp(offset_sourceGroundY + junctionArray.length * 4, 4);
        MemorySegment data = arena.allocate(roundUp(offset_sourceZ + junctionArray.length * 4, 4), 64);
        MemorySegment boxStartX = data.asSlice(offset_boxStartX, pieceArray.length * 4L);
        MemorySegment boxStartY = data.asSlice(offset_boxStartY, pieceArray.length * 4L);
        MemorySegment boxStartZ = data.asSlice(offset_boxStartZ, pieceArray.length * 4L);
        MemorySegment boxEndX = data.asSlice(offset_boxEndX, pieceArray.length * 4L);
        MemorySegment boxEndY = data.asSlice(offset_boxEndY, pieceArray.length * 4L);
        MemorySegment boxEndZ = data.asSlice(offset_boxEndZ, pieceArray.length * 4L);
        MemorySegment groundLevelDelta = data.asSlice(offset_groundLevelDelta, pieceArray.length * 4L);
        MemorySegment terrainAdjustment = data.asSlice(offset_terrainAdjustment, pieceArray.length * 4L);
        MemorySegment sourceX = data.asSlice(offset_sourceX, junctionArray.length * 4L);
        MemorySegment sourceGroundY = data.asSlice(offset_sourceGroundY, junctionArray.length * 4L);
        MemorySegment sourceZ = data.asSlice(offset_sourceZ, junctionArray.length * 4L);
        sws_data$pieceLength.set(data, 0L, pieceArray.length);
        sws_data$boxStartX.set(data, 0L, offset_boxStartX);
        sws_data$boxStartY.set(data, 0L, offset_boxStartY);
        sws_data$boxStartZ.set(data, 0L, offset_boxStartZ);
        sws_data$boxEndX.set(data, 0L, offset_boxEndX);
        sws_data$boxEndY.set(data, 0L, offset_boxEndY);
        sws_data$boxEndZ.set(data, 0L, offset_boxEndZ);
        sws_data$groundLevelDelta.set(data, 0L, offset_groundLevelDelta);
        sws_data$terrainAdjustment.set(data, 0L, offset_terrainAdjustment);
        sws_data$funcLength.set(data, 0L, junctionArray.length);
        sws_data$sourceX.set(data, 0L, offset_sourceX);
        sws_data$sourceGroundY.set(data, 0L, offset_sourceGroundY);
        sws_data$sourceZ.set(data, 0L, offset_sourceZ);

        sws_data$affectedBox_startX.set(data, 0L, ((IStructureWeightSampler) sampler).getAffectedBox().getMinX());
        sws_data$affectedBox_startY.set(data, 0L, ((IStructureWeightSampler) sampler).getAffectedBox().getMinY());
        sws_data$affectedBox_startZ.set(data, 0L, ((IStructureWeightSampler) sampler).getAffectedBox().getMinZ());
        sws_data$affectedBox_endX.set(data, 0L, ((IStructureWeightSampler) sampler).getAffectedBox().getMaxX());
        sws_data$affectedBox_endY.set(data, 0L, ((IStructureWeightSampler) sampler).getAffectedBox().getMaxY());
        sws_data$affectedBox_endZ.set(data, 0L, ((IStructureWeightSampler) sampler).getAffectedBox().getMaxZ());

        for (int i = 0; i < pieceArray.length; i++) {
            boxStartX.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].box().getMinX());
            boxStartY.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].box().getMinY());
            boxStartZ.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].box().getMinZ());
            boxEndX.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].box().getMaxX());
            boxEndY.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].box().getMaxY());
            boxEndZ.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].box().getMaxZ());
            groundLevelDelta.set(ValueLayout.JAVA_INT, i * 4L, pieceArray[i].groundLevelDelta());
            terrainAdjustment.set(
                    ValueLayout.JAVA_INT,
                    i * 4L,
                    switch (pieceArray[i].terrainAdjustment()) {
                        case NONE -> 0;
                        case BURY -> 1;
                        case BEARD_THIN -> 2;
                        case BEARD_BOX -> 3;
                        case ENCAPSULATE -> 4;
                    }
            );
        }

        for (int i = 0; i < junctionArray.length; i++) {
            sourceX.set(ValueLayout.JAVA_INT, i * 4L, junctionArray[i].getSourceX());
            sourceGroundY.set(ValueLayout.JAVA_INT, i * 4L, junctionArray[i].getSourceGroundY());
            sourceZ.set(ValueLayout.JAVA_INT, i * 4L, junctionArray[i].getSourceZ());
        }

        return data;
    }

    public static final StructLayout sws_index = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("startX"),
            ValueLayout.JAVA_INT.withName("startZ"),
            ValueLayout.JAVA_INT.withName("sizeX"),
            ValueLayout.JAVA_INT.withName("sizeZ")
    ).withByteAlignment(4);

    public static final VarHandle sws_index$startX = sws_index.varHandle(MemoryLayout.PathElement.groupElement("startX"));
    public static final VarHandle sws_index$startZ = sws_index.varHandle(MemoryLayout.PathElement.groupElement("startZ"));
    public static final VarHandle sws_index$sizeX = sws_index.varHandle(MemoryLayout.PathElement.groupElement("sizeX"));
    public static final VarHandle sws_index$sizeZ = sws_index.varHandle(MemoryLayout.PathElement.groupElement("sizeZ"));

    public static MemorySegment sws_index$createForSingleChunk(Arena arena, ChunkPos pos, StructureWeightSampler sampler) {
        MemorySegment memorySegment = sws_data$create(arena, sampler);
        MemorySegment data = arena.allocate(sws_index.byteSize() + 4 + memorySegment.byteSize(), 4);
        sws_index$startX.set(data, 0L, pos.x());
        sws_index$startZ.set(data, 0L, pos.z());
        sws_index$sizeX.set(data, 0L, 1);
        sws_index$sizeZ.set(data, 0L, 1);
        data.set(ValueLayout.JAVA_INT, sws_index.byteSize(), (int) (sws_index.byteSize() + 4));
        MemorySegment.copy(memorySegment, 0, data, sws_index.byteSize() + 4, memorySegment.byteSize());
        return data;
    }

    public static MemorySegment sws_index$createForArea(Arena arena, ChunkPos pos, BoundedRegionArray<ProtoChunk> regionArray, Function<ProtoChunk, ChunkNoiseSampler> toChunkNoiseSampler, int sizeX, int sizeZ) {
        MemorySegment data = arena.allocate(roundUp(sws_index.byteSize() * (sizeX * sizeZ) + 4, 512), 4);
        sws_index$startX.set(data, 0L, pos.x());
        sws_index$startZ.set(data, 0L, pos.z());
        sws_index$sizeX.set(data, 0L, sizeX);
        sws_index$sizeZ.set(data, 0L, sizeZ);

        int currentOffset = (int) (sws_index.byteSize() + 4 * (sizeX * sizeZ));
        data.set(ValueLayout.JAVA_INT, sws_index.byteSize(), currentOffset);

        for (int dx = 0; dx < sizeX; dx ++) {
            for (int dz = 0; dz < sizeZ; dz ++) {
                ProtoChunk chunk = regionArray.get(pos.x() + dx, pos.z() + dz);
                ChunkNoiseSampler sampler = toChunkNoiseSampler.apply(chunk);
                StructureWeightSampler structureWeightSampler = (StructureWeightSampler) ((IChunkNoiseSampler) sampler).getBeardifying();
                if (structureWeightSampler == null || ((IStructureWeightSampler) structureWeightSampler).getAffectedBox() == null) {
                    data.set(ValueLayout.JAVA_INT, sws_index.byteSize() + 4 * ((long) dx * sizeZ + dz), 0);
                } else {
                    MemorySegment memorySegment = sws_data$create(arena, structureWeightSampler);
                    currentOffset = roundUp(currentOffset, 8);
                    data.set(ValueLayout.JAVA_INT, sws_index.byteSize() + 4 * ((long) dx * sizeZ + dz), currentOffset);
                    while (data.byteSize() < currentOffset + memorySegment.byteSize()) {
                        // realloc
                        MemorySegment newData = arena.allocate(data.byteSize() * 2, 4);
                        MemorySegment.copy(data, 0, newData, 0, data.byteSize());
                        data = newData;
                    }
                    MemorySegment.copy(memorySegment, 0, data, currentOffset, memorySegment.byteSize());
                    currentOffset += (int) memorySegment.byteSize();
                }
            }
        }
        return data.asSlice(0, roundUp(currentOffset, 8));
    }

    //static constant const uint64_t RANDOM_Checked = 0;
    //static constant const uint64_t RANDOM_Xoroshiro128PlusPlus = 1;

    //typedef struct random_state {
    //    uint64_t type; // see constants above
    //    uint64_t seedLo;
    //    uint64_t seedHi;
    //} random_state_t;
    public static void setRandomState(MemorySegment data, RandomSplitter randomSplitter) {
        switch (randomSplitter) {
            case CheckedRandom.Splitter s -> {
                data.set(ValueLayout.JAVA_LONG, 0, 0L);
                data.set(ValueLayout.JAVA_LONG, 8, ((ICheckedRandomSplitter) s).getSeed());
                data.set(ValueLayout.JAVA_LONG, 16, 0L);
            }
            case Xoroshiro128PlusPlusRandom.Splitter s -> {
                data.set(ValueLayout.JAVA_LONG, 0, 1L);
                data.set(ValueLayout.JAVA_LONG, 8, ((IXoroshiro128PlusPlusRandomSplitter) s).getSeedLo());
                data.set(ValueLayout.JAVA_LONG, 16, ((IXoroshiro128PlusPlusRandomSplitter) s).getSeedHi());
            }
            default -> throw new UnsupportedOperationException("Unknown random splitter type " + randomSplitter.getClass().getName());
        }
    }

    //typedef const struct aquifer_data {
    //    int32_t startX;
    //    int32_t startY;
    //    int32_t startZ;
    //    int32_t sizeX;
    //    int32_t sizeY;
    //    int32_t sizeZ;
    //
    //    int32_t randomDeriver;
    //    int32_t posIdx_len;
    //    int32_t waterLevels; // aquifer_fluidlevel_t[posIdx]: int32_t y, int32_t blockState
    //    int32_t packedBlockPositions; // short[posIdx]
    //} aquifer_data_t;
    public static final StructLayout aquifer_data = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("startX"),
            ValueLayout.JAVA_INT.withName("startY"),
            ValueLayout.JAVA_INT.withName("startZ"),
            ValueLayout.JAVA_INT.withName("sizeX"),
            ValueLayout.JAVA_INT.withName("sizeY"),
            ValueLayout.JAVA_INT.withName("sizeZ"),
            ValueLayout.JAVA_INT.withName("samplingYLowPassCutoff"),
            ValueLayout.JAVA_INT.withName("randomDeriver"),
            ValueLayout.JAVA_INT.withName("posIdx_len"),
            ValueLayout.JAVA_INT.withName("waterLevels"),
            ValueLayout.JAVA_INT.withName("packedBlockPositions")
    ).withByteAlignment(4);
    public static final VarHandle aquifer_data$startX = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("startX"));
    public static final VarHandle aquifer_data$startY = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("startY"));
    public static final VarHandle aquifer_data$startZ = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("startZ"));
    public static final VarHandle aquifer_data$sizeX = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("sizeX"));
    public static final VarHandle aquifer_data$sizeY = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("sizeY"));
    public static final VarHandle aquifer_data$sizeZ = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("sizeZ"));
    public static final VarHandle aquifer_data$samplingYLowPassCutoff = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("samplingYLowPassCutoff"));
    public static final VarHandle aquifer_data$randomDeriver = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("randomDeriver"));
    public static final VarHandle aquifer_data$posIdx_len = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("posIdx_len"));
    public static final VarHandle aquifer_data$waterLevels = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("waterLevels"));
    public static final VarHandle aquifer_data$packedBlockPositions = aquifer_data.varHandle(MemoryLayout.PathElement.groupElement("packedBlockPositions"));

    public static MemorySegment aquifer_data$create(Arena arena, AquiferSampler.Impl impl) {
        long[] blockPositions = ((IAquiferSamplerImpl) impl).getBlockPositions();
        int sizeX = ((IAquiferSamplerImpl) impl).getSizeX();
        int sizeZ = ((IAquiferSamplerImpl) impl).getSizeZ();
        if (blockPositions.length % (sizeX * sizeZ) != 0) {
            throw new AssertionError("Array length");
        }

        int sizeY = blockPositions.length / (sizeX * sizeZ);
        int samplingYLowPassCutoff = ((IAquiferSamplerImpl) impl).getSamplingYLowPassCutoff();
        int startX = ((IAquiferSamplerImpl) impl).getStartX();
        int startY = ((IAquiferSamplerImpl) impl).getStartY();
        int startZ = ((IAquiferSamplerImpl) impl).getStartZ();
        RandomSplitter randomDeriver1 = ((IAquiferSamplerImpl) impl).getRandomDeriver();

        return aquifer_data$create(arena, startX, startY, startZ, sizeX, sizeY, sizeZ, samplingYLowPassCutoff, randomDeriver1);
    }

    public static @NotNull MemorySegment aquifer_data$create(Arena arena, int startX, int startY, int startZ, int sizeX, int sizeY, int sizeZ, int samplingYLowPassCutoff, RandomSplitter randomDeriver1) {
        int cacheLength = sizeX * sizeY * sizeZ;

        int offset_randomDeriver = roundUp((int) aquifer_data.byteSize(), 8);
        int offset_waterLevels = roundUp(offset_randomDeriver + 24, 4);
        int offset_packedBlockPositions = roundUp(offset_waterLevels + cacheLength * 8, 4);
        MemorySegment data = arena.allocate(roundUp(offset_packedBlockPositions + cacheLength * 2, 4), 64);
        MemorySegment randomDeriver = data.asSlice(offset_randomDeriver, 24L);
        MemorySegment waterLevels = data.asSlice(offset_waterLevels, cacheLength * 8L);
        MemorySegment packedBlockPositions = data.asSlice(offset_packedBlockPositions, cacheLength * 2L);
        aquifer_data$startX.set(data, 0L, startX);
        aquifer_data$startY.set(data, 0L, startY);
        aquifer_data$startZ.set(data, 0L, startZ);
        aquifer_data$samplingYLowPassCutoff.set(data, 0L, samplingYLowPassCutoff);
        aquifer_data$sizeX.set(data, 0L, sizeX);
        aquifer_data$sizeY.set(data, 0L, sizeY);
        aquifer_data$sizeZ.set(data, 0L, sizeZ);
        aquifer_data$randomDeriver.set(data, 0L, offset_randomDeriver);
        aquifer_data$posIdx_len.set(data, 0L, cacheLength);
        aquifer_data$waterLevels.set(data, 0L, offset_waterLevels);
        aquifer_data$packedBlockPositions.set(data, 0L, offset_packedBlockPositions);

        setRandomState(randomDeriver, randomDeriver1);
        // data initialized on device

        return data;
    }

    //typedef struct aquifer_fluidlevel {
    //    int32_t y;
    //    int32_t blockState;
    //} aquifer_fluidlevel_t;

    public static MemorySegment fluidLevelSamplerCreate(Arena arena, int minimumY, int height, AquiferSampler.FluidLevelSampler fluidLevelSampler, CLBlockStateMappings mappings) {
        MemorySegment data = arena.allocate((height + 1) * 8L, 4);
        for (int i = 0; i <= height; i ++) {
            int y = i + minimumY;
            AquiferSampler.FluidLevel fluidLevel = fluidLevelSampler.getFluidLevel(0, y, 0);
            data.set(ValueLayout.JAVA_INT, i * 8L, fluidLevel.y());
            data.set(ValueLayout.JAVA_INT, i * 8L + 4, mappings.toId(fluidLevel.state()));
        }
        return data;
    }

}
