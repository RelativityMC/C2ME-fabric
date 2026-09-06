package com.ishland.c2me.opts.accel.vulkan.common.gen;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.io.Closeable;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VulkanCommandPool implements Closeable {

    private final VulkanDevice device;
    private final long pool;

    private final ArrayList<VkCommandBuffer> allocated = new ArrayList<>();
    private final ConcurrentLinkedQueue<VkCommandBuffer> available = new ConcurrentLinkedQueue<>();

    public VulkanCommandPool(VulkanDevice device, VulkanDevice.WorkQueue workQueue) {
        this.device = device;
        this.pool = createCommandPool(workQueue);
    }

    private long createCommandPool(VulkanDevice.WorkQueue workQueue) {
        VulkanDevice.QueueIndices queueIndices = this.device.getQueueIndices();

        int queueFamilyIndex = switch (workQueue) {
            case COMPUTE -> queueIndices.computeFamily;
            case TRANSFER -> queueIndices.transferFamily;
        };

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo info = VkCommandPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(queueFamilyIndex);

            LongBuffer pPool = stack.mallocLong(1);
            VkUtil.check(VK10.vkCreateCommandPool(this.device.getDevice(), info, null, pPool));
            return pPool.get(0);
        }
    }

    public VkCommandBuffer acquire() {
        VkCommandBuffer recycled = this.available.poll();
        if (recycled != null) {
            VkUtil.check(VK10.vkResetCommandBuffer(recycled, 0));
            return recycled;
        }
        VkCommandBuffer fresh = allocateCommandBuffer();
        this.allocated.add(fresh);
        return fresh;
    }

    public void release(VkCommandBuffer commandBuffer) {
        this.available.add(commandBuffer);
    }

    private VkCommandBuffer allocateCommandBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo info = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandBufferCount(1)
                    .commandPool(this.pool)
                    .level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            VkUtil.check(VK10.vkAllocateCommandBuffers(this.device.getDevice(), info, pCommandBuffer));
            return new VkCommandBuffer(pCommandBuffer.get(0), this.device.getDevice());
        }
    }

    @Override
    public void close() {
        this.available.clear();
        this.allocated.clear();
        VK10.vkDestroyCommandPool(this.device.getDevice(), this.pool, null);
    }

}
