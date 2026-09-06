package com.ishland.c2me.opts.accel.vulkan.common.gen;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.GeneratedSpirVSource;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.vulkan.common.gen.cache.Stage1Cache;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class VkServerWorldContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(VkServerWorldContext.class);

    private final ArrayList<DeviceWithPipelines> openDevices = new ArrayList<>();
    private final ReferenceOpenHashSet<Pair<VulkanDevice, CompletableFuture<Void>>> pendingBuilds = new ReferenceOpenHashSet<>();

    private final Stage1Cache stage1Cache;
    private final VkServerGlobalContext globalContext;
    private final String description;
    private final GeneratedSpirVSource generatedSpirVSource;
    private final GenerationShapeConfig generationShapeConfig;
    private final CLBlockStateMappings blockStateMappings;

    public VkServerWorldContext(VkServerGlobalContext globalContext, String description,
                                GeneratedSpirVSource generatedSpirVSource,
                                GenerationShapeConfig generationShapeConfig,
                                CLBlockStateMappings blockStateMappings) {
        this.globalContext = globalContext;
        this.description = description;
        this.generatedSpirVSource = Objects.requireNonNull(generatedSpirVSource);
        this.generationShapeConfig = Objects.requireNonNull(generationShapeConfig);
        this.blockStateMappings = Objects.requireNonNull(blockStateMappings);
        this.stage1Cache = new Stage1Cache(this);
        this.globalContext.registerWorld(this);
    }

    public void addDevice(VulkanDevice device, SubmissionPermits permits) {
        synchronized (this.pendingBuilds) {
            for (Pair<VulkanDevice, CompletableFuture<Void>> pending : this.pendingBuilds) {
                if (pending.left() == device) return;
            }
            LOGGER.info("Building pipelines for {} for device {}", this.description, device);

            CompletableFuture<Void> future = CompletableFuture
                    .supplyAsync(() -> createPipelines(device), GlobalExecutors.prioritizedScheduler.executor(16))
                    .thenAccept(pipelines -> {
                        VulkanBuffer constData = uploadConstData(device);
                        synchronized (this.openDevices) {
                            this.openDevices.add(new DeviceWithPipelines(device, permits, pipelines, constData));
                        }
                        LOGGER.info("Built pipelines for {} for device {}", this.description, device);
                    })
                    .exceptionally(throwable -> {
                        LOGGER.error("Failed to build pipelines for device {}", device, throwable);
                        return null;
                    });

            Pair<VulkanDevice, CompletableFuture<Void>> pair = Pair.of(device, future);
            this.pendingBuilds.add(pair);
            future.handle((_, _) -> {
                try {
                    boolean release = false;
                    synchronized (this.pendingBuilds) {
                        // Absent means removeDevice ran while the build was in flight.
                        if (!this.pendingBuilds.remove(pair)) release = true;
                    }
                    this.globalContext.signalNotEmpty();
                    if (release) this.removeDevice(device);
                } catch (Throwable t) {
                    LOGGER.error("Failed to remove pending pipeline build", t);
                }
                return null;
            });
        }
    }

    // One VkPipelineCache per pipeline, each with its own file: Vulkan cannot evict a single
    // entry, so a device-wide cache would accumulate every world's shaders and every rebuild of
    // them forever. Scoped this way a file holds one entry and is replaced whole when it misses.
    private EnumMap<SpirVGen.ProgramType, VulkanPipeline> createPipelines(VulkanDevice device) {
        EnumMap<SpirVGen.ProgramType, VulkanPipeline> pipelines = new EnumMap<>(SpirVGen.ProgramType.class);
        int compiled = 0;
        try {
            for (SpirVGen.ProgramType type : SpirVGen.ProgramType.values()) {
                long started = System.nanoTime();
                VkPipelineCache cache = VkPipelineCache.open(device.getDevice(), device.getPhysicalDevice(),
                        pipelineCacheFile(device, type));
                try {
                    pipelines.put(type, VulkanPipeline.createComputePipeline(device,
                            this.generatedSpirVSource.getModule(type), new long[0], VkPushConstants.SIZE,
                            this.generatedSpirVSource.getSpecializationConstants(), cache.getHandle()));
                    cache.saveIfChanged();
                    if (!cache.wasSeeded()) compiled++;
                } finally {
                    cache.close();
                }
                LOGGER.debug("Built pipeline {} ({} words) in {} ms", type,
                        this.generatedSpirVSource.getModule(type).length, (System.nanoTime() - started) / 1_000_000L);
            }
        } catch (Throwable t) {
            for (VulkanPipeline pipeline : pipelines.values()) pipeline.close();
            throw t;
        }
        LOGGER.info("Built {} pipelines ({} compiled, {} from cache) for {} on {}",
                pipelines.size(), compiled, pipelines.size() - compiled, this.description, device);
        return pipelines;
    }

    private Path pipelineCacheFile(VulkanDevice device, SpirVGen.ProgramType type) {
        String world = this.description.replaceAll("[^A-Za-z0-9._-]", "_");
        return Path.of(".c2me", "vulkan_pipeline_cache", device.getDeviceUUID().toString(),
                world + "-" + type.name() + ".bin");
    }

    private VulkanBuffer uploadConstData(VulkanDevice device) {
        byte[] constData = this.generatedSpirVSource.getConstData();
        VulkanBuffer buffer = VulkanBuffer.createStorageBuffer(device, Math.max(constData.length, 1));
        ByteBuffer staging = MemoryUtil.memAlloc(constData.length);
        try {
            staging.put(constData).rewind();
            buffer.writeToBuffer(staging, 0);
            buffer.flush(0, constData.length);
        } catch (Throwable t) {
            buffer.close();
            throw t;
        } finally {
            MemoryUtil.memFree(staging);
        }
        return buffer;
    }

    public void removeDevice(VulkanDevice device) {
        synchronized (this.pendingBuilds) {
            for (Pair<VulkanDevice, CompletableFuture<Void>> pending : this.pendingBuilds) {
                if (pending.left() == device) {
                    this.pendingBuilds.remove(pending);
                    return;
                }
            }
        }
        synchronized (this.openDevices) {
            for (DeviceWithPipelines entry : this.openDevices) {
                if (entry.device() == device) {
                    removeDevice0(entry);
                    this.openDevices.remove(entry);
                    return;
                }
            }
        }
    }

    private void removeDevice0(DeviceWithPipelines entry) {
        for (VulkanPipeline pipeline : entry.pipelines().values()) {
            try {
                pipeline.close();
            } catch (Throwable t) {
                LOGGER.error("Failed to release pipeline for device {}", entry.device(), t);
            }
        }
        try {
            entry.constDataBuffer().close();
        } catch (Throwable t) {
            LOGGER.error("Failed to release constant data buffer for device {}", entry.device(), t);
        }
        LOGGER.info("Released pipelines for {} for device {}", this.description, entry.device());
    }

    public Pair<SubmissionPermits.Borrowed, DeviceWithPipelines> tryBorrowSubmission() {
        synchronized (this.openDevices) {
            int size = this.openDevices.size();
            if (size == 0) return null;

            DeviceWithPipelines leastTask = this.openDevices.getFirst();
            for (int i = 1; i < size; i++) {
                DeviceWithPipelines current = this.openDevices.get(i);
                if (current.permits().available() > leastTask.permits().available()) {
                    leastTask = current;
                }
            }

            SubmissionPermits.Borrowed borrowed = leastTask.permits().tryBorrow();
            return borrowed != null ? Pair.of(borrowed, leastTask) : null;
        }
    }

    public CompletableFuture<Pair<SubmissionPermits.Borrowed, DeviceWithPipelines>> borrowSubmission() {
        CompletableFuture<Pair<SubmissionPermits.Borrowed, DeviceWithPipelines>> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                this.globalContext.takeLock.lock();
                try {
                    while (true) {
                        Pair<SubmissionPermits.Borrowed, DeviceWithPipelines> borrowed = tryBorrowSubmission();
                        if (borrowed != null) {
                            future.complete(borrowed);
                            return;
                        }
                        this.globalContext.notEmpty.await();
                    }
                } finally {
                    this.globalContext.takeLock.unlock();
                }
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public void releaseAllDevices() {
        synchronized (this.pendingBuilds) {
            this.pendingBuilds.clear();
        }
        synchronized (this.openDevices) {
            for (DeviceWithPipelines entry : this.openDevices) removeDevice0(entry);
            this.openDevices.clear();
        }
        this.globalContext.unregisterWorld(this);
    }

    public GenerationShapeConfig getGenerationShapeConfig() {
        return this.generationShapeConfig;
    }

    public Stage1Cache getEstimateSurfaceHeightCache() {
        return this.stage1Cache;
    }

    public GeneratedSpirVSource getGeneratedSpirVSource() {
        return this.generatedSpirVSource;
    }

    public CLBlockStateMappings getBlockStateMappings() {
        return this.blockStateMappings;
    }

    public record DeviceWithPipelines(VulkanDevice device,
                                      SubmissionPermits permits,
                                      EnumMap<SpirVGen.ProgramType, VulkanPipeline> pipelines,
                                      VulkanBuffer constDataBuffer) {

        public VulkanPipeline getPipeline(SpirVGen.ProgramType type) {
            VulkanPipeline pipeline = this.pipelines.get(type);
            if (pipeline == null) {
                throw new IllegalStateException("Pipeline for type " + type + " is not available");
            }
            return pipeline;
        }
    }

}
