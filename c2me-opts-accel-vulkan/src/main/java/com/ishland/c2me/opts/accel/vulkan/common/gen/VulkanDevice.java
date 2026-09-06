package com.ishland.c2me.opts.accel.vulkan.common.gen;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class VulkanDevice implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(VulkanDevice.class);

    private static final int MAX_COMPUTE_QUEUES = 8;

    private final VkServerGlobalContext globalContext;

    private final VkDevice device;
    private final VkPhysicalDevice physicalDevice;
    private final String deviceName;
    private final float timestampPeriod;
    private final UUID deviceUUID;
    private final QueueIndices queueIndices;
    private final HeapIndices heapIndices;

    // Queues are externally synchronised only while a submission is in progress, so they are
    // handed out per submission rather than bound to a thread: there are always more worker
    // threads than queues, and a pool balances itself where a fixed mapping does not.
    private final ArrayBlockingQueue<VkQueue> computeQueuePool;
    private final ArrayBlockingQueue<VkQueue> transferQueuePool;

    private final ConcurrentLinkedQueue<Long> fencePool = new ConcurrentLinkedQueue<>();

    private final long allocator;
    private final ThreadLocal<VulkanCommandPool> computeCommandPool;
    private final ThreadLocal<VulkanCommandPool> transferCommandPool;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private Set<VulkanCommandPool> computePoolTracking = ConcurrentHashMap.newKeySet();
    private Set<VulkanCommandPool> transferPoolTracking = ConcurrentHashMap.newKeySet();

    public VulkanDevice(VkServerGlobalContext globalContext, VkPhysicalDevice physicalDevice) {
        this.globalContext = Objects.requireNonNull(globalContext);
        this.physicalDevice = Objects.requireNonNull(physicalDevice);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties2 properties2 = VkPhysicalDeviceProperties2.calloc(stack)
                    .sType$Default();

            VK11.vkGetPhysicalDeviceProperties2(physicalDevice, properties2);
            this.deviceName = properties2.properties().deviceNameString();
            this.timestampPeriod = properties2.properties().limits().timestampPeriod();
        }

        this.deviceUUID = UUID.nameUUIDFromBytes(this.deviceName.getBytes(StandardCharsets.UTF_8));

        this.queueIndices = findQueueFamilies(physicalDevice);
        this.heapIndices = findHeaps(physicalDevice);

        this.device = createLogicalDevice(this.queueIndices, this.physicalDevice);
        this.allocator = createAllocator(globalContext.getInstance(), this.device, physicalDevice);

        this.computeQueuePool = new ArrayBlockingQueue<>(this.queueIndices.computeQueueCount);
        for (int q = 0; q < this.queueIndices.computeQueueCount; q++) {
            this.computeQueuePool.add(getQueue(this.device, this.queueIndices.computeFamily, q));
        }
        this.transferQueuePool = new ArrayBlockingQueue<>(1);
        this.transferQueuePool.add(getQueue(this.device, this.queueIndices.transferFamily,
                this.queueIndices.transferQueueIndex));

        this.computeCommandPool = ThreadLocal.withInitial(() -> {
            VulkanCommandPool pool = new VulkanCommandPool(this, WorkQueue.COMPUTE);
            this.computePoolTracking.add(pool);
            return pool;
        });

        this.transferCommandPool = ThreadLocal.withInitial(() -> {
            VulkanCommandPool pool = new VulkanCommandPool(this, WorkQueue.TRANSFER);
            this.transferPoolTracking.add(pool);
            return pool;
        });

        LOGGER.info("Opened Vulkan device: {}", this.deviceName);
    }

    private static VkDevice createLogicalDevice(QueueIndices indices, VkPhysicalDevice physicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            Map<Integer, Integer> familyQueueCounts = new HashMap<>();
            familyQueueCounts.merge(indices.computeFamily, indices.computeQueueCount, Math::max);
            familyQueueCounts.merge(indices.transferFamily, indices.transferQueueIndex + 1, Math::max);

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(familyQueueCounts.size(), stack);
            int i = 0;
            for (Map.Entry<Integer, Integer> entry : familyQueueCounts.entrySet()) {
                FloatBuffer priorities = stack.callocFloat(entry.getValue());
                for (int q = 0; q < entry.getValue(); q++) priorities.put(q, 1.0f);
                queueCreateInfos.get(i++)
                        .sType$Default()
                        .queueFamilyIndex(entry.getKey())
                        .pQueuePriorities(priorities);
            }

            VkPhysicalDeviceVulkan12Features vulkan12Features = VkPhysicalDeviceVulkan12Features.calloc(stack)
                    .sType$Default()
                    .bufferDeviceAddress(true)
                    .scalarBlockLayout(true)
                    .shaderInt8(true)
                    .storageBuffer8BitAccess(true);

            VkPhysicalDeviceVulkan11Features vulkan11Features = VkPhysicalDeviceVulkan11Features.calloc(stack)
                    .sType$Default()
                    .storageBuffer16BitAccess(true)
                    .pNext(vulkan12Features.address());

            VkPhysicalDeviceFeatures2 deviceFeatures = VkPhysicalDeviceFeatures2.calloc(stack)
                    .sType$Default()
                    .pNext(vulkan11Features.address());
            deviceFeatures.features().shaderInt64(true);
            deviceFeatures.features().shaderFloat64(true);
            deviceFeatures.features().shaderInt16(true);

            PointerBuffer extensions = stack.pointers(stack.UTF8(EXTMemoryBudget.VK_EXT_MEMORY_BUDGET_EXTENSION_NAME));

            VkDeviceCreateInfo info = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pQueueCreateInfos(queueCreateInfos)
                    .ppEnabledExtensionNames(extensions)
                    .pNext(deviceFeatures.address());

            PointerBuffer pDevice = stack.mallocPointer(1);
            VkUtil.check(VK10.vkCreateDevice(physicalDevice, info, null, pDevice));
            return new VkDevice(pDevice.get(0), physicalDevice, info, VK13.VK_API_VERSION_1_3);
        }
    }

    private static long createAllocator(VkInstance instance, VkDevice device, VkPhysicalDevice physicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions functions = VmaVulkanFunctions.calloc(stack).set(instance, device);

            VmaAllocatorCreateInfo info = VmaAllocatorCreateInfo.calloc(stack)
                    .physicalDevice(physicalDevice)
                    .device(device)
                    .pVulkanFunctions(functions)
                    .instance(instance)
                    .vulkanApiVersion(VK13.VK_API_VERSION_1_3)
                    .flags(Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT
                            | Vma.VMA_ALLOCATOR_CREATE_EXT_MEMORY_BUDGET_BIT);

            PointerBuffer pAllocator = stack.mallocPointer(1);
            VkUtil.check(Vma.vmaCreateAllocator(info, pAllocator));
            return pAllocator.get(0);
        }
    }

    private static VkQueue getQueue(VkDevice device, int family, int queueIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            VK10.vkGetDeviceQueue(device, family, queueIndex, pQueue);
            return new VkQueue(pQueue.get(0), device);
        }
    }

    public static HeapIndices findHeaps(VkPhysicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceMemoryProperties2 memoryProperties = VkPhysicalDeviceMemoryProperties2.calloc(stack).sType$Default();
            VK11.vkGetPhysicalDeviceMemoryProperties2(device, memoryProperties);

            HeapIndices heapIndices = new HeapIndices();
            int[] heapFlags = new int[VK10.VK_MAX_MEMORY_HEAPS];

            for (int i = 0; i < memoryProperties.memoryProperties().memoryTypeCount(); i++) {
                var memType = memoryProperties.memoryProperties().memoryTypes(i);
                heapFlags[memType.heapIndex()] |= memType.propertyFlags();
            }

            for (int heapIdx = 0; heapIdx < memoryProperties.memoryProperties().memoryHeapCount(); heapIdx++) {
                int flags = heapFlags[heapIdx];
                if ((flags & (VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT))
                        == (VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)) {
                    heapIndices.rebarIndex = heapIdx;
                    heapIndices.rebarProps = flags;
                }

                if ((flags & VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) == 0) {
                    heapIndices.hostIndex = heapIdx;
                    heapIndices.hostProps = flags;
                }
            }

            return heapIndices;
        }
    }

    public static QueueIndices findQueueFamilies(VkPhysicalDevice device) {
        QueueIndices indices = new QueueIndices();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pCount = stack.mallocInt(1);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, pCount, null);
            int count = pCount.get(0);
            if (count == 0) {
                return indices;
            }
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.calloc(count, stack);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, pCount, queueFamilies);

            Integer dedicatedComputeCandidate = null;
            Integer anyComputeCandidate = null;
            for (int i = 0; i < count; i++) {
                int flags = queueFamilies.get(i).queueFlags();
                if ((flags & VK10.VK_QUEUE_COMPUTE_BIT) == 0) continue;
                if (anyComputeCandidate == null) anyComputeCandidate = i;
                if ((flags & VK10.VK_QUEUE_GRAPHICS_BIT) == 0) {
                    dedicatedComputeCandidate = i;
                    break;
                }
            }

            if (dedicatedComputeCandidate != null) {
                indices.computeFamily = dedicatedComputeCandidate;
                indices.computeQueueIndex = 0;
            } else if (anyComputeCandidate != null) {
                indices.computeFamily = anyComputeCandidate;
                indices.computeQueueIndex = 0;
            } else {
                return indices;
            }
            indices.computeQueueCount = Math.min(
                    queueFamilies.get(indices.computeFamily).queueCount(), MAX_COMPUTE_QUEUES);

            Integer dedicatedTransferCandidate = null;
            for (int i = 0; i < count; i++) {
                int flags = queueFamilies.get(i).queueFlags();
                if ((flags & VK10.VK_QUEUE_TRANSFER_BIT) == 0) continue;
                if ((flags & (VK10.VK_QUEUE_GRAPHICS_BIT | VK10.VK_QUEUE_COMPUTE_BIT)) != 0) continue;
                if (i == indices.computeFamily) continue;
                dedicatedTransferCandidate = i;
                break;
            }

            if (dedicatedTransferCandidate != null) {
                indices.transferFamily = dedicatedTransferCandidate;
                indices.transferQueueIndex = 0;
            } else {
                int computeFamilyIndex = indices.computeFamily;
                indices.transferFamily = computeFamilyIndex;

                int[] usedIndices = new int[indices.computeQueueCount];
                for (int q = 0; q < indices.computeQueueCount; q++) usedIndices[q] = q;
                int queueCount = queueFamilies.get(computeFamilyIndex).queueCount();
                indices.transferQueueIndex = 0;
                for (int i = 0; i < queueCount; i++) {
                    boolean used = false;
                    for (int usedIndex : usedIndices) {
                        if (usedIndex == i) {
                            used = true;
                            break;
                        }
                    }
                    if (!used) {
                        indices.transferQueueIndex = i;
                        break;
                    }
                }
            }
        }

        return indices;
    }

    public int getQueueFamily(WorkQueue workQueue) {
        return switch (workQueue) {
            case COMPUTE -> this.queueIndices.computeFamily;
            case TRANSFER -> this.queueIndices.transferFamily;
        };
    }

    public void submitCommands(WorkQueue workQueue, VkSubmitInfo.Buffer submissions, long fence) {
        ArrayBlockingQueue<VkQueue> pool = switch (workQueue) {
            case COMPUTE -> this.computeQueuePool;
            case TRANSFER -> this.transferQueuePool;
        };

        VkQueue queue;
        try {
            queue = pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for a free queue", e);
        }
        try {
            VkUtil.check(VK10.vkQueueSubmit(queue, submissions, fence));
        } finally {
            pool.add(queue);
        }
    }

    public long acquireFence() {
        Long pooled = this.fencePool.poll();
        if (pooled != null) {
            VkUtil.check(VK10.vkResetFences(this.device, pooled));
            return pooled;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pFence = stack.mallocLong(1);
            VkUtil.check(VK10.vkCreateFence(this.device,
                    VkFenceCreateInfo.calloc(stack).sType$Default(), null, pFence));
            return pFence.get(0);
        }
    }

    public void releaseFence(long fence) {
        if (fence != VK10.VK_NULL_HANDLE) this.fencePool.add(fence);
    }

    public VulkanCommandPool getCommandPool(WorkQueue workQueue) {
        return switch (workQueue) {
            case COMPUTE -> this.computeCommandPool.get();
            case TRANSFER -> this.transferCommandPool.get();
        };
    }

    public VkDevice getDevice() {
        return this.device;
    }

    public VkPhysicalDevice getPhysicalDevice() {
        return this.physicalDevice;
    }

    public UUID getDeviceUUID() {
        return this.deviceUUID;
    }

    public float getTimestampPeriod() {
        return this.timestampPeriod;
    }

    public long getAllocator() {
        return this.allocator;
    }

    public VkServerGlobalContext getGlobalContext() {
        return this.globalContext;
    }

    public QueueIndices getQueueIndices() {
        return this.queueIndices;
    }

    public HeapIndices getHeapIndices() {
        return this.heapIndices;
    }

    @Override
    public String toString() {
        return this.deviceName + " (" + this.deviceUUID + ")";
    }

    @Override
    public void close() {
        if (!this.open.compareAndSet(true, false)) return;
        VK10.vkDeviceWaitIdle(this.device);

        for (Long fence : this.fencePool) VK10.vkDestroyFence(this.device, fence, null);
        this.fencePool.clear();
        this.computePoolTracking.forEach(VulkanCommandPool::close);
        this.transferPoolTracking.forEach(VulkanCommandPool::close);
        Vma.vmaDestroyAllocator(this.allocator);
        VK10.vkDestroyDevice(this.device, null);
        LOGGER.info("Closed Vulkan device: {}", this.deviceName);
    }

    public enum WorkQueue {
        COMPUTE,
        TRANSFER
    }

    public static class QueueIndices {

        public Integer computeFamily;
        public int computeQueueIndex;
        public int computeQueueCount;
        public Integer transferFamily;
        public int transferQueueIndex;

        QueueIndices() {
            this.computeFamily = null;
            this.computeQueueIndex = 0;
            this.computeQueueCount = 1;
            this.transferFamily = null;
            this.transferQueueIndex = 0;
        }

        public boolean isComplete() {
            return this.computeFamily != null && this.transferFamily != null;
        }
    }

    public static class HeapIndices {

        public Integer rebarProps;
        public int rebarIndex;
        public Integer hostProps;
        public int hostIndex;

        HeapIndices() {
            this.rebarProps = null;
            this.rebarIndex = 0;
            this.hostProps = null;
            this.hostIndex = 0;
        }

        public boolean isComplete() {
            return this.rebarProps != null && this.hostProps != null;
        }
    }
}
