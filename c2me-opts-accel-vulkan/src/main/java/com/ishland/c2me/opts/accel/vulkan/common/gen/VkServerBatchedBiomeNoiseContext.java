package com.ishland.c2me.opts.accel.vulkan.common.gen;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.mixin.access.IAquiferSamplerImpl;
import com.ishland.c2me.base.mixin.access.IChunkSection;
import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.ducks.PalettedContainerExtension;
import com.ishland.c2me.opts.accel.vulkan.common.gen.cache.Stage1Cache;
import com.ishland.c2me.opts.accel.vulkan.common.integration.zfastnoise.ZFastNoiseBindings;
import com.ishland.c2me.opts.accel.vulkan.common.util.TLUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.GeneratedSpirVSource;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.flowsched.util.Assertions;
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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkSubmitInfo;

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

public class VkServerBatchedBiomeNoiseContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(VkServerBatchedBiomeNoiseContext.class);

    private static final BlockState AIR = Blocks.AIR.getDefaultState();


    public static final int BATCH_SIZE = Config.useSmallerBatches ? 2 : 4;
    public static final int BATCH_MASK = BATCH_SIZE - 1;
    public static final int BATCH_SHIFT = Integer.bitCount(BATCH_MASK);

    public static boolean isAligned(int x, int z) {
        return (x & BATCH_MASK) == 0 && (z & BATCH_MASK) == 0;
    }

    private final ChunkPos startingPos;
    private final VkServerWorldContext worldContext;
    private final NoiseChunkGenerator generator;
    private final NoiseConfig noiseConfig;

    public VkServerBatchedBiomeNoiseContext(ChunkPos startingPos, VkServerWorldContext worldContext, NoiseChunkGenerator generator, NoiseConfig noiseConfig) {
        this.startingPos = Objects.requireNonNull(startingPos);
        this.worldContext = Objects.requireNonNull(worldContext);
        this.generator = Objects.requireNonNull(generator);
        this.noiseConfig = Objects.requireNonNull(noiseConfig);
    }

    public CompletableFuture<Void> execute(ChunkLoadingContext context, BoundedRegionArray<ProtoChunk> chunks, BoundedRegionArray<StructureAccessor> structureAccessors) {
        return this.worldContext.getEstimateSurfaceHeightCache().getAreaCache(startingPos.x(), startingPos.z(), BATCH_SIZE, BATCH_SIZE)
                .thenComposeAsync(cacheEntry -> {
                    ByteBuffer rwData = ScopedValue.where(TLUtil.stage1CachePassing, cacheEntry).call(() -> CLDataUtil.worldgen_data_root$createForArea(
                            this.startingPos,
                            BATCH_SIZE,
                            chunks,
                            this.generator,
                            this.noiseConfig,
                            structureAccessors,
                            this.worldContext.getBlockStateMappings(),
                            this.worldContext.getGeneratedSpirVSource(),
                            cacheEntry
                    ));

                    return this.worldContext.borrowSubmission().thenCompose(pair -> CompletableFuture.supplyAsync(() -> this.execute0(chunks, structureAccessors,
                                    this.worldContext.getBlockStateMappings(), rwData, cacheEntry,
                                    pair.left(), pair.right()),
                            GlobalExecutors.prioritizedScheduler.executor(16)).thenCompose(Function.identity()));
                }, ((IVanillaChunkManager) context.tacs()).c2me$getSchedulingManager().positionedExecutor(this.startingPos.toLong()));
    }

    private CompletableFuture<Void> execute0(BoundedRegionArray<ProtoChunk> chunks,
                                             BoundedRegionArray<StructureAccessor> structureAccessors,
                                             CLBlockStateMappings blockStateMappings, ByteBuffer rwData,
                                             Stage1Cache.AreaCacheEntry cacheEntry,
                                             SubmissionPermits.Borrowed submission,
                                             VkServerWorldContext.DeviceWithPipelines target) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        VulkanDevice device = target.device();
        GeneratedSpirVSource source = this.worldContext.getGeneratedSpirVSource();
        ChunkGeneratorSettings settings = this.generator.getSettings().value();

        int verticalCellBlockCount = settings.generationShapeConfig().verticalCellBlockCount();
        Assertions.assertTrue(Math.floorDiv(16, settings.generationShapeConfig().horizontalCellBlockCount())
                * settings.generationShapeConfig().horizontalCellBlockCount() == 16);
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

        int biomeOutCount = biomeHeight * 4 * BATCH_SIZE * 4 * BATCH_SIZE;
        int blockOutSize = horizontalSize * verticalSize * horizontalSize;

        VulkanBuffer rwBuffer = null;
        VulkanBuffer biomeOutBuffer = null;
        VulkanBuffer biomeReadback = null;
        VulkanBuffer blockReadback = null;
        VulkanBuffer blockOutBuffer = null;
        VkCommandBuffer commandBuffer = null;
        VulkanCommandPool commandPool = null;
        long fence = VK10.VK_NULL_HANDLE;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            rwBuffer = VulkanBuffer.createStorageBuffer(device, rwData.remaining());
            biomeOutBuffer = VulkanBuffer.createDeviceLocalStorageBuffer(device, biomeOutCount * Integer.BYTES);
            blockOutBuffer = VulkanBuffer.createDeviceLocalStorageBuffer(device, blockOutSize);
            biomeReadback = VulkanBuffer.createReadbackBuffer(device, biomeOutCount * Integer.BYTES);
            blockReadback = VulkanBuffer.createReadbackBuffer(device, blockOutSize);

            rwBuffer.writeToBuffer(rwData, 0);
            rwBuffer.flush(0, rwData.remaining());

            long constData = target.constDataBuffer().deviceAddress();
            long rwAddress = rwBuffer.deviceAddress();

            commandPool = device.getCommandPool(VulkanDevice.WorkQueue.COMPUTE);
            commandBuffer = beginCommandBuffer(device, commandPool, stack);


            // Dependencies, rather than a barrier after every dispatch:
            //   biome        reads the flat cache, writes its own buffer  -- independent
            //   interpolator reads the flat cache                         -- independent
            //   aquifer      reads params and its own region              -- independent
            //   cache2d      reads the flat cache AND the interpolator    -- after interpolator
            //   noise        reads all of them                            -- after everything
            // So the first three are issued back to back and left to overlap.
            if (source.getBiomeMappings() != null) {
                dispatch(commandBuffer, stack, target, SpirVGen.ProgramType.BIOME_MULTINOISE_KERNEL,
                        constData, rwAddress, biomeOutBuffer.deviceAddress(),
                        BiomeCoords.fromBlock(this.startingPos.getStartX()),
                        BiomeCoords.fromBlock(this.startingPos.getStartZ()),
                        BiomeCoords.fromBlock(heightLimitView.getBottomY()),
                        groups(4 * BATCH_SIZE, 8), groups(4 * BATCH_SIZE, 8), biomeHeight);
            }

            if (source.getInterpolatorPrefills() != 0) {
                int latticeExtent = horizontalCellsCount + 1;
                dispatch(commandBuffer, stack, target, SpirVGen.ProgramType.INTERPOLATOR_PREFILL,
                        constData, rwAddress, 0L, latticeExtent, 0, 0,
                        groups(latticeExtent, 8), groups(latticeExtent, 8), verticalCellsCount + 1);
            }

            if (settings.hasAquifers()) {
                // TODO [VanillaCopy] check when aquifer changes
                int startX = IAquiferSamplerImpl.invokeGetLocalX(this.startingPos.getStartX() - 5);
                int startY = IAquiferSamplerImpl.invokeGetLocalY(settings.generationShapeConfig().minimumY() + 1) - 1;
                int startZ = IAquiferSamplerImpl.invokeGetLocalZ(this.startingPos.getStartZ() - 5);
                ChunkPos endChunkPos = new ChunkPos(this.startingPos.x() + BATCH_SIZE - 1, this.startingPos.z() + BATCH_SIZE - 1);
                int endX = IAquiferSamplerImpl.invokeGetLocalX(endChunkPos.getEndX() + 5 - 1) + 1;
                int endY = IAquiferSamplerImpl.invokeGetLocalY(settings.generationShapeConfig().minimumY()
                        + settings.generationShapeConfig().height() - 1) + 1;
                int endZ = IAquiferSamplerImpl.invokeGetLocalZ(endChunkPos.getEndZ() + 5 - 1) + 1;

                dispatch(commandBuffer, stack, target, SpirVGen.ProgramType.AQUIFER_PREFILL,
                        constData, rwAddress, 0L, 0, 0, 0,
                        groups(endX - startX + 1, 8), groups(endZ - startZ + 1, 8), endY - startY + 1);
            }

            memoryBarrier(commandBuffer, stack);

            if (source.getCache2dPrefills() != 0) {
                dispatch(commandBuffer, stack, target, SpirVGen.ProgramType.CACHE2D_PREFILL,
                        constData, rwAddress, 0L, 0, 0, 0,
                        groups(horizontalSize, 8), groups(horizontalSize, 8), source.getCache2dPrefills());
                memoryBarrier(commandBuffer, stack);
            }

            dispatch(commandBuffer, stack, target, SpirVGen.ProgramType.NOISE_KERNEL,
                    constData, rwAddress, blockOutBuffer.deviceAddress(),
                    this.startingPos.x(), this.startingPos.z(), 0,
                    groups(horizontalSize, 16), groups(horizontalSize, 16), verticalSize);

            transferBarrier(commandBuffer, stack);
            if (source.getBiomeMappings() != null) {
                copyBuffer(commandBuffer, stack, biomeOutBuffer, biomeReadback,
                        (long) biomeOutCount * Integer.BYTES);
            }
            copyBuffer(commandBuffer, stack, blockOutBuffer, blockReadback, blockOutSize);

            VkUtil.check(VK10.vkEndCommandBuffer(commandBuffer));

            fence = device.acquireFence();

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack)
                    .sType$Default()
                    .pCommandBuffers(stack.pointers(commandBuffer));
            device.submitCommands(VulkanDevice.WorkQueue.COMPUTE, submitInfo, fence);
        } catch (Throwable t) {
            releaseAll(device, commandPool, rwData, rwBuffer, biomeOutBuffer, blockOutBuffer,
                    biomeReadback, blockReadback, commandBuffer, fence);
            submission.close();
            return CompletableFuture.failedFuture(t);
        }

        final long waitFence = fence;
        final VkCommandBuffer waitCommandBuffer = commandBuffer;
        final VulkanCommandPool waitCommandPool = commandPool;
        final VulkanBuffer waitRw = rwBuffer;
        final VulkanBuffer waitBiome = biomeOutBuffer;
        final VulkanBuffer waitBlock = blockOutBuffer;
        final VulkanBuffer waitBiomeReadback = biomeReadback;
        final VulkanBuffer waitBlockReadback = blockReadback;

        GlobalExecutors.prioritizedScheduler.executor(16).execute(() -> {
            try {
                VkUtil.check(VK10.vkWaitForFences(device.getDevice(), waitFence, true, Long.MAX_VALUE));

                if (source.getBiomeMappings() != null) {
                    waitBiomeReadback.invalidate(0, (long) biomeOutCount * Integer.BYTES);
                    IntBuffer biomeOutBufferData = MemoryUtil.memIntBuffer(waitBiomeReadback.mappedData(), biomeOutCount);
                    writeBiomes(chunks, source, biomeHeight, biomeOutBufferData);
                } else {
                    genBiomesFallback(chunks, structureAccessors);
                }

                waitBlockReadback.invalidate(0L, blockOutSize);
                ByteBuffer blockOutBufferData = MemoryUtil.memByteBuffer(waitBlockReadback.mappedData(), blockOutSize);
                writeBlocks(chunks, blockStateMappings, verticalSize, settings, horizontalSize, blockOutBufferData);

                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            } finally {
                releaseAll(device, waitCommandPool, rwData, waitRw, waitBiome,
                        waitBlock, waitBiomeReadback, waitBlockReadback, waitCommandBuffer, waitFence);
                submission.close();
            }
        });

        future.exceptionally(throwable -> {
            LOGGER.error("VkServerBatchedBiomeNoiseContext threw exception", throwable);
            return null;
        });

        return future
                .thenApply(Function.identity())
                .orTimeout(120, TimeUnit.SECONDS)
                .exceptionallyCompose(throwable -> throwable instanceof TimeoutException
                        ? CompletableFuture.failedFuture(new TimeoutException(String.format(
                                "VkServerBatchedBiomeNoiseContext timed out for batch %s on %s",
                                this.startingPos, device)))
                        : CompletableFuture.failedFuture(throwable));
    }

    private static int groups(int globalSize, int localSize) {
        return Math.floorDiv(globalSize + localSize - 1, localSize);
    }

    private static void dispatch(VkCommandBuffer commandBuffer, MemoryStack stack,
                                 VkServerWorldContext.DeviceWithPipelines target, SpirVGen.ProgramType type,
                                 long constData, long rwData, long out, int arg0, int arg1, int arg2,
                                 int groupsX, int groupsY, int groupsZ) {
        VulkanPipeline pipeline = target.getPipeline(type);
        VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.getPipeline());

        ByteBuffer data = stack.calloc(VkPushConstants.SIZE);
        data.putLong(VkPushConstants.OFFSET_CONST_DATA, constData);
        data.putLong(VkPushConstants.OFFSET_RW_DATA, rwData);
        data.putLong(VkPushConstants.OFFSET_OUT_DATA, out);
        data.putInt(VkPushConstants.OFFSET_ARG0, arg0);
        data.putInt(VkPushConstants.OFFSET_ARG1, arg1);
        data.putInt(VkPushConstants.OFFSET_ARG2, arg2);
        VK10.vkCmdPushConstants(commandBuffer, pipeline.getPipelineLayout(),
                VK10.VK_SHADER_STAGE_COMPUTE_BIT, 0, data);

        VK10.vkCmdDispatch(commandBuffer, groupsX, groupsY, groupsZ);
    }

    private static VkCommandBuffer beginCommandBuffer(VulkanDevice device, VulkanCommandPool pool,
                                                      MemoryStack stack) {
        VkCommandBuffer commandBuffer = pool.acquire();
        VkUtil.check(VK10.vkBeginCommandBuffer(commandBuffer, VkCommandBufferBeginInfo.calloc(stack)
                .sType$Default()
                .flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)));
        return commandBuffer;
    }

    private static void transferBarrier(VkCommandBuffer commandBuffer, MemoryStack stack) {
        VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc(1, stack)
                .sType$Default()
                .srcAccessMask(VK10.VK_ACCESS_SHADER_WRITE_BIT)
                .dstAccessMask(VK10.VK_ACCESS_TRANSFER_READ_BIT);
        VK10.vkCmdPipelineBarrier(commandBuffer,
                VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                0, barrier, null, null);
    }

    private static void copyBuffer(VkCommandBuffer commandBuffer, MemoryStack stack,
                                   VulkanBuffer source, VulkanBuffer destination, long size) {
        VkBufferCopy.Buffer region = VkBufferCopy.calloc(1, stack).srcOffset(0).dstOffset(0).size(size);
        VK10.vkCmdCopyBuffer(commandBuffer, source.getBuffer(), destination.getBuffer(), region);
    }

    private static void memoryBarrier(VkCommandBuffer commandBuffer, MemoryStack stack) {
        VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc(1, stack)
                .sType$Default()
                .srcAccessMask(VK10.VK_ACCESS_SHADER_WRITE_BIT)
                .dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT | VK10.VK_ACCESS_SHADER_WRITE_BIT);
        VK10.vkCmdPipelineBarrier(commandBuffer,
                VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                0, barrier, null, null);
    }

    private static void releaseAll(VulkanDevice device, VulkanCommandPool commandPool, ByteBuffer rwData,
                                   VulkanBuffer rwBuffer, VulkanBuffer biomeOut,
                                   VulkanBuffer blockOut, VulkanBuffer biomeReadback,
                                   VulkanBuffer blockReadback, VkCommandBuffer commandBuffer, long fence) {
        try {
            if (rwBuffer != null) rwBuffer.close();
            if (biomeOut != null) biomeOut.close();
            if (blockOut != null) blockOut.close();
            if (biomeReadback != null) biomeReadback.close();
            if (blockReadback != null) blockReadback.close();
            if (commandBuffer != null && commandPool != null) commandPool.release(commandBuffer);
            device.releaseFence(fence);
        } catch (Throwable t) {
            LOGGER.error("Failed to release batch resources", t);
        } finally {
            if (rwData != null) MemoryUtil.memFree(rwData);
        }
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

    private void writeBiomes(BoundedRegionArray<ProtoChunk> chunks, GeneratedSpirVSource generatedSpirVSource, int biomeHeight, IntBuffer biomeOutBufferData) {
        RegistryEntry<Biome>[] biomeMappings = generatedSpirVSource.getBiomeMappings();
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

    private void writeBlocks(BoundedRegionArray<ProtoChunk> chunks, CLBlockStateMappings blockStateMappings, int verticalSize, ChunkGeneratorSettings settings, int horizontalSize, ByteBuffer blockOutBufferData) {
        if (ZFastNoiseBindings.MH_FastCopyBufferDataIntoChunks$copyData != null) {
            ZFastNoiseBindings.call_FastCopyBufferDataIntoChunks$copyData(
                    chunks,
                    blockStateMappings.getIdToBlockState(),
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
                    BlockState blockState = blockStateMappings.getBlockState(blockIdx);
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
