package com.ishland.c2me.opts.accel.vulkan.common.gen;

import org.lwjgl.vulkan.VK10;

public class VkUtil {

    private VkUtil() {
    }

    public static void check(int result) {
        if (result != VK10.VK_SUCCESS) {
            throw new VulkanException(caller() + " failed: " + resultName(result) + " (" + result + ")");
        }
    }

    private static String caller() {
        return StackWalker.getInstance()
                .walk(frames -> frames.skip(2).findFirst())
                .map(frame -> frame.getClassName() + "." + frame.getMethodName() + ":" + frame.getLineNumber())
                .orElse("<unknown>");
    }

    public static String resultName(int result) {
        return switch (result) {
            case VK10.VK_SUCCESS -> "VK_SUCCESS";
            case VK10.VK_NOT_READY -> "VK_NOT_READY";
            case VK10.VK_TIMEOUT -> "VK_TIMEOUT";
            case VK10.VK_EVENT_SET -> "VK_EVENT_SET";
            case VK10.VK_EVENT_RESET -> "VK_EVENT_RESET";
            case VK10.VK_INCOMPLETE -> "VK_INCOMPLETE";
            case VK10.VK_ERROR_OUT_OF_HOST_MEMORY -> "VK_ERROR_OUT_OF_HOST_MEMORY";
            case VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY -> "VK_ERROR_OUT_OF_DEVICE_MEMORY";
            case VK10.VK_ERROR_INITIALIZATION_FAILED -> "VK_ERROR_INITIALIZATION_FAILED";
            case VK10.VK_ERROR_DEVICE_LOST -> "VK_ERROR_DEVICE_LOST";
            case VK10.VK_ERROR_MEMORY_MAP_FAILED -> "VK_ERROR_MEMORY_MAP_FAILED";
            case VK10.VK_ERROR_LAYER_NOT_PRESENT -> "VK_ERROR_LAYER_NOT_PRESENT";
            case VK10.VK_ERROR_EXTENSION_NOT_PRESENT -> "VK_ERROR_EXTENSION_NOT_PRESENT";
            case VK10.VK_ERROR_FEATURE_NOT_PRESENT -> "VK_ERROR_FEATURE_NOT_PRESENT";
            case VK10.VK_ERROR_INCOMPATIBLE_DRIVER -> "VK_ERROR_INCOMPATIBLE_DRIVER";
            case VK10.VK_ERROR_TOO_MANY_OBJECTS -> "VK_ERROR_TOO_MANY_OBJECTS";
            case VK10.VK_ERROR_FORMAT_NOT_SUPPORTED -> "VK_ERROR_FORMAT_NOT_SUPPORTED";
            case VK10.VK_ERROR_FRAGMENTED_POOL -> "VK_ERROR_FRAGMENTED_POOL";
            case VK10.VK_ERROR_UNKNOWN -> "VK_ERROR_UNKNOWN";
            default -> "UNKNOWN";
        };
    }

    public static class VulkanException extends RuntimeException {
        public VulkanException(String message) {
            super(message);
        }
    }

}
