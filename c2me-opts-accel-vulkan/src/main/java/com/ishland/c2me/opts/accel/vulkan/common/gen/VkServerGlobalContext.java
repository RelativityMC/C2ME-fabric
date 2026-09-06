package com.ishland.c2me.opts.accel.vulkan.common.gen;

import com.ishland.c2me.opts.accel.vulkan.common.Config;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.EXTMemoryBudget;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.EXTValidationFeatures;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkValidationFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceIDProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VkServerGlobalContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(VkServerGlobalContext.class);

    private static final boolean VALIDATION = Boolean.getBoolean("c2me.vulkan.debug.validation");
    private static final boolean SYNC_VALIDATION = Boolean.getBoolean("c2me.vulkan.debug.syncValidation");

    private static final String LAYER_KHRONOS_VALIDATION = "VK_LAYER_KHRONOS_validation";

    private static final String[] DEVICE_EXTENSIONS = { EXTMemoryBudget.VK_EXT_MEMORY_BUDGET_EXTENSION_NAME };

    private final VkInstance instance;
    private final long debugMessenger;
    private final List<VkPhysicalDevice> physicalDevices;

    private final ArrayList<OpenDevice> openDevices = new ArrayList<>();
    private final ReferenceArrayList<VkServerWorldContext> registeredWorlds = new ReferenceArrayList<>();

    final ReentrantLock takeLock = new ReentrantLock();
    final Condition notEmpty = this.takeLock.newCondition();

    public VkServerGlobalContext() {
        this.instance = createInstance();
        this.debugMessenger = VALIDATION ? setupDebugMessenger(this.instance) : VK10.VK_NULL_HANDLE;
        this.physicalDevices = pickPhysicalDevices(this.instance);
    }

    public VkInstance getInstance() {
        return this.instance;
    }

    public List<VkPhysicalDevice> getPhysicalDevices() {
        return this.physicalDevices;
    }

    private static VkInstance createInstance() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationName(stack.UTF8("C2ME"))
                    .applicationVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
                    .pEngineName(stack.UTF8("C2ME Vulkan Acceleration"))
                    .engineVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
                    .apiVersion(VK13.VK_API_VERSION_1_2);

            PointerBuffer extensions = null;
            PointerBuffer layers = null;
            if (VALIDATION) {
                // VkValidationFeaturesEXT is only honoured when its instance extension is enabled.
                extensions = SYNC_VALIDATION
                        ? stack.pointers(
                                stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME),
                                stack.UTF8(EXTValidationFeatures.VK_EXT_VALIDATION_FEATURES_EXTENSION_NAME))
                        : stack.pointers(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
                layers = stack.pointers(stack.UTF8(LAYER_KHRONOS_VALIDATION));
            }

            VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationInfo(appInfo)
                    .ppEnabledExtensionNames(extensions)
                    .ppEnabledLayerNames(layers);

            if (VALIDATION && SYNC_VALIDATION) {
                IntBuffer enabled = stack.ints(
                        EXTValidationFeatures.VK_VALIDATION_FEATURE_ENABLE_SYNCHRONIZATION_VALIDATION_EXT);
                VkValidationFeaturesEXT validationFeatures = VkValidationFeaturesEXT.calloc(stack)
                        .sType$Default()
                        .pEnabledValidationFeatures(enabled);
                instanceInfo.pNext(validationFeatures.address());
            }

            PointerBuffer pInstance = stack.mallocPointer(1);
            VkUtil.check(VK10.vkCreateInstance(instanceInfo, null, pInstance));
            return new VkInstance(pInstance.get(0), instanceInfo);
        }
    }

    private static long setupDebugMessenger(VkInstance instance) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDebugUtilsMessengerCreateInfoEXT debugMessengerInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
                    .sType$Default()
                    .messageSeverity(
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                                    | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
                                    | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT)
                    .messageType(
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                                    | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                                    | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
                    .pfnUserCallback(VkServerGlobalContext::debugCallback);

            LongBuffer pMessenger = stack.mallocLong(1);
            VkUtil.check(EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, debugMessengerInfo, null, pMessenger));
            return pMessenger.get(0);
        }
    }

    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        String message = callbackData.pMessageString();

        String stype = switch (messageType) {
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT -> "GENERAL: ";
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT -> "VALIDATION: ";
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT -> "PERFORMANCE: ";
            default -> "OTHER: ";
        };

        boolean isWarnOrErr = (messageSeverity & (EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)) != 0;
        if (!isWarnOrErr && !message.contains("C2MEDBG")) {
            return VK10.VK_FALSE;
        }

        if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
            LOGGER.error("{}{}", stype, message);
        } else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
            LOGGER.warn("{}{}", stype, message);
        } else {
            LOGGER.info("{}{}", stype, message);
        }

        return VK10.VK_FALSE;
    }

    private static boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {
        int count;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pCount = stack.mallocInt(1);
            VkUtil.check(VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pCount, null));
            count = pCount.get(0);
        }
        if (count == 0) return DEVICE_EXTENSIONS.length == 0;

        VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.calloc(count);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pCount = stack.ints(count);
            VkUtil.check(VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pCount, availableExtensions));

            Set<String> requiredExtensionsSet = new HashSet<>(List.of(DEVICE_EXTENSIONS));
            for (int i = 0; i < count; i++) {
                requiredExtensionsSet.remove(availableExtensions.get(i).extensionNameString());
            }
            return requiredExtensionsSet.isEmpty();
        } finally {
            availableExtensions.free();
        }
    }

    private static boolean isDeviceSuitable(VkPhysicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties2 properties2 = VkPhysicalDeviceProperties2.calloc(stack)
                    .sType$Default();

            VK11.vkGetPhysicalDeviceProperties2(device, properties2);

            String name = properties2.properties().deviceNameString();

            if (properties2.properties().apiVersion() < VK13.VK_API_VERSION_1_2) {
                LOGGER.warn("Vulkan device ({}) does not support Vulkan 1.2", name);
                return false;
            }

            if (!deviceTypeAllowed(properties2.properties().deviceType())) {
                LOGGER.info("Skipping Vulkan device ({}): device type disabled by config", name);
                return false;
            }

            UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));

            if (Config.deviceUUIDBlacklist.contains(uuid)) {
                LOGGER.info("Skipping Vulkan device ({}, {}): blacklisted", name, uuid);
                return false;
            }
            if (!Config.deviceUUIDWhitelist.isEmpty() && !Config.deviceUUIDWhitelist.contains(uuid)) {
                LOGGER.info("Skipping Vulkan device ({}, {}): not whitelisted", name, uuid);
                return false;
            }

            VulkanDevice.QueueIndices queueIndices = VulkanDevice.findQueueFamilies(device);

            if (!queueIndices.isComplete()) {
                LOGGER.warn("Vulkan device ({}) has no compute-capable queue family", name);
                return false;
            }

            VulkanDevice.HeapIndices heapIndices = VulkanDevice.findHeaps(device);

            if (!heapIndices.isComplete()) {
                LOGGER.warn("Vulkan device ({}) has no usable memory heaps", name);
                return false;
            }

            if (!checkDeviceExtensionSupport(device)) {
                LOGGER.warn("Vulkan device ({}) is missing a required device extension", name);
                return false;
            }

            VkPhysicalDeviceVulkan12Features vulkan12Features = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
            VkPhysicalDeviceVulkan11Features vulkan11Features = VkPhysicalDeviceVulkan11Features.calloc(stack)
                    .sType$Default()
                    .pNext(vulkan12Features.address());

            VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc(stack)
                    .sType$Default()
                    .pNext(vulkan11Features.address());

            VK11.vkGetPhysicalDeviceFeatures2(device, features2);

            if (!features2.features().shaderInt64() ||
                    !features2.features().shaderFloat64() ||
                    !vulkan12Features.bufferDeviceAddress() ||
                    !vulkan12Features.scalarBlockLayout() ||
                    !vulkan12Features.shaderInt8() ||
                    !vulkan12Features.storageBuffer8BitAccess() ||
                    !features2.features().shaderInt16() ||
                    !vulkan11Features.storageBuffer16BitAccess()) {
                LOGGER.warn("Vulkan device ({}) is missing a required feature (shaderInt64={}, shaderFloat64={}, bufferDeviceAddress={}, scalarBlockLayout={}, shaderInt8={}, storageBuffer8BitAccess={}, shaderInt16={}, storageBuffer16BitAccess={})",
                        name,
                        features2.features().shaderInt64(),
                        features2.features().shaderFloat64(),
                        vulkan12Features.bufferDeviceAddress(),
                        vulkan12Features.scalarBlockLayout(),
                        vulkan12Features.shaderInt8(),
                        vulkan12Features.storageBuffer8BitAccess(),
                        features2.features().shaderInt16(),
                        vulkan11Features.storageBuffer16BitAccess());
                return false;
            }
            return true;
        }
    }

    private static List<VkPhysicalDevice> pickPhysicalDevices(VkInstance instance) {
        List<VkPhysicalDevice> accepted = new ArrayList<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pCount = stack.mallocInt(1);
            VkUtil.check(VK10.vkEnumeratePhysicalDevices(instance, pCount, null));
            int count = pCount.get(0);
            if (count == 0) {
                LOGGER.warn("No Vulkan physical devices found");
                return List.of();
            }
            PointerBuffer pDevices = stack.mallocPointer(count);
            VkUtil.check(VK10.vkEnumeratePhysicalDevices(instance, pCount, pDevices));

            for (int i = 0; i < count; i++) {
                VkPhysicalDevice device = new VkPhysicalDevice(pDevices.get(i), instance);
                if (!isDeviceSuitable(device)) continue;

                VkPhysicalDeviceIDProperties idProperties = VkPhysicalDeviceIDProperties.calloc(stack).sType$Default();
                VkPhysicalDeviceProperties2 properties2 = VkPhysicalDeviceProperties2.calloc(stack)
                        .sType$Default()
                        .pNext(idProperties);
                VK11.vkGetPhysicalDeviceProperties2(device, properties2);

                String name = properties2.properties().deviceNameString();

                LOGGER.info("Physical device: {}", name);
                accepted.add(device);
            }
        }

        if (accepted.isEmpty()) {
            LOGGER.warn("No Vulkan device met the requirements for accelerated world generation");
        }
        return List.copyOf(accepted);
    }

    private static boolean deviceTypeAllowed(int deviceType) {
        return switch (deviceType) {
            case VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU, VK10.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU -> Config.allowGPUDevices;
            case VK10.VK_PHYSICAL_DEVICE_TYPE_CPU -> Config.allowCPUDevices;
            default -> Config.allowOtherDevices;
        };
    }

    public boolean openDevice(VkPhysicalDevice physicalDevice) {
        OpenDevice opened;
        synchronized (this) {
            for (OpenDevice openDevice : this.openDevices) {
                if (openDevice.device().getPhysicalDevice().equals(physicalDevice)) return false;
            }

            opened = new OpenDevice(new VulkanDevice(this, physicalDevice),
                    new SubmissionPermits(Config.maxConcurrentTasksPerDevice, this::signalNotEmpty));
            this.openDevices.add(opened);
        }

        for (VkServerWorldContext world : this.registeredWorlds) world.addDevice(opened.device(), opened.permits());
        return true;
    }

    public void closeDevice(VkPhysicalDevice physicalDevice) {
        OpenDevice device = null;
        synchronized (this) {
            for (OpenDevice openDevice : this.openDevices) {
                if (openDevice.device().getPhysicalDevice().equals(physicalDevice)) {
                    device = openDevice;
                    this.openDevices.remove(openDevice);
                    break;
                }
            }
        }
        closeDevice0(device);
    }

    private void closeDevice0(OpenDevice openDevice) {
        if (openDevice == null) return;
        for (VkServerWorldContext world : this.registeredWorlds) world.removeDevice(openDevice.device());
        openDevice.device().close();
    }

    public synchronized void closeAllDevices() {
        for (OpenDevice openDevice : this.openDevices) {
            try {
                closeDevice0(openDevice);
            } catch (Throwable t) {
                LOGGER.error("Failed to close Vulkan device", t);
            }
        }
        this.openDevices.clear();
    }

    void registerWorld(VkServerWorldContext world) {
        this.registeredWorlds.add(world);
        for (OpenDevice openDevice : this.openDevices) world.addDevice(openDevice.device(), openDevice.permits());
    }

    void unregisterWorld(VkServerWorldContext world) {
        this.registeredWorlds.remove(world);
        for (OpenDevice openDevice : this.openDevices) world.removeDevice(openDevice.device());
    }

    void signalNotEmpty() {
        this.takeLock.lock();
        try {
            this.notEmpty.signal();
        } finally {
            this.takeLock.unlock();
        }
    }

    public List<VulkanDevice> getOpenDevices() {
        return this.openDevices.stream().map(OpenDevice::device).toList();
    }

    private record OpenDevice(VulkanDevice device, SubmissionPermits permits) {
    }

    public void close() {
        closeAllDevices();
        if (this.debugMessenger != VK10.VK_NULL_HANDLE) {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(this.instance, this.debugMessenger, null);
        }
        VK10.vkDestroyInstance(this.instance, null);
    }
}
