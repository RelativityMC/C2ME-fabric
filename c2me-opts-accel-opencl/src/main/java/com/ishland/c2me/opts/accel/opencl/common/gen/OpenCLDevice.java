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

import com.ishland.c2me.base.common.scheduler.SingleThreadExecutor;
import com.ishland.c2me.opts.accel.opencl.common.Config;
import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.gen.cache.CLBufferCache;
import com.ishland.c2me.opts.accel.opencl.common.progress.GlobalProgressStash;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.Workarounds;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.intel.IntelWorkarounds;
import com.ishland.c2me.opts.accel.opencl.common.compiler.GeneratedCLSource;
import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.flowsched.util.Assertions;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.INTELDeviceAttributeQuery;
import org.lwjgl.opencl.KHRPriorityHints;
import org.lwjgl.opencl.KHRThrottleHints;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class OpenCLDevice implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCLDevice.class);

    private static final AtomicInteger DEVICE_COUNTER = new AtomicInteger(0);

    private final CLServerGlobalContext globalContext;
    private final OpenCLDeviceMetadata metadata;
    private final Set<Workarounds.Reference> workarounds;
    private CLContextCallback callback;
    private long context;
    private long commandQueue;
    private final AtomicInteger permits = new AtomicInteger(Config.maxConcurrentTasksPerDevice);
    private final Object contextMutex = new Object();
    private final AtomicBoolean open = new AtomicBoolean(true);
