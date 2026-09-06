package com.ishland.c2me.opts.accel.vulkan.common.gen.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.opts.accel.vulkan.common.gen.CLDataUtil;
import com.ishland.c2me.opts.accel.vulkan.common.gen.SubmissionPermits;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VkPushConstants;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VkServerWorldContext;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VkUtil;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VulkanBuffer;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VulkanCommandPool;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VulkanDevice;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class Stage1Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stage1Cache.class);

    private static final int CACHE_CHUNK_WIDTH = Config.useSmallerBatches ? 8 : 16;
    private static final int CACHE_CHUNK_WIDTH_SHIFT = Integer.numberOfTrailingZeros(CACHE_CHUNK_WIDTH);

    private static final int CACHE_WIDTH = CACHE_CHUNK_WIDTH << 2;
    private static final int CACHE_WIDTH_MASK = CACHE_WIDTH - 1;
    private static final int CACHE_WIDTH_SHIFT = Integer.numberOfTrailingZeros(CACHE_WIDTH);

    private final AsyncLoadingCache<CacheIndex, RawCacheEntry> cache;
    private final VkServerWorldContext worldContext;

    public Stage1Cache(VkServerWorldContext worldContext) {
        this.worldContext = Objects.requireNonNull(worldContext, "worldContext must not be null");
        this.cache = Caffeine.newBuilder()
                .maximumSize(256)
                .executor(GlobalExecutors.asyncScheduler)
                .buildAsync(this::asyncLoad0);
    }

    private CompletableFuture<RawCacheEntry> asyncLoad0(CacheIndex cacheIndex, Executor executor) {
        return this.worldContext.borrowSubmission().thenCompose(pair ->
                CompletableFuture.supplyAsync(() -> this.execute0(cacheIndex, pair),
                        GlobalExecutors.prioritizedScheduler.executor(16)).thenCompose(Function.identity()));
    }

    private @NotNull CompletableFuture<RawCacheEntry> execute0(
            CacheIndex cacheIndex,
            Pair<SubmissionPermits.Borrowed, VkServerWorldContext.DeviceWithPipelines> pair) {

        CompletableFuture<RawCacheEntry> future = new CompletableFuture<>();

        SubmissionPermits.Borrowed submission = pair.left();
        VkServerWorldContext.DeviceWithPipelines target = pair.right();
        VulkanDevice device = target.device();

        int flatCachePrefills = this.worldContext.getGeneratedSpirVSource().getFlatCachePrefills();

        ByteBuffer rwData = CLDataUtil.worldgen_data_root$createForFlatCacheOnly(
                new ChunkPos(cacheIndex.x << CACHE_CHUNK_WIDTH_SHIFT, cacheIndex.z << CACHE_CHUNK_WIDTH_SHIFT),
                CACHE_CHUNK_WIDTH,
                this.worldContext.getGeneratedSpirVSource(),
                null,
                false
        );

        VulkanBuffer rwBuffer = null;
        VulkanBuffer surfaceHeightOut = null;
        VulkanBuffer flatCacheOut = null;
        VulkanBuffer surfaceHeightReadback = null;
        VulkanBuffer flatCacheReadback = null;
        long fence = VK10.VK_NULL_HANDLE;
        VkCommandBuffer commandBuffer = null;
        VulkanCommandPool commandPool = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            rwBuffer = VulkanBuffer.createStorageBuffer(device, rwData.remaining());
            int surfaceHeightSize = CACHE_WIDTH * CACHE_WIDTH * Integer.BYTES;
            int flatCacheSize = Math.max(flatCachePrefills * CACHE_WIDTH * CACHE_WIDTH * Double.BYTES, 1);
            surfaceHeightOut = VulkanBuffer.createDeviceLocalStorageBuffer(device, surfaceHeightSize);
            flatCacheOut = VulkanBuffer.createDeviceLocalStorageBuffer(device, flatCacheSize);
            surfaceHeightReadback = VulkanBuffer.createReadbackBuffer(device, surfaceHeightSize);
            flatCacheReadback = VulkanBuffer.createReadbackBuffer(device, flatCacheSize);

            rwBuffer.writeToBuffer(rwData, 0);
            rwBuffer.flush(0, rwData.remaining());

            long constData = target.constDataBuffer().deviceAddress();
            long rwAddress = rwBuffer.deviceAddress();

            commandPool = device.getCommandPool(VulkanDevice.WorkQueue.COMPUTE);
            commandBuffer = beginCommandBuffer(device, commandPool, stack);

            if (flatCachePrefills != 0) {
                long pipeline = target.getPipeline(SpirVGen.ProgramType.FLAT_CACHE_PREFILL).getPipeline();
                long layout = target.getPipeline(SpirVGen.ProgramType.FLAT_CACHE_PREFILL).getPipelineLayout();
                VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
                for (int i = 0; i < flatCachePrefills; i++) {
                    pushConstants(commandBuffer, layout, stack, constData, rwAddress,
                            flatCacheOut.deviceAddress(),
                            cacheIndex.x << CACHE_CHUNK_WIDTH_SHIFT, cacheIndex.z << CACHE_CHUNK_WIDTH_SHIFT, i);
                    VK10.vkCmdDispatch(commandBuffer, CACHE_WIDTH / 16, CACHE_WIDTH / 16, 1);
                }
                memoryBarrier(commandBuffer, stack);
            }

            {
                long pipeline = target.getPipeline(SpirVGen.ProgramType.ESTIMATE_SURFACE_HEIGHT).getPipeline();
                long layout = target.getPipeline(SpirVGen.ProgramType.ESTIMATE_SURFACE_HEIGHT).getPipelineLayout();
                VK10.vkCmdBindPipeline(commandBuffer, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
                pushConstants(commandBuffer, layout, stack, constData, rwAddress,
                        surfaceHeightOut.deviceAddress(),
                        cacheIndex.x << CACHE_CHUNK_WIDTH_SHIFT, cacheIndex.z << CACHE_CHUNK_WIDTH_SHIFT, CACHE_WIDTH);
                VK10.vkCmdDispatch(commandBuffer, CACHE_WIDTH / 8, CACHE_WIDTH / 8, 1);
            }

            transferBarrier(commandBuffer, stack);
            copyBuffer(commandBuffer, stack, surfaceHeightOut, surfaceHeightReadback,
                    surfaceHeightSize);
            if (flatCachePrefills != 0) {
                copyBuffer(commandBuffer, stack, flatCacheOut, flatCacheReadback, flatCacheSize);
            }

            VkUtil.check(VK10.vkEndCommandBuffer(commandBuffer));

            fence = device.acquireFence();

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack)
                    .sType$Default()
                    .pCommandBuffers(stack.pointers(commandBuffer));
            device.submitCommands(VulkanDevice.WorkQueue.COMPUTE, submitInfo, fence);
        } catch (Throwable t) {
            releaseAll(device, commandPool, rwData, rwBuffer, surfaceHeightOut,
                    surfaceHeightReadback, flatCacheReadback, flatCacheOut, commandBuffer, fence);
            submission.close();
            return CompletableFuture.failedFuture(t);
        }

        final long waitFence = fence;
        final VkCommandBuffer waitCommandBuffer = commandBuffer;
        final VulkanCommandPool waitCommandPool = commandPool;
        final VulkanBuffer waitRw = rwBuffer;
        final VulkanBuffer waitSurface = surfaceHeightOut;
        final VulkanBuffer waitFlat = flatCacheOut;
        final VulkanBuffer waitSurfaceReadback = surfaceHeightReadback;
        final VulkanBuffer waitFlatReadback = flatCacheReadback;

        GlobalExecutors.prioritizedScheduler.executor(16).execute(() -> {
            try {
                VkUtil.check(VK10.vkWaitForFences(device.getDevice(), waitFence, true, Long.MAX_VALUE));

                int[] surfaceHeight = new int[CACHE_WIDTH * CACHE_WIDTH];
                waitSurfaceReadback.invalidate(0L, (long) surfaceHeight.length * Integer.BYTES);
                readInts(waitSurfaceReadback, surfaceHeight);

                double[] flatCache = new double[flatCachePrefills * CACHE_WIDTH * CACHE_WIDTH];
                if (flatCachePrefills != 0) {
                    waitFlatReadback.invalidate(0L, (long) flatCache.length * Double.BYTES);
                    readDoubles(waitFlatReadback, flatCache);
                }

                future.complete(new RawCacheEntry(surfaceHeight, flatCache));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            } finally {
                releaseAll(device, waitCommandPool, rwData, waitRw, waitSurface,
                        waitSurfaceReadback, waitFlatReadback,
                        waitFlat, waitCommandBuffer, waitFence);
                submission.close();
            }
        });

        future.exceptionally(throwable -> {
            LOGGER.error("Stage1Cache threw exception", throwable);
            return null;
        });

        return future
                .thenApply(Function.identity())
                .orTimeout(120, TimeUnit.SECONDS)
                .exceptionallyCompose(throwable -> throwable instanceof TimeoutException
                        ? CompletableFuture.failedFuture(new TimeoutException(String.format(
                                "Stage1Cache timed out for batch [%d, %d] on %s",
                                cacheIndex.x, cacheIndex.z, device)))
                        : CompletableFuture.failedFuture(throwable));
    }

    private static VkCommandBuffer beginCommandBuffer(VulkanDevice device, VulkanCommandPool pool,
                                                      MemoryStack stack) {
        VkCommandBuffer commandBuffer = pool.acquire();
        VkUtil.check(VK10.vkBeginCommandBuffer(commandBuffer, VkCommandBufferBeginInfo.calloc(stack)
                .sType$Default()
                .flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)));
        return commandBuffer;
    }

    private static void pushConstants(VkCommandBuffer commandBuffer, long layout, MemoryStack stack,
                                      long constData, long rwData, long out, int arg0, int arg1, int arg2) {
        ByteBuffer data = stack.calloc(VkPushConstants.SIZE);
        data.putLong(VkPushConstants.OFFSET_CONST_DATA, constData);
        data.putLong(VkPushConstants.OFFSET_RW_DATA, rwData);
        data.putLong(VkPushConstants.OFFSET_OUT_DATA, out);
        data.putInt(VkPushConstants.OFFSET_ARG0, arg0);
        data.putInt(VkPushConstants.OFFSET_ARG1, arg1);
        data.putInt(VkPushConstants.OFFSET_ARG2, arg2);
        VK10.vkCmdPushConstants(commandBuffer, layout, VK10.VK_SHADER_STAGE_COMPUTE_BIT, 0, data);
    }

    private static void memoryBarrier(VkCommandBuffer commandBuffer, MemoryStack stack) {
        VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc(1, stack)
                .sType$Default()
                .srcAccessMask(VK10.VK_ACCESS_SHADER_WRITE_BIT)
                .dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT);
        VK10.vkCmdPipelineBarrier(commandBuffer,
                VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                0, barrier, null, null);
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

    private static void readInts(VulkanBuffer buffer, int[] destination) {
        long mapped = buffer.mappedData();
        if (mapped == 0L) throw new IllegalStateException("Output buffer is not host visible");
        MemoryUtil.memIntBuffer(mapped, destination.length).get(destination);
    }

    private static void readDoubles(VulkanBuffer buffer, double[] destination) {
        long mapped = buffer.mappedData();
        if (mapped == 0L) throw new IllegalStateException("Output buffer is not host visible");
        MemoryUtil.memDoubleBuffer(mapped, destination.length).get(destination);
    }

    private static void releaseAll(VulkanDevice device, VulkanCommandPool commandPool, ByteBuffer rwData,
                                   VulkanBuffer rwBuffer, VulkanBuffer surfaceHeightOut,
                                   VulkanBuffer surfaceHeightReadback,
                                   VulkanBuffer flatCacheReadback,
                                   VulkanBuffer flatCacheOut, VkCommandBuffer commandBuffer, long fence) {
        try {
            if (rwBuffer != null) rwBuffer.close();
            if (surfaceHeightOut != null) surfaceHeightOut.close();
            if (flatCacheOut != null) flatCacheOut.close();
            if (surfaceHeightReadback != null) surfaceHeightReadback.close();
            if (flatCacheReadback != null) flatCacheReadback.close();
            if (commandBuffer != null && commandPool != null) commandPool.release(commandBuffer);
            device.releaseFence(fence);
        } catch (Throwable t) {
            LOGGER.error("Failed to release Stage1Cache resources", t);
        } finally {
            if (rwData != null) MemoryUtil.memFree(rwData);
        }
    }

    public CompletableFuture<AreaCacheEntry> getChunkCache(int chunkX, int chunkZ) {
        return this.getAreaCache0(chunkX, chunkZ, 1, 1)
                .thenApply(entry -> new AreaCacheEntry(chunkX, chunkZ, 1, 1, entry.surfaceHeights(), entry.flatCaches()));
    }

    public CompletableFuture<AreaCacheEntry> getAreaCache(int startChunkX, int startChunkZ, int sizeX, int sizeZ) {
        return this.getAreaCache0(startChunkX, startChunkZ, sizeX, sizeZ)
                .thenApply(entry -> new AreaCacheEntry(startChunkX, startChunkZ, sizeX, sizeZ,
                        entry.surfaceHeights(), entry.flatCaches()));
    }

    private CompletableFuture<RawCacheEntry> getAreaCache0(int startChunkX, int startChunkZ, int sizeX, int sizeZ) {
        int startCacheX = (startChunkX - 4) >> CACHE_CHUNK_WIDTH_SHIFT;
        int startCacheZ = (startChunkZ - 4) >> CACHE_CHUNK_WIDTH_SHIFT;
        int endCacheX = (startChunkX + sizeX - 1 + 4) >> CACHE_CHUNK_WIDTH_SHIFT;
        int endCacheZ = (startChunkZ + sizeZ - 1 + 4) >> CACHE_CHUNK_WIDTH_SHIFT;

        Long2ReferenceArrayMap<CompletableFuture<RawCacheEntry>> futures =
                new Long2ReferenceArrayMap<>((endCacheX - startCacheX + 1) * (endCacheZ - startCacheZ + 1));
        for (int cacheX = startCacheX; cacheX <= endCacheX; cacheX++) {
            for (int cacheZ = startCacheZ; cacheZ <= endCacheZ; cacheZ++) {
                CacheIndex cacheIndex = new CacheIndex(cacheX, cacheZ);
                futures.put(cacheIndex.toLong(), this.cache.get(cacheIndex));
            }
        }
        return CompletableFuture.allOf(futures.values().toArray(CompletableFuture[]::new))
                .thenApply(_ -> {
                    int surfaceHeightSizeX = 36 + 4 * (sizeX - 1);
                    int surfaceHeightSizeZ = 36 + 4 * (sizeZ - 1);
                    int[] surfaceHeight = new int[surfaceHeightSizeX * surfaceHeightSizeZ];
                    for (int relX = 0; relX < surfaceHeightSizeX; relX++) {
                        for (int relZ = 0; relZ < surfaceHeightSizeZ; relZ++) {
                            int cacheX = (((startChunkX - 4) << 2) + relX) >> CACHE_WIDTH_SHIFT;
                            int cacheZ = (((startChunkZ - 4) << 2) + relZ) >> CACHE_WIDTH_SHIFT;
                            int cacheRelX = (((startChunkX - 4) << 2) + relX) & CACHE_WIDTH_MASK;
                            int cacheRelZ = (((startChunkZ - 4) << 2) + relZ) & CACHE_WIDTH_MASK;
                            int[] cacheData = futures.get(CacheIndex.toLong(cacheX, cacheZ)).join().surfaceHeights();
                            surfaceHeight[relX * surfaceHeightSizeZ + relZ] =
                                    cacheData[(cacheRelX << CACHE_WIDTH_SHIFT) + cacheRelZ];
                        }
                    }
                    int flatCachePrefills = this.worldContext.getGeneratedSpirVSource().getFlatCachePrefills();
                    int flatCacheSizeX = 5 + 4 * (sizeX - 1);
                    int flatCacheSizeZ = 5 + 4 * (sizeZ - 1);
                    int cacheIndexScale = flatCacheSizeX * flatCacheSizeZ;
                    double[] flatCache = new double[flatCachePrefills * flatCacheSizeX * flatCacheSizeZ];
                    for (int cacheIndex = 0; cacheIndex < flatCachePrefills; cacheIndex++) {
                        for (int relX = 0; relX < flatCacheSizeX; relX++) {
                            for (int relZ = 0; relZ < flatCacheSizeZ; relZ++) {
                                int cacheX = (((startChunkX) << 2) + relX) >> CACHE_WIDTH_SHIFT;
                                int cacheZ = (((startChunkZ) << 2) + relZ) >> CACHE_WIDTH_SHIFT;
                                int cacheRelX = (((startChunkX) << 2) + relX) & CACHE_WIDTH_MASK;
                                int cacheRelZ = (((startChunkZ) << 2) + relZ) & CACHE_WIDTH_MASK;
                                double[] cacheData = futures.get(CacheIndex.toLong(cacheX, cacheZ)).join().flatCaches();
                                flatCache[cacheIndex * cacheIndexScale + relX * flatCacheSizeZ + relZ] =
                                        cacheData[(cacheIndex << (CACHE_WIDTH_SHIFT << 1))
                                                + (cacheRelX << CACHE_WIDTH_SHIFT) + cacheRelZ];
                            }
                        }
                    }
                    return new RawCacheEntry(surfaceHeight, flatCache);
                });
    }

    private record CacheIndex(int x, int z) {

        public long toLong() {
            return toLong(this.x, this.z);
        }

        public static long toLong(int x, int z) {
            return ((long) x << 32) | (z & 0xFFFFFFFFL);
        }

        public CacheIndex(long value) {
            this((int) (value >> 32), (int) value);
        }
    }

    public record RawCacheEntry(int[] surfaceHeights, double[] flatCaches) {
    }

    public record AreaCacheEntry(int chunkX, int chunkZ, int sizeX, int sizeZ, int[] surfaceHeights,
                                 double[] flatCaches) {
    }
}
