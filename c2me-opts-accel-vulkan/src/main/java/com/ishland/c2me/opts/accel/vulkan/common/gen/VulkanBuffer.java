package com.ishland.c2me.opts.accel.vulkan.common.gen;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class VulkanBuffer implements Closeable {

    private final VulkanDevice device;
    private final long buffer;
    private final long allocation;
    private final long bufferSize;

    private final AtomicBoolean open = new AtomicBoolean(true);

    private VulkanBuffer(VulkanDevice device, long buffer, long allocation, long bufferSize) {
        this.device = device;
        this.buffer = buffer;
        this.allocation = allocation;
        this.bufferSize = bufferSize;
    }

    public static VulkanBuffer create(VulkanDevice device, long bufferSize, int usage, int requiredFlags,
                                      int allocationFlags) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo info = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(bufferSize)
                    .usage(usage | VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT)
                    .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(Vma.VMA_MEMORY_USAGE_AUTO)
                    .requiredFlags(requiredFlags)
                    .flags(allocationFlags);

            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            VkUtil.check(Vma.vmaCreateBuffer(device.getAllocator(), info, allocationInfo, pBuffer, pAllocation, null));

            return new VulkanBuffer(device, pBuffer.get(0), pAllocation.get(0), bufferSize);
        }
    }

    public static VulkanBuffer createStorageBuffer(VulkanDevice device, long bufferSize) {
        return create(device, bufferSize,
                VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT | VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT
                        | Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_ALLOW_TRANSFER_INSTEAD_BIT
                        | Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT);
    }

    public static VulkanBuffer createStagingBuffer(VulkanDevice device, long bufferSize) {
        return create(device, bufferSize,
                VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT
                        | Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT);
    }

    public static VulkanBuffer createDeviceLocalStorageBuffer(VulkanDevice device, long bufferSize) {
        return create(device, bufferSize,
                VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT
                        | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                        | VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                0);
    }

    public static VulkanBuffer createReadbackBuffer(VulkanDevice device, long bufferSize) {
        return create(device, bufferSize,
                VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT
                        | Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT);
    }

    public void writeToBuffer(ByteBuffer data, long offset) {
        if (mappedData() == VK10.VK_NULL_HANDLE) {
            throw new IllegalStateException("Memory is not mapped, a staging buffer is required");
        }
        VkUtil.check(Vma.vmaCopyMemoryToAllocation(this.device.getAllocator(), data, this.allocation, offset));
    }

    public void readFromBuffer(ByteBuffer destination, long offset) {
        if (destination.remaining() > this.bufferSize) {
            throw new IllegalArgumentException("Destination is larger than the buffer");
        }
        VkUtil.check(Vma.vmaCopyAllocationToMemory(this.device.getAllocator(), this.allocation, offset, destination));
    }

    public void flush(long offset, long size) {
        Vma.vmaFlushAllocation(this.device.getAllocator(), this.allocation, offset, size);
    }

    public void invalidate(long offset, long size) {
        Vma.vmaInvalidateAllocation(this.device.getAllocator(), this.allocation, offset, size);
    }

    public long mappedData() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaAllocationInfo info = VmaAllocationInfo.calloc(stack);
            Vma.vmaGetAllocationInfo(this.device.getAllocator(), this.allocation, info);
            return info.pMappedData();
        }
    }

    public long deviceAddress() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferDeviceAddressInfo info = VkBufferDeviceAddressInfo.calloc(stack)
                    .sType$Default()
                    .buffer(this.buffer);
            return VK12.vkGetBufferDeviceAddress(this.device.getDevice(), info);
        }
    }

    public long getBuffer() {
        return this.buffer;
    }

    public long getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public void close() {
        if (!this.open.compareAndSet(true, false)) return;
        Vma.vmaDestroyBuffer(this.device.getAllocator(), this.buffer, this.allocation);
    }
}