//    private final CompletableFuture<Void> closeFuture = new CompletableFuture<>(); // TODO probably only available on CL3.0+
    private final String deviceDescription;
    private final AtomicReference<CompletableFuture<Void>> pendingActions = new AtomicReference<>(CompletableFuture.completedFuture(null));
    private final SingleThreadExecutor executor;
    private final CLBufferCache bufferCache;
    private final CLEventCallbackManager eventCallbackManager;

    public OpenCLDevice(CLServerGlobalContext globalContext, OpenCLDeviceMetadata metadata) {
        this.globalContext = Objects.requireNonNull(globalContext);
        this.metadata = metadata;
        this.workarounds = Workarounds.getWorkarounds(metadata);
        this.deviceDescription = String.format("OpenCL Device %s (%s)", CLUtil.getDeviceInfoStringUTF8(metadata.devicePtr, CL12.CL_DEVICE_NAME), metadata.deviceUUID);
        if (!this.workarounds.isEmpty()) {
            LOGGER.warn("One or more workarounds have been applied to prevent crashes or other issues on {}:", this.deviceDescription);
            LOGGER.warn("[{}]", this.workarounds.stream().map(Enum::name).collect(Collectors.joining(", ")));
            LOGGER.warn("This is not necessarily an issue, but it may disable certain features or optimizations.");
        }
        LOGGER.info("Initializing {}", this.deviceDescription);
        this.executor = new SingleThreadExecutor();
        this.executor.setName("c2me-cldev-%d-%s".formatted(DEVICE_COUNTER.getAndIncrement(), this.deviceDescription.replaceAll("[^a-zA-Z0-9]", "_")));
        this.executor.start();
        this.bufferCache = new CLBufferCache(this.executor);
        this.eventCallbackManager = new CLEventCallbackManager();
        CompletableFuture.runAsync(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer errorCodeRet = stack.callocInt(1);
                PointerBuffer ctxProps = stack.callocPointer(3);
                ctxProps
                        .put(CL12.CL_CONTEXT_PLATFORM)
                        .put(metadata.platformPtr);
                ctxProps.rewind();
                this.callback = CLContextCallback.create((errinfo, private_info, cb, user_data) -> {
                    LOGGER.error("OpenCL Error on {}: {}", this.deviceDescription, MemoryUtil.memUTF8(errinfo));
                });
                try {
                    this.context = CL12.clCreateContext(
                            ctxProps,
                            metadata.devicePtr,
                            this.callback,
                            0,
                            errorCodeRet
                    );
                    CLUtil.checkCLError(errorCodeRet);
                    if (this.workarounds.contains(Workarounds.Reference.INTEL_LINUX_CLEANUP_HANG)) {
                        CL12.clCreateUserEvent(this.context, errorCodeRet);
                        CLUtil.checkCLError(errorCodeRet);
                    }
                    try {
                        PointerBuffer property = stack.callocPointer(1);
                        CLUtil.checkCLError(CL12.clGetDeviceInfo(metadata.devicePtr, CL12.CL_DEVICE_QUEUE_PROPERTIES, property, null));
                        boolean supportsOutOfOrder = (property.get() & CL12.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE) != 0;
                        if (!supportsOutOfOrder) {
                            LOGGER.warn("Device {} does not support out-of-order execution, performance may be significantly impacted", this.deviceDescription);
                        }

                        if (!this.metadata.supportsNonUniformWorkgroups) {
                            LOGGER.warn("Device {} does not support non-uniform workgroups, performance may be slightly impacted", this.deviceDescription);
                        }

                        boolean useQueueProperties = false;
                        LongBuffer queueProp = stack.callocLong(7);
                        queueProp
                                .put(CL20.CL_QUEUE_PROPERTIES)
                                .put(supportsOutOfOrder ? CL12.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE : 0);
                        if (Config.lowPriorityQueues) {
                            if ((metadata.deviceCaps.OpenCL20 || metadata.deviceCaps.OpenCL30) && metadata.deviceCaps.cl_khr_priority_hints) {
                                LOGGER.info("Using cl_khr_priority_hints for {}", this.deviceDescription);
                                useQueueProperties = true;
                                queueProp
                                        .put(KHRPriorityHints.CL_QUEUE_PRIORITY_KHR)
                                        .put(KHRPriorityHints.CL_QUEUE_PRIORITY_LOW_KHR);
                            } else {
                                LOGGER.warn("cl_khr_priority_hints is not supported on {}", this.deviceDescription);
                            }
                            if ((metadata.deviceCaps.OpenCL20 || metadata.deviceCaps.OpenCL30) && metadata.deviceCaps.cl_khr_throttle_hints) {
                                LOGGER.info("Using cl_khr_throttle_hints for {}", this.deviceDescription);
                                useQueueProperties = true;
                                queueProp
                                        .put(KHRThrottleHints.CL_QUEUE_THROTTLE_KHR)
                                        .put(KHRThrottleHints.CL_QUEUE_THROTTLE_LOW_KHR);
                            } else {
                                LOGGER.warn("cl_khr_throttle_hints is not supported on {}", this.deviceDescription);
                            }
                        }

                        try {
                            if (useQueueProperties) {
                                commandQueue = CL20.clCreateCommandQueueWithProperties(this.context, metadata.devicePtr, queueProp.rewind(), errorCodeRet);
                                CLUtil.checkCLError(errorCodeRet);
                            } else {
                                commandQueue = CL12.clCreateCommandQueue(this.context, metadata.devicePtr, supportsOutOfOrder ? CL12.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE : 0, errorCodeRet);
                                CLUtil.checkCLError(errorCodeRet);
                            }
                        } catch (Throwable t) {
                            try {
                                if (this.commandQueue != 0L) {
                                    try {
                                        CLUtil.checkCLError(CL12.clReleaseCommandQueue(this.commandQueue));
                                        this.commandQueue = 0L;
                                    } catch (Throwable t1) {
                                        t.addSuppressed(t1);
                                        // not rethrowing here
                                    }
                                }
                            } catch (Throwable t1) {
                                t.addSuppressed(t1);
                            }
                            throw t;
                        }
                    } catch (Throwable t) {
                        if (this.context != 0L) {
                            try {
                                CLUtil.checkCLError(CL12.clReleaseContext(this.context));
                            } catch (Throwable t1) {
                                t.addSuppressed(t1);
                            }
                        }
                        throw t;
                    }
                } catch (Throwable t) {
                    this.callback.free();
                    throw t;
                }
            }
        }, this.executor).join();
        Assertions.assertTrue(this.context != 0L, "Failed to create OpenCL context for " + this.deviceDescription);
        Assertions.assertTrue(this.callback != null, "Failed to create OpenCL context callback for " + this.deviceDescription);
    }

    public CompletableFuture<EnumMap<OpenCLCGen.ProgramType, Long>> compileProgramAsync(String desc, GeneratedCLSource source) {
        assertOpen();
        CompletableFuture<EnumMap<OpenCLCGen.ProgramType, Long>> future = CompletableFuture.supplyAsync(() -> {
            EnumMap<OpenCLCGen.ProgramType, Long> map = new EnumMap<>(OpenCLCGen.ProgramType.class);
            try {
                String sha256;
                try {
                    sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source.getGeneratedSource().getBytes(StandardCharsets.UTF_8)));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                LOGGER.info("Shader sha256sum: {}", sha256);

                GlobalProgressStash.PROGRESS_TEXT = String.format("Loading shader delivery cache for %s on %s", desc, this.deviceDescription);

                String osKey = PlatformDependent.isWindows() ? "windows" : "linux";

                if (this.metadata.deviceCaps.cl_intel_device_attribute_query) {
                    int ipVersion = CLUtil.getDeviceInfoInt(this.metadata.devicePtr, INTELDeviceAttributeQuery.CL_DEVICE_IP_VERSION_INTEL);

                    if (ipVersion == IntelWorkarounds.DUMMY_GEN9_IP_VERSION) {
                        this.globalContext.shaderCacheManager.tryCacheDirs(this.context, this.metadata, map, String.format("%s/%s/%s/Gen9", sha256, "intel", osKey));
                    } else {
                        this.globalContext.shaderCacheManager.tryCacheDirs(this.context, this.metadata, map, String.format("%s/%s/%s/%d", sha256, "intel", osKey, ipVersion));
                    }
                } else if (IntelWorkarounds.isUsingGen9OnWindows(this.metadata)) {
                    this.globalContext.shaderCacheManager.tryCacheDirs(this.context, this.metadata, map, String.format("%s/%s/%s/Gen9", sha256, "intel", osKey));
                }

                if (!map.isEmpty()) {
                    LOGGER.info("Loaded {} cache entries", map.size());
                }

                OpenCLCGen.ProgramType[] values = OpenCLCGen.ProgramType.values();
                for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
                    OpenCLCGen.ProgramType value = values[i];
                    if (map.containsKey(value)) {
                        continue;
                    }
                    LOGGER.info("Compiling program {} for {} for device {} ({}/{})", value, desc, this, i + 1, valuesLength);
                    GlobalProgressStash.PROGRESS_TEXT = String.format("Compiling %s for %s on %s (%d/%d)", value, desc, this.deviceDescription, i + 1, valuesLength);
                    map.put(value, this.compileProgram0(desc, source.getGeneratedSource(), source.getDumpedPath().toString(), source.getDefines(), value));
                }
                GlobalProgressStash.PROGRESS_TEXT = null;
                try {
                    CLUtil.checkCLError(CL12.clUnloadPlatformCompiler(this.metadata.platformPtr));
                } catch (Throwable t) {
                    LOGGER.warn("Failed to unload OpenCL platform compiler for {}", this.deviceDescription, t);
                }
            } catch (Throwable t) {
                for (Long value : map.values()) {
                    try {
                        CLUtil.checkCLError(CL12.clReleaseProgram(value));
                    } catch (Throwable t1) {
                        t.addSuppressed(t1);
                    }
                }
                GlobalProgressStash.PROGRESS_TEXT = String.format("Compilation failed for %s on %s, check logs for details", desc, this.deviceDescription);
                throw t;
            }
            return map;
        }, CLExecutors.COMPILE_POOL);
        this.combinePendingAction(future);
        return future;
    }

    private void workaroundGen9Caches(String sha256, EnumMap<OpenCLCGen.ProgramType, Long> map) {
        LOGGER.info("Attempting to load all Gen9 variants of shaders");
        int[] gen9IPs = IntelWorkarounds.gen9IPs.toIntArray();
        Arrays.sort(gen9IPs);
        ArrayUtils.reverse(gen9IPs);
        String[] paths = Arrays.stream(gen9IPs)
                .mapToObj(ipVersion1 -> String.format("%s/%s/%d", sha256, "intel", ipVersion1))
                .toArray(String[]::new);
        this.globalContext.shaderCacheManager.tryCacheDirs(this.context, this.metadata, map, paths);
    }

    private long compileProgram0(String desc, String source, String absoluteDumpPath, Object2ReferenceOpenHashMap<String, String> defines, OpenCLCGen.ProgramType type) {
        synchronized (this.contextMutex) {
            assertOpen();
            CLUtil.checkCLError(CL12.clRetainContext(this.context));
        }
        ByteBuffer sourceBuffer = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer errorCodeRet = stack.callocInt(1);
            PointerBuffer sources = stack.callocPointer(1);
            sourceBuffer = MemoryUtil.memUTF8(source, true);
            sources.put(0, sourceBuffer);
            long program = CL12.clCreateProgramWithSource(this.context, sources, null, errorCodeRet);
            CLUtil.checkCLError(errorCodeRet);
            try {
                StringBuilder option = new StringBuilder();
                if (this.metadata.supportsNonUniformWorkgroups) {
                    option.append("-cl-std=CL3.0 ");
                } else {
                    option.append("-cl-std=CL1.2 ");
                }
                option.append("-cl-fp32-correctly-rounded-divide-sqrt -cl-kernel-arg-info ");
                if (this.metadata.deviceCaps.OpenCL21 && !this.workarounds.contains(Workarounds.Reference.NVIDIA_INCOMPLETE_CL30_IMPLEMENTATION)) {
                    option.append("-cl-no-subgroup-ifp ");
                }
                if (!Config.preferFastCompilation) {
                    option.append("-D FUNC_NOINLINE= ");
                }
                if (this.workarounds.contains(Workarounds.Reference.BUILTIN_TRAP_BROKEN)) {
                    option.append("-D AVOID_TRAP=1 ");
                }

                if (Config.enableIntelFastCompilation && this.metadata.isIntelNeoRuntime) {
                    if (!this.workarounds.contains(Workarounds.Reference.INTEL_IGC_OPTS_UNAVAILABLE)) {
//                        option.append("-igc_opts 'KernelTotalSizeThreshold=1048576,UnitSizeThreshold=1536,ExpandedUnitSizeThreshold=1048576,PartitionUnit=1,StaticProfileGuidedPartitioning=0,ControlUnitSize=1,StaticProfileGuidedTrimming=0,LoopCountAwareTrimming=0,EnableLeafCollapsing=1,EnableSizeContributionOptimization=1,ControlInlineTinySize=1,PrintControlKernelTotalSize=15,PrintControlUnitSize=15,PrintFunctionSizeAnalysis=15,PrintStaticProfileGuidedKernelSizeReduction=15,PrintPartitionUnit=15' ");
                        option.append("-igc_opts 'KernelTotalSizeThreshold=1048576,UnitSizeThreshold=1536,ExpandedUnitSizeThreshold=1048576,PartitionUnit=1,StaticProfileGuidedPartitioning=0,ControlUnitSize=1,StaticProfileGuidedTrimming=0,LoopCountAwareTrimming=0,EnableLeafCollapsing=1,EnableSizeContributionOptimization=1,ControlInlineTinySize=1' ");
//                        option.append("-D FUNC_NOINLINE_MIDDF= -igc_opts 'OCLInlineThreshold=96,EnableLeafCollapsing=1,EnableSizeContributionOptimization=1,ControlInlineTinySize=1' ");
                    } else {
                        option.append("-D FUNC_NOINLINE_MIDDF= -D BLOAT_APPARENT_FUNCTION_SIZES=1 ");
                    }
                    if (!this.workarounds.contains(Workarounds.Reference.INTEL_STATIC_PROFILE_GUIDED_TRIMMING_UNAVAILABLE)) {
                        option.append("-cl-intel-static-profile-guided-trimming ");
                    }
                }

                if (!this.workarounds.contains(Workarounds.Reference.NVIDIA_FAST_COMPILE_UNAVAILABLE) && Config.enableNvidiaFastCompilation && this.metadata.deviceCaps.cl_nv_compiler_options) {
                    option.append("-cl-nv-opt-level=1 -cl-nv-verbose -nv-use-200772613 ");
                }

//                option.append("-cl-opt-disable -g -s \"").append(absoluteDumpPath).append("\" ");

                option.append("-D DF_COMPILE_").append(type.name()).append("=1 ");
                if (defines != null) {
                    for (var entry : defines.object2ReferenceEntrySet()) {
                        option.append(" -D ").append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
                    }
                }

                LOGGER.info("Invoking compiler for device {} with options: {}", this, option.toString());

                int buildError = CL12.clBuildProgram(program, this.metadata.devicePtr, option.toString(), null, 0);
                IntBuffer buildStatus = stack.callocInt(1);
                CLUtil.checkCLError(CL12.clGetProgramBuildInfo(program, this.metadata.devicePtr, CL12.CL_PROGRAM_BUILD_STATUS, buildStatus, null));
                String buildLog = null;
                {
                    PointerBuffer sizeRet = stack.callocPointer(1);
                    CLUtil.checkCLError(CL12.clGetProgramBuildInfo(program, this.metadata.devicePtr, CL12.CL_PROGRAM_BUILD_LOG, (ByteBuffer) null, sizeRet));
                    if (sizeRet.get(0) != 0) {
                        ByteBuffer log = MemoryUtil.memAlloc((int) sizeRet.get(0));
                        try {
                            CLUtil.checkCLError(CL12.clGetProgramBuildInfo(program, this.metadata.devicePtr, CL12.CL_PROGRAM_BUILD_LOG, log, null));
                            if (log.get(log.remaining() - 1) == 0) {
                                log.limit(log.remaining() - 1);
                            }
                            buildLog = MemoryUtil.memUTF8(log);
                        } finally {
                            MemoryUtil.memFree(log);
                        }
                    }
                }
                if (buildStatus.get(0) != CL12.CL_BUILD_SUCCESS) {
                    LOGGER.error("Failed to build OpenCL program for {} on {}: \n{}", desc, this.deviceDescription, buildLog);
                    throw new RuntimeException("Failed to build OpenCL program: \n" + buildLog);
                } else {
                    if (buildLog != null && !buildLog.isBlank()) {
                        LOGGER.info("Build log for {} on {}: \n{}", desc, this.deviceDescription, buildLog);
                    }
                    CLUtil.checkCLError(buildError);
                    return program;
                }
            } catch (Throwable t) {
                try {
                    CLUtil.checkCLError(CL12.clReleaseProgram(program));
                } catch (Throwable t1) {
                    t.addSuppressed(t1);
                }
                throw t;
            }
        } finally {
            if (sourceBuffer != null) {
                MemoryUtil.memFree(sourceBuffer);
            }
            synchronized (this.contextMutex) {
                CLUtil.checkCLError(CL12.clReleaseContext(this.context));
            }
        }
    }

    private void combinePendingAction(CompletionStage<?> stage) {
        this.pendingActions.updateAndGet(future -> future.thenCombine(stage, (_, _) -> (Void) null).exceptionally(_ -> null));
    }

    private void assertOpen() {
        if (!this.open.get()) throw new IllegalStateException("OpenCL device " + this.deviceDescription + " is closed");
    }

    public BorrowedCommandQueue borrowCommandQueue() {
        synchronized (this.contextMutex) {
            if (this.permits.get() > 0) {
                this.permits.getAndDecrement();
                CLUtil.checkCLError(CL12.clRetainCommandQueue(this.commandQueue));
                return new BorrowedCommandQueue(this.commandQueue);
            }
            return null;
        }
    }

    private void returnCommandQueue(long queue) {
        Assertions.assertTrue(queue == this.commandQueue);
        synchronized (this.contextMutex) {
            this.permits.getAndIncrement();
            CLUtil.checkCLError(CL12.clReleaseCommandQueue(this.commandQueue));
        }
        this.globalContext.signalNotEmpty();
    }

    public int getPermits() {
        return this.permits.get();
    }

    public long getContext() {
        return this.context;
    }

    public OpenCLDeviceMetadata getMetadata() {
        return this.metadata;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    public CLBufferCache getBufferCache() {
        return this.bufferCache;
    }

    public CLEventCallbackManager getEventCallbackManager() {
        return this.eventCallbackManager;
    }

    public Set<Workarounds.Reference> getWorkarounds() {
        return this.workarounds;
    }

    @Override
    public String toString() {
        return this.deviceDescription;
    }

    @Override
    public void close() {
        synchronized (this.contextMutex) {
            if (this.open.compareAndSet(true, false)) {
                LOGGER.info("Closing {}", this.deviceDescription);
                this.bufferCache.close();
                try {
                    CLUtil.checkCLError(CL12.clReleaseCommandQueue(this.commandQueue));
                } catch (Throwable t) {
                    LOGGER.error("Failed to release OpenCL command queue for {}", this.deviceDescription, t);
                }
                try {
                    int[] refcnt = new int[1];
                    CLUtil.checkCLError(CL12.clGetContextInfo(this.context, CL12.CL_CONTEXT_REFERENCE_COUNT, refcnt, null));
                    if (refcnt[0] > 1) {
                        LOGGER.warn("OpenCL context for {} is still referenced by {} objects", this.deviceDescription, refcnt[0] - 1);
                    }
                } catch (Throwable t) {
                    LOGGER.error("Failed to query refcnt for {}", this.deviceDescription, t);
                }
                try {
                    CLUtil.checkCLError(CL12.clReleaseContext(this.context));
                } catch (Throwable t) {
                    LOGGER.error("Failed to release OpenCL context for {}", this.deviceDescription, t);
                }
                this.executor.shutdown();
                this.eventCallbackManager.close();
                // TODO properly release callback
            } else {
                LOGGER.warn("Attempted to close OpenCL context twice for {}", this.deviceDescription);
            }
        }
    }

    public class BorrowedCommandQueue implements Closeable {
        private final long commandQueue;
        private final AtomicBoolean open = new AtomicBoolean(true);

        private BorrowedCommandQueue(long commandQueue) {
            this.commandQueue = commandQueue;
        }

        public long getCommandQueue() {
            return this.commandQueue;
        }

        public long getContext() {
            return OpenCLDevice.this.context;
        }

        public OpenCLDevice getDevice() {
            return OpenCLDevice.this;
        }

        @Override
        public void close() {
            if (this.open.compareAndSet(true, false)) {
                OpenCLDevice.this.returnCommandQueue(this.commandQueue);
            } else {
                LOGGER.warn("Attempted to close OpenCL command queue twice for {}", OpenCLDevice.this.deviceDescription);
            }
        }
    }

}
