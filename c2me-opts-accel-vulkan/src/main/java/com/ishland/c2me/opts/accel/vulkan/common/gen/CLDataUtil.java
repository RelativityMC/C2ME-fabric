package com.ishland.c2me.opts.accel.vulkan.common.gen;

import com.ishland.c2me.base.mixin.access.IAquiferSamplerImpl;
import com.ishland.c2me.base.mixin.access.INoiseChunkGenerator;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.GeneratedSpirVSource;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.vulkan.common.gen.cache.Stage1Cache;
import com.ishland.c2me.opts.accel.vulkan.common.util.VkStructs;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.lwjgl.system.MemoryUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.function.Function;

import static com.ishland.c2me.base.common.util.MemoryUtil.roundUp;

public class CLDataUtil {

    public static ByteBuffer worldgen_data_root$createForFlatCacheOnly(ChunkPos base, int chunkSize, GeneratedSpirVSource generatedSpirVSource, double[] flatCachePrefilled, boolean extendByOne) {
        if (generatedSpirVSource == null) {
            throw new IllegalStateException("Generated CL source not found");
        }

        record OffsetAndData(int offset, byte[] data) {
        }

//        int localSize = 0;

        OffsetAndData[] allocatedOffsets = new OffsetAndData[generatedSpirVSource.getGlobalDynamicDataOffsets().size()];
        int currentTail = roundUp(VkStructs.GLOBAL_OFFSET_TABLE_START + generatedSpirVSource.getGlobalDynamicDataOffsets().size() * Integer.BYTES, 32);
        for (Reference2IntMap.Entry<Object> entry : generatedSpirVSource.getGlobalDynamicDataOffsets().reference2IntEntrySet()) {
            Object key = entry.getKey();
            int index = entry.getIntValue();
            Assertions.assertTrue(allocatedOffsets[index] == null);
            if (key == SpirVGen.MARKER_localOffsetTable) {
                allocatedOffsets[index] = new OffsetAndData(0, null);
            } else if (key instanceof ConstantBlob blob) {
                int offset = roundUp(currentTail, blob.alignment);
                currentTail = offset + blob.data.length;
                allocatedOffsets[index] = new OffsetAndData(offset, blob.data);
            } else if (key == DensityFunctionTypes.Beardifier.INSTANCE) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no beardifier
            } else if (key == SpirVGen.MARKER_estimateSurfaceHeightCache) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no cache
            } else if (key == SpirVGen.MARKER_aquifer) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no aquifer
            } else if (key == SpirVGen.MARKER_fluidLevelSampler) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no fluid level sampler
            } else if (key == SpirVGen.MARKER_oreVeinRandom) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no ore veins
            } else if (key == SpirVGen.MARKER_cacheLike_interpolator) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no interpolator
            } else if (key == SpirVGen.MARKER_cacheLike_flatCache) {
                int offset = roundUp(currentTail, 8);
                int bufSize = generatedSpirVSource.getFlatCachePrefills() * MathHelper.square(chunkSize * 4 + 1) * 8;
                currentTail = offset + bufSize;
                if (flatCachePrefilled != null) {
                    byte[] data = new byte[flatCachePrefilled.length * Double.BYTES];
                    MemorySegment.copy(MemorySegment.ofArray(flatCachePrefilled), ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                    Assertions.assertTrue(data.length == bufSize);
                    allocatedOffsets[index] = new OffsetAndData(offset, data);
                } else {
                    allocatedOffsets[index] = new OffsetAndData(offset, null);
                }
            } else if (key == SpirVGen.MARKER_cacheLike_cache2d) {
                allocatedOffsets[index] = new OffsetAndData(0, null); // no interpolator
            } else {
                throw new UnsupportedOperationException("Unsupported key type " + key.getClass().getName());
            }
        }

        ByteBuffer byteBuffer = MemoryUtil.memAlloc(roundUp(currentTail, 32));
        MemorySegment segment = MemorySegment.ofBuffer(byteBuffer);
        VkStructs.worldgen_params$startBiomeX.set(segment, 0L, BiomeCoords.fromChunk(base.x()));
        VkStructs.worldgen_params$startBiomeZ.set(segment, 0L, BiomeCoords.fromChunk(base.z()));
        VkStructs.worldgen_params$sizeBiomeX.set(segment, 0L, (chunkSize * 4) - (extendByOne ? 0 : 1));
        VkStructs.worldgen_params$sizeBiomeZ.set(segment, 0L, (chunkSize * 4) - (extendByOne ? 0 : 1));
        VkStructs.worldgen_params$startCellX.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$startCellY.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$startCellZ.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$sizeCellX.set(segment, 0L, 0);
        VkStructs.worldgen_params$sizeCellY.set(segment, 0L, 0);
        VkStructs.worldgen_params$sizeCellZ.set(segment, 0L, 0);
        VkStructs.worldgen_params$estimateSurfaceHeight_startBiomeX.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$estimateSurfaceHeight_startBiomeZ.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$estimateSurfaceHeight_sizeBiomeX.set(segment, 0L, 0);
        VkStructs.worldgen_params$estimateSurfaceHeight_sizeBiomeZ.set(segment, 0L, 0);
        VkStructs.worldgen_params$cache2d_startX.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$cache2d_startZ.set(segment, 0L, Integer.MAX_VALUE);
        VkStructs.worldgen_params$cache2d_sizeX.set(segment, 0L, 0);
        VkStructs.worldgen_params$cache2d_sizeZ.set(segment, 0L, 0);
        VkStructs.worldgen_params$offset_estimateSurfaceHeight.set(segment, 0L, 0);
        VkStructs.worldgen_params$genConfig_defaultBlock.set(segment, 0L, 0);
        VkStructs.worldgen_params$genConfig_defaultFluid.set(segment, 0L, 0);
        VkStructs.worldgen_params$offset_aquifer.set(segment, 0L, 0);
        VkStructs.worldgen_params$offset_fluidLevelSampler.set(segment, 0L, 0);
        VkStructs.worldgen_params$offset_oreVeinRandom.set(segment, 0L, 0);

        for (int i = 0, allocatedOffsetsLength = allocatedOffsets.length; i < allocatedOffsetsLength; i++) {
            OffsetAndData allocatedOffset = allocatedOffsets[i];
            Assertions.assertTrue(allocatedOffset != null);
            assert allocatedOffset != null;
            segment.set(ValueLayout.JAVA_INT, VkStructs.GLOBAL_OFFSET_TABLE_START + (long) i * Integer.BYTES, allocatedOffset.offset);
            if (allocatedOffset.offset != 0 && allocatedOffset.data != null) {
                MemorySegment.copy(allocatedOffset.data, 0, segment, ValueLayout.JAVA_BYTE, allocatedOffset.offset, allocatedOffset.data.length);
            }
        }

        return byteBuffer;
    }

    public static ByteBuffer worldgen_data_root$createForArea(ChunkPos basePos, int horizontalChunkSize, BoundedRegionArray<ProtoChunk> regionArray, NoiseChunkGenerator generator,
                                                              NoiseConfig noiseConfig, BoundedRegionArray<StructureAccessor> structureAccessors,
                                                              CLBlockStateMappings mappings, GeneratedSpirVSource generatedSpirVSource, Stage1Cache.AreaCacheEntry stage1Cache) {
        if (generatedSpirVSource == null) {
            throw new IllegalStateException("Generated CL source not found");
        }

        ChunkGeneratorSettings settings = generator.getSettings().value();
        AquiferSampler.FluidLevelSampler fluidLevelSampler = ((INoiseChunkGenerator) (Object) generator).getFluidLevelSampler().get();

        GenerationShapeConfig generationShapeConfig = settings.generationShapeConfig();
        int horizontalCellCount = (horizontalChunkSize * 16) / generationShapeConfig.horizontalCellBlockCount();
        int verticalCellCount = MathHelper.floorDiv(generationShapeConfig.height(), generationShapeConfig.verticalCellBlockCount());
        Assertions.assertTrue(horizontalCellCount * generationShapeConfig.horizontalCellBlockCount() == horizontalChunkSize * 16);

        Function<ProtoChunk, ChunkNoiseSampler> toChunkNoiseSampler =
                chunkx -> chunkx.getOrCreateChunkNoiseSampler(chunkxx -> {
                    StructureAccessor structureAccessor = structureAccessors.get(chunkxx.getPos().x(), chunkxx.getPos().z());
                    return ((INoiseChunkGenerator) (Object) generator).invokeCreateChunkNoiseSampler(chunkxx, structureAccessor, Blender.getNoBlending(), noiseConfig);
                });

        record OffsetAndData(int offset, byte[] data) {
        }

//        int localSize = 0;

        OffsetAndData[] allocatedOffsets = new OffsetAndData[generatedSpirVSource.getGlobalDynamicDataOffsets().size()];
        int currentTail = roundUp(VkStructs.GLOBAL_OFFSET_TABLE_START + generatedSpirVSource.getGlobalDynamicDataOffsets().size() * Integer.BYTES, 32);
        for (Reference2IntMap.Entry<Object> entry : generatedSpirVSource.getGlobalDynamicDataOffsets().reference2IntEntrySet()) {
            Object key = entry.getKey();
            int index = entry.getIntValue();
            Assertions.assertTrue(allocatedOffsets[index] == null);
            if (key == SpirVGen.MARKER_localOffsetTable) {
                allocatedOffsets[index] = new OffsetAndData(0, null);
            } else if (key instanceof ConstantBlob blob) {
                int offset = roundUp(currentTail, blob.alignment);
                currentTail = offset + blob.data.length;
                allocatedOffsets[index] = new OffsetAndData(offset, blob.data);
            } else if (key == DensityFunctionTypes.Beardifier.INSTANCE) {
                int offset = roundUp(currentTail, 4);
                byte[] data;
                try (Arena arena1 = Arena.ofConfined()) {
                    MemorySegment segment = VkStructs.sws_index$createForArea(arena1, basePos, regionArray, toChunkNoiseSampler, horizontalChunkSize, horizontalChunkSize);
                    data = new byte[(int) segment.byteSize()];
                    MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                }
                currentTail = offset + data.length;
                allocatedOffsets[index] = new OffsetAndData(offset, data);
            } else if (key == SpirVGen.MARKER_estimateSurfaceHeightCache) {
                if (stage1Cache != null) {
                    int offset = roundUp(currentTail, 4);
                    currentTail = offset + (stage1Cache.surfaceHeights().length) * 4;
//                    Assertions.assertTrue(stage1Cache.surfaceHeights().length == VkStructs.SIZE_estimateSurfaceHeight * VkStructs.SIZE_estimateSurfaceHeight);
                    byte[] data = new byte[stage1Cache.surfaceHeights().length * Integer.BYTES];
                    MemorySegment.copy(MemorySegment.ofArray(stage1Cache.surfaceHeights()), ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                    allocatedOffsets[index] = new OffsetAndData(offset, data);
                } else {
                    allocatedOffsets[index] = new OffsetAndData(0, null); // no cache
                }
            } else if (key == SpirVGen.MARKER_aquifer) {
                if (settings.hasAquifers()) {
                    int offset = roundUp(currentTail, 8);
                    byte[] data;
                    try (Arena arena1 = Arena.ofConfined()) {

                        // TODO [VanillaCopy] check when aquifer changes
                        int startX = IAquiferSamplerImpl.invokeGetLocalX(basePos.getStartX() - 5) + 0;
                        int startY = IAquiferSamplerImpl.invokeGetLocalY(generationShapeConfig.minimumY() + 1) - 1;
                        int startZ = IAquiferSamplerImpl.invokeGetLocalZ(basePos.getStartZ() - 5) + 0;
                        ChunkPos endChunkPos = new ChunkPos(basePos.x() + horizontalChunkSize - 1, basePos.z() + horizontalChunkSize - 1);
                        int endX = IAquiferSamplerImpl.invokeGetLocalX(endChunkPos.getEndX() + 5 - 1) + 1;
                        int endY = IAquiferSamplerImpl.invokeGetLocalY(generationShapeConfig.minimumY() + generationShapeConfig.height() - 1) + 1;
                        int endZ = IAquiferSamplerImpl.invokeGetLocalZ(endChunkPos.getEndZ() + 5 - 1) + 1;

                        int samplingYLowPassCutoff = generationShapeConfig.minimumY();

                        for (int dx = 0; dx < horizontalChunkSize; dx++) {
                            for (int dz = 0; dz < horizontalChunkSize; dz++) {
                                int x = basePos.x() + dx;
                                int z = basePos.z() + dz;
                                ProtoChunk chunk = regionArray.get(x, z);
                                ChunkNoiseSampler sampler = toChunkNoiseSampler.apply(chunk);
                                AquiferSampler.Impl aquiferSampler = (AquiferSampler.Impl) sampler.getAquiferSampler();
                                samplingYLowPassCutoff = Math.max(samplingYLowPassCutoff, ((IAquiferSamplerImpl) aquiferSampler).getSamplingYLowPassCutoff());
                            }
                        }

                        MemorySegment segment = VkStructs.aquifer_data$create(
                                arena1,
                                startX, startY, startZ,
                                endX - startX + 1, endY - startY + 1, endZ - startZ + 1,
                                samplingYLowPassCutoff, noiseConfig.getAquiferRandomDeriver()
                        );
                        data = new byte[(int) segment.byteSize()];
                        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                    }
                    currentTail = offset + data.length;
                    allocatedOffsets[index] = new OffsetAndData(offset, data);
                } else {
                    allocatedOffsets[index] = new OffsetAndData(0, null); // no aquifer
                }
            } else if (key == SpirVGen.MARKER_fluidLevelSampler) {
                int offset = roundUp(currentTail, 4);
                byte[] data;
                try (Arena arena1 = Arena.ofConfined()) {
                    MemorySegment segment = VkStructs.fluidLevelSamplerCreate(
                            arena1,
                            generationShapeConfig.minimumY(),
                            generationShapeConfig.height(),
                            fluidLevelSampler,
                            mappings
                    );
                    data = new byte[(int) segment.byteSize()];
                    MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                }
                currentTail = offset + data.length;
                allocatedOffsets[index] = new OffsetAndData(offset, data);
            } else if (key == SpirVGen.MARKER_oreVeinRandom) {
                if (settings.oreVeins()) {
                    int offset = roundUp(currentTail, 8);
                    byte[] data;
                    try (Arena arena1 = Arena.ofConfined()) {
                        MemorySegment segment = arena1.allocate(24);
                        VkStructs.setRandomState(segment, noiseConfig.getOreRandomDeriver());
                        data = new byte[(int) segment.byteSize()];
                        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                    }
                    currentTail = offset + data.length;
                    allocatedOffsets[index] = new OffsetAndData(offset, data);
                } else {
                    allocatedOffsets[index] = new OffsetAndData(0, null); // no ore veins
                }
            } else if (key == SpirVGen.MARKER_cacheLike_interpolator) {
                int offset = roundUp(currentTail, 8);
                currentTail = offset + generatedSpirVSource.getInterpolatorPrefills() * (horizontalCellCount + 1) * (verticalCellCount + 1) * (horizontalCellCount + 1) * 8;
                allocatedOffsets[index] = new OffsetAndData(offset, null);
            } else if (key == SpirVGen.MARKER_cacheLike_flatCache) {
                int offset = roundUp(currentTail, 8);
                int bufSize = generatedSpirVSource.getFlatCachePrefills() * MathHelper.square(horizontalChunkSize * 4 + 1) * 8;
                if (stage1Cache != null) {
                    currentTail = offset + bufSize;
                    byte[] data = new byte[stage1Cache.flatCaches().length * Double.BYTES];
                    MemorySegment.copy(MemorySegment.ofArray(stage1Cache.flatCaches()), ValueLayout.JAVA_BYTE, 0L, data, 0, data.length);
                    Assertions.assertTrue(data.length == bufSize);
                    allocatedOffsets[index] = new OffsetAndData(offset, data);
                } else {
                    currentTail = offset + bufSize;
                    allocatedOffsets[index] = new OffsetAndData(offset, null);
                }
            } else if (key == SpirVGen.MARKER_cacheLike_cache2d) {
                int offset = roundUp(currentTail, 8);
                currentTail = offset + generatedSpirVSource.getCache2dPrefills() * MathHelper.square(horizontalChunkSize * 16) * 8;
                allocatedOffsets[index] = new OffsetAndData(offset, null);
            } else {
                throw new UnsupportedOperationException("Unsupported key type " + key.getClass().getName());
            }
        }

        ByteBuffer byteBuffer = MemoryUtil.memAlloc(roundUp(currentTail, 32));
        MemorySegment segment = MemorySegment.ofBuffer(byteBuffer);
        VkStructs.worldgen_params$startBiomeX.set(segment, 0L, BiomeCoords.fromChunk(basePos.x()));
        VkStructs.worldgen_params$startBiomeZ.set(segment, 0L, BiomeCoords.fromChunk(basePos.z()));
        VkStructs.worldgen_params$sizeBiomeX.set(segment, 0L, horizontalChunkSize << 2);
        VkStructs.worldgen_params$sizeBiomeZ.set(segment, 0L, horizontalChunkSize << 2);
        VkStructs.worldgen_params$startCellX.set(segment, 0L, Math.floorDiv(basePos.getStartX(), generationShapeConfig.horizontalCellBlockCount()));
        VkStructs.worldgen_params$startCellY.set(segment, 0L, Math.floorDiv(generationShapeConfig.minimumY(), generationShapeConfig.verticalCellBlockCount()));
        VkStructs.worldgen_params$startCellZ.set(segment, 0L, Math.floorDiv(basePos.getStartZ(), generationShapeConfig.horizontalCellBlockCount()));
        VkStructs.worldgen_params$sizeCellX.set(segment, 0L, horizontalCellCount);
        VkStructs.worldgen_params$sizeCellY.set(segment, 0L, verticalCellCount);
        VkStructs.worldgen_params$sizeCellZ.set(segment, 0L, horizontalCellCount);
        VkStructs.worldgen_params$estimateSurfaceHeight_startBiomeX.set(segment, 0L, (basePos.x() - 4) << 2);
        VkStructs.worldgen_params$estimateSurfaceHeight_startBiomeZ.set(segment, 0L, (basePos.z() - 4) << 2);
        VkStructs.worldgen_params$estimateSurfaceHeight_sizeBiomeX.set(segment, 0L, 32 + (4 * horizontalChunkSize));
        VkStructs.worldgen_params$estimateSurfaceHeight_sizeBiomeZ.set(segment, 0L, 32 + (4 * horizontalChunkSize));
        VkStructs.worldgen_params$cache2d_startX.set(segment, 0L, basePos.getStartX());
        VkStructs.worldgen_params$cache2d_startZ.set(segment, 0L, basePos.getStartZ());
        VkStructs.worldgen_params$cache2d_sizeX.set(segment, 0L, (horizontalChunkSize * 16));
        VkStructs.worldgen_params$cache2d_sizeZ.set(segment, 0L, (horizontalChunkSize * 16));
        VkStructs.worldgen_params$offset_estimateSurfaceHeight.set(segment, 0L, allocatedOffsets[generatedSpirVSource.getGlobalDynamicDataOffsets().getInt(SpirVGen.MARKER_estimateSurfaceHeightCache)].offset);
        VkStructs.worldgen_params$genConfig_defaultBlock.set(segment, 0L, mappings.toId(settings.defaultBlock()));
        VkStructs.worldgen_params$genConfig_defaultFluid.set(segment, 0L, mappings.toId(settings.defaultFluid()));
        VkStructs.worldgen_params$offset_aquifer.set(segment, 0L, allocatedOffsets[generatedSpirVSource.getGlobalDynamicDataOffsets().getInt(SpirVGen.MARKER_aquifer)].offset);
        VkStructs.worldgen_params$offset_fluidLevelSampler.set(segment, 0L, allocatedOffsets[generatedSpirVSource.getGlobalDynamicDataOffsets().getInt(SpirVGen.MARKER_fluidLevelSampler)].offset);
        VkStructs.worldgen_params$offset_oreVeinRandom.set(segment, 0L, allocatedOffsets[generatedSpirVSource.getGlobalDynamicDataOffsets().getInt(SpirVGen.MARKER_oreVeinRandom)].offset);

        for (int i = 0, allocatedOffsetsLength = allocatedOffsets.length; i < allocatedOffsetsLength; i++) {
            OffsetAndData allocatedOffset = allocatedOffsets[i];
            Assertions.assertTrue(allocatedOffset != null);
            assert allocatedOffset != null;
            segment.set(ValueLayout.JAVA_INT, VkStructs.GLOBAL_OFFSET_TABLE_START + (long) i * Integer.BYTES, allocatedOffset.offset);
            if (allocatedOffset.offset != 0 && allocatedOffset.data != null) {
                MemorySegment.copy(allocatedOffset.data, 0, segment, ValueLayout.JAVA_BYTE, allocatedOffset.offset, allocatedOffset.data.length);
            }
        }

        return byteBuffer;
    }

    public static Reference2IntLinkedOpenHashMap<Object> transformGlobalDynamicDataOffsets(Reference2IntLinkedOpenHashMap<Object> globalDynamicDataOffsets) {
        Reference2IntLinkedOpenHashMap<Object> newMap = new Reference2IntLinkedOpenHashMap<>();
        ObjectBidirectionalIterator<Reference2IntMap.Entry<Object>> iterator = globalDynamicDataOffsets.reference2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Reference2IntMap.Entry<Object> entry = iterator.next();
            if (entry.getKey() instanceof InterpolatedNoiseSampler sampler) {
                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment memorySegment = BindingsTemplate.interpolated_noise_sampler$create(arena, sampler, true);
                    byte[] bytes = new byte[(int) memorySegment.byteSize()];
                    MemorySegment.copy(memorySegment, ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
                    newMap.put(new ConstantBlob(bytes, 8), entry.getIntValue());
                }
            } else if (entry.getKey() instanceof DoublePerlinNoiseSampler sampler) {
                byte[] bytes = SpirVGen.bytes(sampler);
                newMap.put(new ConstantBlob(bytes, 8), entry.getIntValue());
            } else {
                newMap.put(entry.getKey(), entry.getIntValue());
            }
        }
        return newMap;
    }

    public record ConstantBlob(byte[] data, int alignment) {
    }

}
