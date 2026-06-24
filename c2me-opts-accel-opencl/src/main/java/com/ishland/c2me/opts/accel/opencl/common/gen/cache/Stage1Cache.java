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

package com.ishland.c2me.opts.accel.opencl.common.gen.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.opts.accel.opencl.common.Config;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLDataUtil;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLServerWorldContext;
import com.ishland.c2me.opts.accel.opencl.common.gen.OpenCLDevice;
import com.ishland.c2me.opts.accel.opencl.common.util.CLEventList;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.Workarounds;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLEventCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Stage1Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stage1Cache.class);

    private static final int CACHE_CHUNK_WIDTH = Config.useSmallerBatches ? 8 : 16;
    private static final int CACHE_CHUNK_WIDTH_MASK = CACHE_CHUNK_WIDTH - 1;
    private static final int CACHE_CHUNK_WIDTH_SHIFT = Integer.numberOfTrailingZeros(CACHE_CHUNK_WIDTH);

    private static final int CACHE_WIDTH = CACHE_CHUNK_WIDTH << 2;
    private static final int CACHE_WIDTH_MASK = CACHE_WIDTH - 1;
    private static final int CACHE_WIDTH_SHIFT = Integer.numberOfTrailingZeros(CACHE_WIDTH);

//    private final AsyncSemaphore cacheLoadingSemaphore = new FairAsyncSemaphore(4);
    private final AsyncLoadingCache<CacheIndex, RawCacheEntry> cache; // [(relX << CACHE_WIDTH_SHIFT) + relZ]
    private final CLServerWorldContext worldContext;

    public Stage1Cache(CLServerWorldContext worldContext) {
        this.worldContext = Objects.requireNonNull(worldContext, "worldContext must not be null");
        this.cache = Caffeine.newBuilder()
                .maximumSize(256)
                .executor(GlobalExecutors.asyncScheduler)
                .buildAsync(this::asyncLoad0);
    }

    // cacheIndex is the chunk position >> CACHE_CHUNK_WIDTH_SHIFT
    private CompletableFuture<RawCacheEntry> asyncLoad0(CacheIndex cacheIndex, Executor executor) {
        return this.worldContext.borrowCommandQueue().thenCompose(pair -> {
            return CompletableFuture.supplyAsync(() -> this.execute0(cacheIndex, pair), pair.left().getDevice().getExecutor()).thenCompose(Function.identity());
        });
    }

    private @NotNull CompletableFuture<RawCacheEntry> execute0(CacheIndex cacheIndex, Pair<OpenCLDevice.BorrowedCommandQueue, CLServerWorldContext.DeviceWithProgram> pair) {
        CompletableFuture<RawCacheEntry> future = new CompletableFuture<>();

        OpenCLDevice.BorrowedCommandQueue commandQueue = pair.left();
        CLServerWorldContext.DeviceWithProgram deviceWithProgram = pair.right();


        try (final MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer errorCodeRet = stack.callocInt(1);

            CLBufferCache.BufferEntry surfaceHeightOutBuffer = deviceWithProgram.device().getBufferCache().allocate(
                    CLBufferCache.Type.ESTIMATE_SURFACE_HEIGHT_RX,
                    CACHE_WIDTH * CACHE_WIDTH * 4,
                    size -> CL12.clCreateBuffer(commandQueue.getContext(), CL12.CL_MEM_WRITE_ONLY, size, errorCodeRet)
            );
            CLUtil.checkCLError(errorCodeRet);

            CLBufferCache.BufferEntry flatCacheOutBuffer;

            ByteBuffer rwData = CLDataUtil.worldgen_data_root$createForFlatCacheOnly(
                    new ChunkPos(cacheIndex.x << CACHE_CHUNK_WIDTH_SHIFT, cacheIndex.z << CACHE_CHUNK_WIDTH_SHIFT),
                    CACHE_CHUNK_WIDTH,
                    worldContext.getGeneratedCLSource(),
                    null,
                    false
            );
            CLBufferCache.BufferEntry rwBuffer = deviceWithProgram.device().getBufferCache().allocate(
                    CLBufferCache.Type.GEN_STAGE1_RW_DATA,
                    rwData.remaining(),
                    size -> CL12.clCreateBuffer(commandQueue.getContext(), CL12.CL_MEM_READ_WRITE, size, errorCodeRet)
            );
            CLUtil.checkCLError(errorCodeRet);

            PointerBuffer eventRet = stack.callocPointer(1);
            CLEventList eventList = new CLEventList();

            LongList eventsToRelease = new LongArrayList();
            LongList kernelsToRelease = new LongArrayList();

            CLUtil.checkCLError(CL12.clEnqueueWriteBuffer(commandQueue.getCommandQueue(), rwBuffer.buffer(), false, 0, rwData, null, eventRet));
            eventsToRelease.add(eventRet.get(0));
            eventList.add(eventRet.get(0));

            if (this.worldContext.getGeneratedCLSource().getFlatCachePrefills() != 0) {
                flatCacheOutBuffer = deviceWithProgram.device().getBufferCache().allocate(
                        CLBufferCache.Type.FLATCACHE_RX,
                        worldContext.getGeneratedCLSource().getFlatCachePrefills() * (CACHE_WIDTH) * (CACHE_WIDTH) * 8,
                        size -> CL12.clCreateBuffer(commandQueue.getContext(), CL12.CL_MEM_WRITE_ONLY, size, errorCodeRet)
                );
                CLUtil.checkCLError(errorCodeRet);

                try (final MemoryStack _ = MemoryStack.stackPush()) {
                    PointerBuffer workSize = stack.mallocPointer(2);
                    workSize.put(0, CACHE_WIDTH);
                    workSize.put(1, CACHE_WIDTH);
                    workSize.rewind();

                    PointerBuffer local = stack.callocPointer(2);
                    local.put(0, 16);
                    local.put(1, 16);
                    local.rewind();

                    PointerBuffer prevEvent = eventList.getEventWaitList(stack);
                    eventList.clear();

                    for (int i = 0; i < worldContext.getGeneratedCLSource().getFlatCachePrefills(); i ++) {
                        long flatCachePrefillKernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.FLAT_CACHE_PREFILL), "df_flatcache_prefill_kernel_" + i, errorCodeRet);
                        CLUtil.checkCLError(errorCodeRet);
                        CLUtil.checkCLError(CL12.clSetKernelArg1p(flatCachePrefillKernel, 0, deviceWithProgram.programConstDataBuffer()));
                        CLUtil.checkCLError(CL12.clSetKernelArg1p(flatCachePrefillKernel, 1, rwBuffer.buffer()));
                        CLUtil.checkCLError(CL12.clSetKernelArg1p(flatCachePrefillKernel, 2, flatCacheOutBuffer.buffer()));
                        CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), flatCachePrefillKernel, 2, null, workSize, local, prevEvent, eventRet));
                        eventsToRelease.add(eventRet.get(0));
                        eventList.add(eventRet.get(0));
                        kernelsToRelease.add(flatCachePrefillKernel);
                    }
                }
            } else {
                flatCacheOutBuffer = null;
            }

            try (final MemoryStack _ = MemoryStack.stackPush()) {
                PointerBuffer workSize = stack.mallocPointer(3);
                workSize.put(0, CACHE_WIDTH);
                workSize.put(1, CACHE_WIDTH);
                workSize.rewind();

                PointerBuffer local = stack.callocPointer(3);
                local.put(0, 8);
                local.put(1, 8);
                local.rewind();

                PointerBuffer prevEvent = eventList.getEventWaitList(stack);
                eventList.clear();

                long estimateSurfaceHeightKernel = CL12.clCreateKernel(deviceWithProgram.getProgram(OpenCLCGen.ProgramType.ESTIMATE_SURFACE_HEIGHT), "chunkNoiseSampler_estimateSurfaceHeight_prefill_indep", errorCodeRet);
                CLUtil.checkCLError(errorCodeRet);
                CLUtil.checkCLError(CL12.clSetKernelArg1p(estimateSurfaceHeightKernel, 0, deviceWithProgram.programConstDataBuffer()));
                CLUtil.checkCLError(CL12.clSetKernelArg1p(estimateSurfaceHeightKernel, 1, rwBuffer.buffer()));
                CLUtil.checkCLError(CL12.clSetKernelArg1p(estimateSurfaceHeightKernel, 2, surfaceHeightOutBuffer.buffer()));
                CLUtil.checkCLError(CL12.clSetKernelArg1i(estimateSurfaceHeightKernel, 3, cacheIndex.x << CACHE_CHUNK_WIDTH_SHIFT));
                CLUtil.checkCLError(CL12.clSetKernelArg1i(estimateSurfaceHeightKernel, 4, cacheIndex.z << CACHE_CHUNK_WIDTH_SHIFT));
                CLUtil.checkCLError(CL12.clSetKernelArg1i(estimateSurfaceHeightKernel, 5, CACHE_WIDTH));
                CLUtil.checkCLError(CL12.clEnqueueNDRangeKernel(commandQueue.getCommandQueue(), estimateSurfaceHeightKernel, 2, null, workSize, local, prevEvent, eventRet));
                eventsToRelease.add(eventRet.get(0));
                eventList.add(eventRet.get(0));
                kernelsToRelease.add(estimateSurfaceHeightKernel);
            }

            DoubleBuffer flatCacheOutBufferData;
            IntBuffer surfaceHeightBufferData = MemoryUtil.memAllocInt(CACHE_WIDTH * CACHE_WIDTH); // int[relX * cacheWidth + relZ]
            {
                PointerBuffer prevEvent = eventList.getEventWaitList(stack);
                eventList.clear();

                if (this.worldContext.getGeneratedCLSource().getFlatCachePrefills() != 0) {
                    Assertions.assertTrue(flatCacheOutBuffer != null);
                    assert flatCacheOutBuffer != null;
                    flatCacheOutBufferData = MemoryUtil.memAllocDouble(worldContext.getGeneratedCLSource().getFlatCachePrefills() * (CACHE_WIDTH) * (CACHE_WIDTH));
                    try (MemoryStack _ = MemoryStack.stackPush()) {
                        CLUtil.checkCLError(CL12.clEnqueueReadBuffer(commandQueue.getCommandQueue(), flatCacheOutBuffer.buffer(), false, 0, flatCacheOutBufferData, prevEvent, eventRet));
                        eventsToRelease.add(eventRet.get(0));
                        eventList.add(eventRet.get(0));
                    }
                } else {
                    flatCacheOutBufferData = null;
                }

                try (MemoryStack _ = MemoryStack.stackPush()) {
                    CLUtil.checkCLError(CL12.clEnqueueReadBuffer(commandQueue.getCommandQueue(), surfaceHeightOutBuffer.buffer(), false, 0, surfaceHeightBufferData, prevEvent, eventRet));
                    eventsToRelease.add(eventRet.get(0));
                    eventList.add(eventRet.get(0));
                }
            }

            if (Config.doExplicitFlushes || deviceWithProgram.device().getWorkarounds().contains(Workarounds.Reference.REQUIRE_EXPLICIT_FLUSHES)) {
                CLUtil.checkCLError(CL12.clFlush(commandQueue.getCommandQueue()));
            }

            AtomicInteger counter = new AtomicInteger(eventList.size());
            CLEventCallbackI callback = (event1, event_command_exec_status, user_data) -> {
                if (counter.decrementAndGet() != 0) return;
                GlobalExecutors.prioritizedScheduler.executor(16).execute(() -> {
                    try {
                        try {
                            if (event_command_exec_status != CL12.CL_COMPLETE) {
                                LOGGER.error("OpenCL command failed: {}", event_command_exec_status);
                                future.completeExceptionally(new RuntimeException("OpenCL command failed: " + event_command_exec_status));
                            } else {
                                int[] surfaceHeight = new int[CACHE_WIDTH * CACHE_WIDTH];
                                surfaceHeightBufferData.get(0, surfaceHeight);
                                double[] flatCache = new double[worldContext.getGeneratedCLSource().getFlatCachePrefills() * (CACHE_WIDTH) * (CACHE_WIDTH)];
                                if (flatCacheOutBufferData != null) {
                                    flatCacheOutBufferData.get(0, flatCache);
                                }
                                future.complete(new RawCacheEntry(surfaceHeight, flatCache));
                            }
                        } finally {
                            deviceWithProgram.device().getExecutor().execute(() -> {
                                deviceWithProgram.device().getBufferCache().returnBuffer(CLBufferCache.Type.ESTIMATE_SURFACE_HEIGHT_RX, surfaceHeightOutBuffer);
                                deviceWithProgram.device().getBufferCache().returnBuffer(CLBufferCache.Type.GEN_STAGE1_RW_DATA, rwBuffer);
                                if (flatCacheOutBuffer != null) deviceWithProgram.device().getBufferCache().returnBuffer(CLBufferCache.Type.FLATCACHE_RX, flatCacheOutBuffer);
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
                            MemoryUtil.memFree(surfaceHeightBufferData);
                            if (flatCacheOutBufferData != null) MemoryUtil.memFree(flatCacheOutBufferData);
                            MemoryUtil.memFree(rwData);
                            commandQueue.close();
                        }
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
            };
            LongListIterator iterator = eventList.iterator();
            while (iterator.hasNext()) {
                deviceWithProgram.device().getEventCallbackManager().registerCallback(
                        iterator.nextLong(),
                        CL12.CL_COMPLETE,
                        callback
                );
            }
        }

        future.exceptionally(throwable -> {
            LOGGER.error("Stage1Cache threw exception", throwable);
            return null;
        });

        return future
                .thenApply(Function.identity())
                .orTimeout(120, TimeUnit.SECONDS)
                .exceptionallyCompose(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        return CompletableFuture.failedFuture(new TimeoutException(String.format("Stage1Cache timed out for batch [%d, %d] on %s", cacheIndex.x, cacheIndex.z, deviceWithProgram.device().toString())));
                    } else {
                        return CompletableFuture.failedFuture(throwable);
                    }
                });
    }

    public CompletableFuture<AreaCacheEntry> getChunkCache(int chunkX, int chunkZ) {
        return this.getAreaCache0(chunkX, chunkZ, 1, 1)
                .thenApply(entry -> new AreaCacheEntry(chunkX, chunkZ, 1, 1, entry.surfaceHeights(), entry.flatCaches()));
    }

    public CompletableFuture<AreaCacheEntry> getAreaCache(int startChunkX, int startChunkZ, int sizeX, int sizeZ) {
        return this.getAreaCache0(startChunkX, startChunkZ, sizeX, sizeZ)
                .thenApply(entry -> new AreaCacheEntry(startChunkX, startChunkZ, sizeX, sizeZ, entry.surfaceHeights(), entry.flatCaches()));
    }

    private CompletableFuture<RawCacheEntry> getAreaCache0(int startChunkX, int startChunkZ, int sizeX, int sizeZ) {
        // one chunk need 36x36 worth of cache, and it can span across multiple cache entries
        int startCacheX = (startChunkX - 4) >> CACHE_CHUNK_WIDTH_SHIFT;
        int startCacheZ = (startChunkZ - 4) >> CACHE_CHUNK_WIDTH_SHIFT;
        int endCacheX = (startChunkX + sizeX - 1 + 4) >> CACHE_CHUNK_WIDTH_SHIFT;
        int endCacheZ = (startChunkZ + sizeZ - 1 + 4) >> CACHE_CHUNK_WIDTH_SHIFT;

        Long2ReferenceArrayMap<CompletableFuture<RawCacheEntry>> futures = new Long2ReferenceArrayMap<>((endCacheX - startCacheX + 1) * (endCacheZ - startCacheZ + 1));
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
                            surfaceHeight[relX * surfaceHeightSizeZ + relZ] = cacheData[(cacheRelX << CACHE_WIDTH_SHIFT) + cacheRelZ];
                        }
                    }
                    int flatCachePrefills = this.worldContext.getGeneratedCLSource().getFlatCachePrefills();
                    int flatCacheSizeX = 5 + 4 * (sizeX - 1);
                    int flatCacheSizeZ = 5 + 4 * (sizeZ - 1);
                    int cacheIndexScale = flatCacheSizeX * flatCacheSizeZ;
                    double[] flatCache = new double[flatCachePrefills * flatCacheSizeX * flatCacheSizeZ];
                    {
                        for (int cacheIndex = 0; cacheIndex < flatCachePrefills; cacheIndex++) {
                            for (int relX = 0; relX < flatCacheSizeX; relX++) {
                                for (int relZ = 0; relZ < flatCacheSizeZ; relZ++) {
                                    int cacheX = (((startChunkX) << 2) + relX) >> CACHE_WIDTH_SHIFT;
                                    int cacheZ = (((startChunkZ) << 2) + relZ) >> CACHE_WIDTH_SHIFT;
                                    int cacheRelX = (((startChunkX) << 2) + relX) & CACHE_WIDTH_MASK;
                                    int cacheRelZ = (((startChunkZ) << 2) + relZ) & CACHE_WIDTH_MASK;
                                    double[] cacheData = futures.get(CacheIndex.toLong(cacheX, cacheZ)).join().flatCaches();
                                    flatCache[cacheIndex * cacheIndexScale + relX * flatCacheSizeZ + relZ] = cacheData[(cacheIndex << (CACHE_WIDTH_SHIFT << 1)) + (cacheRelX << CACHE_WIDTH_SHIFT) + cacheRelZ];
                                }
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

    public record AreaCacheEntry(int chunkX, int chunkZ, int sizeX, int sizeZ, int[] surfaceHeights, double[] flatCaches) {
    }

}
