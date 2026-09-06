package com.ishland.c2me.opts.accel.vulkan.common.gen;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSpecializationInfo;
import org.lwjgl.vulkan.VkSpecializationMapEntry;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class VulkanPipeline implements Closeable {

    private final VulkanDevice device;
    private final long shaderModule;
    private final long pipelineLayout;
    private final long pipeline;

    private final AtomicBoolean open = new AtomicBoolean(true);

    private VulkanPipeline(VulkanDevice device, long shaderModule, long pipelineLayout, long pipeline) {
        this.device = device;
        this.shaderModule = shaderModule;
        this.pipelineLayout = pipelineLayout;
        this.pipeline = pipeline;
    }

    public static VulkanPipeline createComputePipeline(VulkanDevice device, int[] spirv,
                                                       long[] descriptorSetLayouts, int pushConstantSize,
                                                       Int2IntMap specializationConstants, long pipelineCache) {
        long shaderModule = loadShaderModule(device, spirv);
        long pipelineLayout = createPipelineLayout(device, descriptorSetLayouts, pushConstantSize);
        long pipeline = createComputePipeline(device, shaderModule, pipelineLayout, specializationConstants, pipelineCache);
        return new VulkanPipeline(device, shaderModule, pipelineLayout, pipeline);
    }

    private static long loadShaderModule(VulkanDevice device, int[] spirv) {
        ByteBuffer code = MemoryUtil.memAlloc(spirv.length * Integer.BYTES);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            code.asIntBuffer().put(spirv);

            VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.calloc(stack)
                    .sType$Default()
                    .pCode(code);

            LongBuffer pModule = stack.mallocLong(1);
            VkUtil.check(VK10.vkCreateShaderModule(device.getDevice(), info, null, pModule));
            return pModule.get(0);
        } finally {
            MemoryUtil.memFree(code);
        }
    }

    private static long createPipelineLayout(VulkanDevice device, long[] descriptorSetLayouts, int pushConstantSize) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo info = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default();

            if (descriptorSetLayouts.length != 0) {
                info.pSetLayouts(stack.longs(descriptorSetLayouts));
            }
            if (pushConstantSize != 0) {
                info.pPushConstantRanges(VkPushConstantRange.calloc(1, stack)
                        .stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
                        .offset(0)
                        .size(pushConstantSize));
            }

            LongBuffer pLayout = stack.mallocLong(1);
            VkUtil.check(VK10.vkCreatePipelineLayout(device.getDevice(), info, null, pLayout));
            return pLayout.get(0);
        }
    }

    private static long createComputePipeline(VulkanDevice device, long shaderModule, long layout,
                                              Int2IntMap specializationConstants, long pipelineCache) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineShaderStageCreateInfo stage = VkPipelineShaderStageCreateInfo.calloc(stack)
                    .sType$Default()
                    .stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
                    .module(shaderModule)
                    .pName(stack.UTF8("main"));

            if (!specializationConstants.isEmpty()) {
                int count = specializationConstants.size();
                VkSpecializationMapEntry.Buffer entries = VkSpecializationMapEntry.calloc(count, stack);
                ByteBuffer data = stack.malloc(count * Integer.BYTES);

                int index = 0;
                for (Int2IntMap.Entry entry : specializationConstants.int2IntEntrySet()) {
                    entries.get(index)
                            .constantID(entry.getIntKey())
                            .offset(index * Integer.BYTES)
                            .size(Integer.BYTES);
                    data.putInt(index * Integer.BYTES, entry.getIntValue());
                    index++;
                }

                stage.pSpecializationInfo(VkSpecializationInfo.calloc(stack)
                        .pMapEntries(entries)
                        .pData(data));
            }

            VkComputePipelineCreateInfo.Buffer info = VkComputePipelineCreateInfo.calloc(1, stack)
                    .sType$Default()
                    .layout(layout)
                    .stage(stage);

            LongBuffer pPipeline = stack.mallocLong(1);
            VkUtil.check(VK10.vkCreateComputePipelines(device.getDevice(), pipelineCache, info, null, pPipeline));
            return pPipeline.get(0);
        }
    }

    public long getPipeline() {
        return this.pipeline;
    }

    public long getPipelineLayout() {
        return this.pipelineLayout;
    }

    @Override
    public void close() {
        if (!this.open.compareAndSet(true, false)) return;
        VK10.vkDestroyShaderModule(this.device.getDevice(), this.shaderModule, null);
        VK10.vkDestroyPipelineLayout(this.device.getDevice(), this.pipelineLayout, null);
        VK10.vkDestroyPipeline(this.device.getDevice(), this.pipeline, null);
    }

}
