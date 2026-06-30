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

package com.ishland.c2me.opts.accel.opencl.common.gen;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.mixin.access.IAquiferSamplerImpl;
import com.ishland.c2me.base.mixin.access.IChunkSection;
import com.ishland.c2me.opts.accel.opencl.common.Config;
import com.ishland.c2me.opts.accel.opencl.common.ducks.PalettedContainerExtension;
import com.ishland.c2me.opts.accel.opencl.common.gen.cache.CLBufferCache;
import com.ishland.c2me.opts.accel.opencl.common.gen.cache.Stage1Cache;
import com.ishland.c2me.opts.accel.opencl.common.integration.zfastnoise.ZFastNoiseBindings;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import com.ishland.c2me.opts.accel.opencl.common.util.TLUtil;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.opencl.common.compiler.GeneratedCLSource;
import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.Workarounds;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class CLServerBatchedBiomeNoiseContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLServerBatchedBiomeNoiseContext.class);

    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    public static final int BATCH_SIZE = Config.useSmallerBatches ? 2 : 4; // must be power of 2, determines axis size
    public static final int BATCH_MASK = BATCH_SIZE - 1;
    public static final int BATCH_SHIFT = Integer.bitCount(BATCH_MASK);

    public static boolean isAligned(int x, int z) {
        return (x & BATCH_MASK) == 0 && (z & BATCH_MASK) == 0;
    }

    private final ChunkPos startingPos;
    private final CLServerWorldContext worldContext;
    private final NoiseChunkGenerator generator;
    private final NoiseConfig noiseConfig;

    public CLServerBatchedBiomeNoiseContext(ChunkPos startingPos, CLServerWorldContext worldContext, NoiseChunkGenerator generator, NoiseConfig noiseConfig) {
        this.startingPos = Objects.requireNonNull(startingPos);
        this.worldContext = Objects.requireNonNull(worldContext);
        this.generator = Objects.requireNonNull(generator);
        this.noiseConfig = Objects.requireNonNull(noiseConfig);
    }

    public CompletableFuture<Void> execute(ChunkLoadingContext context, BoundedRegionArray<ProtoChunk> chunks, BoundedRegionArray<StructureAccessor> structureAccessors) {
        return this.worldContext.getEstimateSurfaceHeightCache().getAreaCache(startingPos.x(), startingPos.z(), BATCH_SIZE, BATCH_SIZE)
                .thenComposeAsync(cacheEntry -> {
                    ByteBuffer rwData = ScopedValue.where(TLUtil.stage1CachePassing, cacheEntry).call(() -> {
                        return CLDataUtil.worldgen_data_root$createForArea(
                                this.startingPos,
                                BATCH_SIZE,
                                chunks,
                                this.generator,
                                this.noiseConfig,
                                structureAccessors,
                                this.worldContext.getClBlockStateMappings(),
                                this.worldContext.getGeneratedCLSource(),
                                cacheEntry
                        );
                    });

                    return this.worldContext.borrowCommandQueue().thenCompose(pair -> {
                        return CompletableFuture.supplyAsync(() -> this.execute0(chunks, structureAccessors, this.worldContext.getClBlockStateMappings(), rwData, cacheEntry, pair.left(), pair.right()), pair.left().getDevice().getExecutor()).thenCompose(Function.identity());
                    });
                }, ((IVanillaChunkManager) context.tacs()).c2me$getSchedulingManager().positionedExecutor(this.startingPos.toLong()));
    }

    private CompletableFuture<Void> execute0(BoundedRegionArray<ProtoChunk> chunks, BoundedRegionArray<StructureAccessor> structureAccessors,
                                             CLBlockStateMappings clBlockStateMappings, ByteBuffer rwData,
                                             Stage1Cache.AreaCacheEntry cacheEntry, OpenCLDevice.BorrowedCommandQueue commandQueue,
                                             CLServerWorldContext.DeviceWithProgram deviceWithProgram) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GeneratedCLSource generatedCLSource = this.worldContext.getGeneratedCLSource();
            IntBuffer errorCodeRet = stack.callocInt(1);

            ChunkGeneratorSettings settings = this.generator.getSettings().value();

            int verticalCellBlockCount = settings.generationShapeConfig().verticalCellBlockCount();
            Assertions.assertTrue(Math.floorDiv(16, settings.generationShapeConfig().horizontalCellBlockCount()) * settings.generationShapeConfig().horizontalCellBlockCount() == 16);
            int horizontalCellsCount = Math.floorDiv(16, settings.generationShapeConfig().horizontalCellBlockCount()) * BATCH_SIZE;
            int verticalCellsCount = Math.floorDiv(settings.generationShapeConfig().height(), verticalCellBlockCount);
            int horizontalSize = 16 * BATCH_SIZE;
            int verticalSize = verticalCellsCount * verticalCellBlockCount;

            int biomeHeight;
            HeightLimitView heightLimitView;
            {
                ProtoChunk startingChunk = chunks.get(this.startingPos.x(), this.startingPos.z());
                heightLimitView = startingChunk.getHeightLimitView();
                biomeHeight = (heightLimitView.getTopSectionCoord() - heightLimitView.getBottomSectionCoord() + 1) * 4;
            }

            if (TLUtil.stage1CachePassing.isBound()) {
                throw new IllegalStateException("Reentrance");
            }

            CLBufferCache.BufferEntry rwBuffer = deviceWithProgram.device().getBufferCache().allocate(
                    CLBufferCache.Type.GEN_BATCHING_RW_DATA,
                    rwData.remaining(),
                    size -> CL12.clCreateBuffer(commandQueue.getContext(), CL12.CL_MEM_READ_WRITE, size, errorCodeRet)
            );
            CLUtil.checkCLError(errorCodeRet);

            int biomeOutCount = biomeHeight * 4 * BATCH_SIZE * 4 * BATCH_SIZE;
            CLBufferCache.BufferEntry biomeOutBuffer = deviceWithProgram.device().getBufferCache().allocate(
                    CLBufferCache.Type.GEN_BATCHING_BIOME_RX,
                    biomeOutCount * 4, // 4 bytes per uint32_t, 4x4 xz axis
                    size -> CL12.clCreateBuffer(commandQueue.getContext(), CL12.CL_MEM_WRITE_ONLY, size, errorCodeRet)
            );
            CLUtil.checkCLError(errorCodeRet);

            long blockOutBufferSize = (long) horizontalSize * verticalSize * horizontalSize * 4;
            CLBufferCache.BufferEntry blockOutBuffer = deviceWithProgram.device().getBufferCache().allocate(
                    CLBufferCache.Type.GEN_BATCHING_BLOCK_STATE_RX,
                    Math.toIntExact(blockOutBufferSize),
                    size -> CL12.clCreateBuffer(commandQueue.getContext(), CL12.CL_MEM_WRITE_ONLY, size, errorCodeRet)
            );
            CLUtil.checkCLError(errorCodeRet);

            IntBuffer biomeOutBufferData = MemoryUtil.memAllocInt(biomeOutCount);
            ByteBuffer blockOutBufferData = MemoryUtil.memAlloc(horizontalSize * verticalSize * horizontalSize);

            LongList eventsToRelease = new LongArrayList();
            LongList kernelsToRelease = new LongArrayList();

            PointerBuffer event = stack.callocPointer(1);

            long rwBufferWriteEvent0;
            long biomeOutBufferReadEvent0;

            CLUtil.checkCLError(CL12.clEnqueueWriteBuffer(commandQueue.getCommandQueue(), rwBuffer.buffer(), false, 0, rwData, null, event));
            eventsToRelease.add(event.get(0));
            rwBufferWriteEvent0 = event.get(0);

            if (generatedCLSource.getBiomeMappings() != null) {
                try (MemoryStack _ = MemoryStack.stackPush()) {
                    PointerBuffer workSize = stack.mallocPointer(3);
                    workSize.put(0, 4 * BATCH_SIZE);
                    workSize.put(1, 4 * BATCH_SIZE);
                    workSize.put(2, biomeHeight);
                    workSize.rewind();

                    PointerBuffer local = stack.callocPointer(3);
                    local.put(0, 8);
                    local.put(1, 8);
                    local.put(2, 1);
                    local.rewind();

                    PointerBuffer eventWaitList = stack.callocPointer(1);
                    eventWaitList.put(0, rwBufferWriteEvent0);
                    eventWaitList.rewind();

                    long kernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.BIOME_MULTINOISE_KERNEL), "df_biome_multinoise_kernel", errorCodeRet);
                    CLUtil.checkCLError(errorCodeRet);
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 0, deviceWithProgram.programConstDataBuffer()));
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 1, rwBuffer.buffer()));
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 2, biomeOutBuffer.buffer()));
                    CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 3, BiomeCoords.fromBlock(this.startingPos.getStartX())));
                    CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 4, BiomeCoords.fromBlock(this.startingPos.getStartZ())));
                    CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 5, BiomeCoords.fromBlock(heightLimitView.getBottomY())));
                    CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 6, 4 * BATCH_SIZE));
                    CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 7, 4 * BATCH_SIZE));
                    CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 8, biomeHeight));
                    CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), kernel, 3, null, workSize, local, eventWaitList, event));
                    eventsToRelease.add(event.get(0));
                    kernelsToRelease.add(kernel);

                    long kernelExecEvent0 = event.get(0);
                    eventWaitList.put(0, kernelExecEvent0);
                    eventWaitList.rewind();

                    CLUtil.checkCLError(CL12.clEnqueueReadBuffer(commandQueue.getCommandQueue(), biomeOutBuffer.buffer(), false, 0, biomeOutBufferData, eventWaitList, event));
                    eventsToRelease.add(event.get(0));
                    biomeOutBufferReadEvent0 = event.get(0);

                    event.put(0, rwBufferWriteEvent0);
                }
            } else {
                biomeOutBufferReadEvent0 = 0L;
            }

            try (MemoryStack _ = MemoryStack.stackPush()) {
                PointerBuffer globalWorkSize = stack.callocPointer(3);
                globalWorkSize.put(0, horizontalCellsCount + 1);
                globalWorkSize.put(2, verticalCellsCount + 1);
                globalWorkSize.put(1, horizontalCellsCount + 1);
                PointerBuffer localWorkSize;
                if (deviceWithProgram.device().getMetadata().supportsNonUniformWorkgroups) {
                    localWorkSize = stack.callocPointer(3);
                    localWorkSize.put(0, 16);
                    localWorkSize.put(1, 16);
                    localWorkSize.put(2, 1);
                } else {
                    localWorkSize = null;
                }
                if (generatedCLSource.getInterpolatorPrefills() != 0) {
                    PointerBuffer prevEvent = event.get(0) != 0L ? stack.mallocPointer(1).put(0, event.get(0)) : null;
                    long kernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.INTERPOLATOR_PREFILL), "df_interpolator_buffer_prefill_kernel", errorCodeRet);
                    CLUtil.checkCLError(errorCodeRet);
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 0, deviceWithProgram.programConstDataBuffer()));
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 1, rwBuffer.buffer()));
                    CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), kernel, 3, null, globalWorkSize, localWorkSize, prevEvent, event));
                    eventsToRelease.add(event.get(0));
                    kernelsToRelease.add(kernel);
                }
            }

            if (settings.hasAquifers()) {
                try (MemoryStack _ = MemoryStack.stackPush()) {

                    // TODO [VanillaCopy] check when aquifer changes
                    int startX = IAquiferSamplerImpl.invokeGetLocalX(this.startingPos.getStartX() - 5) + 0;
                    int startY = IAquiferSamplerImpl.invokeGetLocalY(settings.generationShapeConfig().minimumY() + 1) - 1;
                    int startZ = IAquiferSamplerImpl.invokeGetLocalZ(this.startingPos.getStartZ() - 5) + 0;
                    ChunkPos endChunkPos = new ChunkPos(this.startingPos.x() + BATCH_SIZE - 1, this.startingPos.z() + BATCH_SIZE - 1);
                    int endX = IAquiferSamplerImpl.invokeGetLocalX(endChunkPos.getEndX() + 5 - 1) + 1;
                    int endY = IAquiferSamplerImpl.invokeGetLocalY(settings.generationShapeConfig().minimumY() + settings.generationShapeConfig().height() - 1) + 1;
                    int endZ = IAquiferSamplerImpl.invokeGetLocalZ(endChunkPos.getEndZ() + 5 - 1) + 1;


                    PointerBuffer globalWorkSize = stack.callocPointer(3);
                    globalWorkSize.put(0, endX - startX + 1);
                    globalWorkSize.put(2, endY - startY + 1);
                    globalWorkSize.put(1, endZ - startZ + 1);

                    PointerBuffer prevEvent = event.get(0) != 0L ? stack.mallocPointer(1).put(0, event.get(0)) : null;
                    long kernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.AQUIFER_PREFILL), "aquifer_data_prefill", errorCodeRet);
                    CLUtil.checkCLError(errorCodeRet);
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 0, deviceWithProgram.programConstDataBuffer()));
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 1, rwBuffer.buffer()));
                    CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), kernel, 3, null, globalWorkSize, null, prevEvent, event));
                    eventsToRelease.add(event.get(0));
                    kernelsToRelease.add(kernel);
                }
            }

            if (generatedCLSource.getCache2dPrefills() != 0) {
                try (MemoryStack _ = MemoryStack.stackPush()) {
                    PointerBuffer globalWorkSize = stack.callocPointer(3);
                    globalWorkSize.put(0, horizontalSize);
                    globalWorkSize.put(1, horizontalSize);
                    globalWorkSize.put(2, generatedCLSource.getCache2dPrefills());

                    PointerBuffer localWorkSize = stack.callocPointer(3);
                    localWorkSize.put(0, 8);
                    localWorkSize.put(1, 8);
                    localWorkSize.put(2, 1);

                    PointerBuffer prevEvent = event.get(0) != 0L ? stack.mallocPointer(1).put(0, event.get(0)) : null;
                    long kernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.CACHE2D_PREFILL), "df_cache2d_prefill_kernel", errorCodeRet);
                    CLUtil.checkCLError(errorCodeRet);
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 0, deviceWithProgram.programConstDataBuffer()));
                    CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 1, rwBuffer.buffer()));
                    CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), kernel, 3, null, globalWorkSize, localWorkSize, prevEvent, event));
                    eventsToRelease.add(event.get(0));
                    kernelsToRelease.add(kernel);
                }
            }

            try (MemoryStack _ = MemoryStack.stackPush()) {
                PointerBuffer globalWorkSize = stack.callocPointer(3);
                globalWorkSize.put(0, horizontalSize);
                globalWorkSize.put(2, verticalSize);
                globalWorkSize.put(1, horizontalSize);

                PointerBuffer localWorkSize = stack.callocPointer(3);
                localWorkSize.put(0, 16);
                localWorkSize.put(1, 16);
                localWorkSize.put(2, 1);

                PointerBuffer prevEvent = event.get(0) != 0L ? stack.mallocPointer(1).put(0, event.get(0)) : null;
                long kernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.NOISE_KERNEL), "df_noise_kernel", errorCodeRet);
                CLUtil.checkCLError(errorCodeRet);
                CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 0, deviceWithProgram.programConstDataBuffer()));
                CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 1, rwBuffer.buffer()));
                CLUtil.checkCLError(CL12.clSetKernelArg1p(kernel, 2, blockOutBuffer.buffer()));
                CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 3, this.startingPos.x()));
                CLUtil.checkCLError(CL12.clSetKernelArg1i(kernel, 4, this.startingPos.z()));
                CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), kernel, 3, null, globalWorkSize, localWorkSize, prevEvent, event));
                eventsToRelease.add(event.get(0));
                kernelsToRelease.add(kernel);
            }

            try (MemoryStack _ = MemoryStack.stackPush()) {
                PointerBuffer eventWaitList;
                if (biomeOutBufferReadEvent0 != 0) {
                    eventWaitList = stack.callocPointer(2);
                    Assertions.assertTrue(event.get(0) != 0L);
                    eventWaitList.put(0, event.get(0));
                    eventWaitList.put(1, biomeOutBufferReadEvent0);
                    eventWaitList.rewind();
                } else {
                    eventWaitList = stack.callocPointer(1);
                    Assertions.assertTrue(event.get(0) != 0L);
                    eventWaitList.put(0, event.get(0));
                    eventWaitList.rewind();
                }

                CLUtil.checkCLError(CL12.clEnqueueReadBuffer(commandQueue.getCommandQueue(), blockOutBuffer.buffer(), false, 0, blockOutBufferData, eventWaitList, event));
                eventsToRelease.add(event.get(0));
            }

            if (Config.doExplicitFlushes || deviceWithProgram.device().getWorkarounds().contains(Workarounds.Reference.REQUIRE_EXPLICIT_FLUSHES)) {
                CLUtil.checkCLError(CL12.clFlush(commandQueue.getCommandQueue()));
            }

            deviceWithProgram.device().getEventCallbackManager().registerCallback(
                    event.get(0),
                    CL12.CL_COMPLETE,
                    (_, event_command_status, _) -> {
                        GlobalExecutors.prioritizedScheduler.executor(16).execute(() -> {
                            try {
                                try {
                                    if (event_command_status != CL12.CL_COMPLETE) {
                                        LOGGER.error("OpenCL command failed: {}", event_command_status);
                                        future.completeExceptionally(new RuntimeException("OpenCL command failed: " + event_command_status));
                                    } else {
                                        // biomes
                                        if (generatedCLSource.getBiomeMappings() != null) {
                                            writeBiomes(chunks, generatedCLSource, biomeHeight, biomeOutBufferData);
                                        } else {
                                            genBiomesFallback(chunks, structureAccessors);
                                        }

                                        // noise

                                        writeBlocks(chunks, clBlockStateMappings, verticalSize, settings, horizontalSize, blockOutBufferData);

                                        future.complete(null);
                                    }
                                } finally {
                                    try {
                                        deviceWithProgram.device().getExecutor().execute(() -> {
                                            deviceWithProgram.device().getBufferCache().returnBuffer(CLBufferCache.Type.GEN_BATCHING_RW_DATA, rwBuffer);
                                            deviceWithProgram.device().getBufferCache().returnBuffer(CLBufferCache.Type.GEN_BATCHING_BLOCK_STATE_RX, blockOutBuffer);
                                            deviceWithProgram.device().getBufferCache().returnBuffer(CLBufferCache.Type.GEN_BATCHING_BIOME_RX, biomeOutBuffer);
                                            for (int i = 0; i < eventsToRelease.size(); i++) {
                                                try {
                                                    CLUtil.checkCLError(CL12.clReleaseEvent(eventsToRelease.getLong(i)));
                                                } catch (Throwable t) {
                                                    LOGGER.error("Failed to release event", t);
                                                }
                                            }
                                            for (int i = 0; i < kernelsToRelease.size(); i++) {
                                                try {
                                                    CLUtil.checkCLError(CL12.clReleaseKernel(kernelsToRelease.getLong(i)));
                                                } catch (Throwable t) {
                                                    LOGGER.error("Failed to release kernel", t);
                                                }
                                            }
                                        });

                                        MemoryUtil.memFree(rwData);
                                        MemoryUtil.memFree(blockOutBufferData);
                                        MemoryUtil.memFree(biomeOutBufferData);
                                        commandQueue.close();
                                    } catch (Throwable t) {
                                        LOGGER.error("Error cleaning up", t);
                                    }
                                }
                            } catch (Throwable t) {
                                future.completeExceptionally(t);
                            }
                        });
                    }
            );
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        future.exceptionally(throwable -> {
            LOGGER.error("CLServerBatchedBiomeNoiseContext threw exception", throwable);
            return null;
        });

        return future
                .thenApply(Function.identity())
                .orTimeout(120, TimeUnit.SECONDS)
                .exceptionallyCompose(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        return CompletableFuture.failedFuture(new TimeoutException(String.format("CLServerBatchedBiomeNoiseContext timed out for batch %s on %s", this.startingPos.toString(), deviceWithProgram.device().toString())));
                    } else {
                        return CompletableFuture.failedFuture(throwable);
                    }
                });
    }

    private void genBiomesFallback(BoundedRegionArray<ProtoChunk> chunks, BoundedRegionArray<StructureAccessor> structureAccessors) {
        for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
            for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
                ProtoChunk chunk = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ);
                if (chunk.getStatus().isAtLeast(ChunkStatus.BIOMES)) {
                    continue;
                }
                this.generator.populateBiomes(this.noiseConfig, Blender.getNoBlending(), structureAccessors.get(chunk.getPos().x(), chunk.getPos().z()), chunk); // fallback
                chunk.setStatus(ChunkStatus.BIOMES);
            }
        }
    }

    private void writeBiomes(BoundedRegionArray<ProtoChunk> chunks, GeneratedCLSource generatedCLSource, int biomeHeight, IntBuffer biomeOutBufferData) {
        RegistryEntry<Biome>[] biomeMappings = generatedCLSource.getBiomeMappings();
        ProtoChunk startingChunk = chunks.get(this.startingPos.x(), this.startingPos.z());

        int curSectionIndex = startingChunk.getSectionIndex(startingChunk.getBottomY());
        ChunkSection[] chunkSection = new ChunkSection[BATCH_SIZE * BATCH_SIZE];
        PalettedContainer<RegistryEntry<Biome>>[] sectionBiome = new PalettedContainer[BATCH_SIZE * BATCH_SIZE];
        BitSet writeProtectedMask = new BitSet(BATCH_SIZE * BATCH_SIZE);

        for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
            for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
                int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                ProtoChunk chunk = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ);
                boolean writeProtected = chunk.getStatus().isAtLeast(ChunkStatus.BIOMES);
                if (writeProtected) {
                    writeProtectedMask.set(idx);
                    continue;
                }
                chunkSection[idx] = chunk.getSection(curSectionIndex);
                sectionBiome[idx] = chunkSection[idx].getBiomeContainer().slice();
            }
        }

        for (int y = 0; y < biomeHeight; y ++) {
            int biomeY = BiomeCoords.fromBlock(startingChunk.getBottomY()) + y;
            int sectionIndex = startingChunk.getSectionIndex(BiomeCoords.toBlock(biomeY));
            if (sectionIndex != curSectionIndex) {
                for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
                    for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
                        int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                        if (writeProtectedMask.get(idx)) continue;
                        ((IChunkSection) chunkSection[idx]).setBiomeContainer(sectionBiome[idx]);
                        chunkSection[idx] = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ).getSection(sectionIndex);
                        sectionBiome[idx] = chunkSection[idx].getBiomeContainer().slice();
                    }
                }
                curSectionIndex = sectionIndex;
            }

            int horizontalBiomeSize = 4 << BATCH_SHIFT;
            for (int z = 0; z < horizontalBiomeSize; z ++) {
                for (int x = 0; x < horizontalBiomeSize; x++) {
                    int value = biomeOutBufferData.get((y * horizontalBiomeSize + z) * horizontalBiomeSize + x);
                    int chunkOffX = (x >> 2) & BATCH_MASK;
                    int chunkOffZ = (z >> 2) & BATCH_MASK;
                    int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                    if (writeProtectedMask.get(idx)) continue;
                    ((PalettedContainerExtension<RegistryEntry<Biome>>) sectionBiome[idx]).c2me$setUnsafe(x & 3, biomeY & 3, z & 3, biomeMappings[value]);
                }
            }
        }
        for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
            for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
                int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                if (writeProtectedMask.get(idx)) continue;

                ((IChunkSection) chunkSection[idx]).setBiomeContainer(sectionBiome[idx]);
                ProtoChunk chunk = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ);
                chunk.setStatus(ChunkStatus.BIOMES);
            }
        }
    }

    private void writeBlocks(BoundedRegionArray<ProtoChunk> chunks, CLBlockStateMappings clBlockStateMappings, int verticalSize, ChunkGeneratorSettings settings, int horizontalSize, ByteBuffer blockOutBufferData) {
        if (ZFastNoiseBindings.MH_FastCopyBufferDataIntoChunks$copyData != null) {
            ZFastNoiseBindings.call_FastCopyBufferDataIntoChunks$copyData(
                    chunks,
                    clBlockStateMappings.getIdToBlockState(),
                    verticalSize,
                    settings,
                    horizontalSize,
                    blockOutBufferData,
                    this.startingPos,
                    BATCH_SIZE
            );
            return;
        }

        ProtoChunk startingChunk = chunks.get(this.startingPos.x(), this.startingPos.z());

        int curSectionIndex = startingChunk.getSectionIndex(startingChunk.getBottomY());
        ChunkSection[] chunkSection = new ChunkSection[BATCH_SIZE * BATCH_SIZE];
        int writeProtectedMask = 0;

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
            for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
                int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                ProtoChunk chunk = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ);
                boolean writeProtected = chunk.getStatus().isAtLeast(ChunkStatus.NOISE);
                writeProtectedMask |= writeProtected ? (1 << idx) : 0;
                if (writeProtected) continue;
                chunkSection[idx] = chunk.getSection(curSectionIndex);
                chunkSection[idx].lock();
            }
        }

        for (int y = 0; y < verticalSize; y++) {
            int blockY = settings.generationShapeConfig().minimumY() + y;
            int sectionIndex = startingChunk.getSectionIndex(blockY);
            if (sectionIndex != curSectionIndex) {
                for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
                    for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
                        int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                        if ((writeProtectedMask & (1L << idx)) != 0) continue;
                        chunkSection[idx].calculateCounts();
                        chunkSection[idx].unlock();
                        chunkSection[idx] = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ).getSection(sectionIndex);
                        chunkSection[idx].lock();
                    }
                }
                curSectionIndex = sectionIndex;
            }

            for (int z = 0; z < horizontalSize; z++) {
                for (int x = 0; x < horizontalSize; x++) {
                    int index = y * horizontalSize * horizontalSize + z * horizontalSize + x;
                    byte value = blockOutBufferData.get(index);
                    int chunkOffX = (x >> 4) & BATCH_MASK;
                    int chunkOffZ = (z >> 4) & BATCH_MASK;
                    int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                    if ((writeProtectedMask & (1L << idx)) != 0) continue;
                    boolean needsFluidTick = (value & (byte) ((byte) 1) << ((byte) 7)) != 0;
                    int blockIdx = value & ((byte) ~(((byte) 1) << ((byte) 7)));
                    BlockState blockState = clBlockStateMappings.getBlockState(blockIdx);
                    Assertions.assertTrue(blockState != null);
                    if (blockState != AIR) {
                        ((PalettedContainerExtension<BlockState>) chunkSection[idx].getBlockStateContainer()).c2me$setUnsafe(x & 15, blockY & 15, z & 15, blockState);
                        if (needsFluidTick) {
                            ProtoChunk chunk = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ);
                            mutablePos.set(x + chunk.getPos().getStartX(), blockY, z + chunk.getPos().getStartZ());
                            chunk.markBlockForPostProcessing(mutablePos);
                        }
                    }
                }
            }
        }

        for (int chunkOffZ = 0; chunkOffZ < BATCH_SIZE; chunkOffZ++) {
            for (int chunkOffX = 0; chunkOffX < BATCH_SIZE; chunkOffX++) {
                int idx = (chunkOffZ << BATCH_SHIFT) + chunkOffX;
                if ((writeProtectedMask & (1L << idx)) != 0) continue;

                chunkSection[idx].calculateCounts();
                chunkSection[idx].unlock();

                ProtoChunk chunk = chunks.get(this.startingPos.x() + chunkOffX, this.startingPos.z() + chunkOffZ);
                Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG));
                chunk.setStatus(ChunkStatus.NOISE);
            }
        }
    }


}
