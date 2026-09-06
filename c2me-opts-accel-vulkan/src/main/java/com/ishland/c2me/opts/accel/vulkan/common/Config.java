package com.ishland.c2me.opts.accel.vulkan.common;

import com.ishland.c2me.base.common.config.ConfigSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;

public class Config {

    public static final int maxConcurrentTasksPerDevice = (int) new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.maxConcurrentTasksPerDevice")
            .comment("""
                    Maximum number of concurrent tasks per device
                    Increasing this may increase the performance and will increase the VRAM usage
                    """)
            .getLong(32, 32, ConfigSystem.LongChecks.THREAD_COUNT);
    public static final boolean optimizeGeneratedSpirV = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.optimizeGeneratedSpirV")
            .comment("""
                    Run SPIRV-Tools' optimizer over the generated shader modules before handing
                    them to the driver
                    
                    The generated half of each module is assembled instruction by instruction at
                    world load and carries redundancy a compiler would not emit, so this is worth
                    considerably more than it would be for hand-written shaders
                    """)
            .getBoolean(true, true);
    public static final boolean allowCPUDevices = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.allowCPUDevices")
            .comment("""
                    Whether to allow the usage of VK_PHYSICAL_DEVICE_TYPE_CPU devices
                    (software rasterizers such as lavapipe or SwiftShader)
                    """)
            .getBoolean(false, false);
    public static final boolean allowGPUDevices = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.allowGPUDevices")
            .comment("""
                    Whether to allow the usage of VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU
                    and VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU devices
                    """)
            .getBoolean(true, false);
    public static final boolean allowOtherDevices = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.allowOtherDevices")
            .comment("""
                    Whether to allow the usage of VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU
                    and VK_PHYSICAL_DEVICE_TYPE_OTHER devices
                    """)
            .getBoolean(true, false);
    public static final boolean allowIncompatibilityFallback = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.allowIncompatibilityFallback")
            .comment("""
                    Whether to allow falling back to non-Vulkan world generation if Vulkan initialization fails
                    """)
            .getBoolean(false, false);
    public static final boolean preserveAllControlFlows = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.preserveAllControlFlows")
            .comment("""
                    Uses old compiler behavior of preserving all control flows in the generated SPIR-V.
                    This will increase memory pressure and time used when compiling, but will usually produce faster code.
                    """)
            .getBoolean(true, false);
    public static final boolean useSmallerBatches = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.useSmallerBatches")
            .comment("""
                    Whether to use smaller batches in worldgen
                    
                    Smaller batches helps older iGPUs to avoid tripping GPU timeouts,
                    but it *will* reduce scheduling efficiency for modern GPUs
                    """)
            .getBoolean(false, false);
    public static final String deviceUUIDBlacklistRaw = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.deviceUUIDBlacklist")
            .comment("""
                    A comma-separated list of device UUIDs to blacklist
                    You can find the UUIDs in the log when the Vulkan devices are enumerated
                    Example: "951e5ce5-ccec-4a37-9ece-d0a800662d8f,3e825b77-fa62-403e-9155-aed1c014b2a2"
                    """)
            .getString("", "");
    public static final String deviceUUIDWhitelistRaw = new ConfigSystem.ConfigAccessor()
            .key("vulkanAccel.deviceUUIDWhitelist")
            .comment("""
                    A comma-separated list of device UUIDs to whitelist
                    If non-empty, only the devices in this list will be used
                    You can find the UUIDs in the log when the Vulkan devices are enumerated
                    Example: "951e5ce5-ccec-4a37-9ece-d0a800662d8f,3e825b77-fa62-403e-9155-aed1c014b2a2"
                    """)
            .getString("", "");
    public static final HashSet<UUID> deviceUUIDBlacklist = new HashSet<>();
    public static final HashSet<UUID> deviceUUIDWhitelist = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    static {
        for (String s : deviceUUIDBlacklistRaw.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                try {
                    deviceUUIDBlacklist.add(UUID.fromString(trimmed));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in vulkanAccel.deviceUUIDBlacklist: {}", trimmed);
                }
            }
        }
        for (String s : deviceUUIDWhitelistRaw.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                try {
                    deviceUUIDWhitelist.add(UUID.fromString(trimmed));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in vulkanAccel.deviceUUIDWhitelist: {}", trimmed);
                }
            }
        }
    }

    public static void init() {
        tryChunkyMaxWorkingCount();
    }

    private static void tryChunkyMaxWorkingCount() {
        String chunkyMaxWorkingCount = System.getProperty("chunky.maxWorkingCount", "");
        int value;
        try {
            value = Integer.parseInt(chunkyMaxWorkingCount);
        } catch (NumberFormatException e) {
            value = 0;
        }
        if (value == 0) {
            if (Runtime.getRuntime().maxMemory() > 10L * 1024L * 1024L * 1024L) {
                value = 512;
            } else {
                value = 192;
            }
            System.setProperty("chunky.maxWorkingCount", Integer.toString(value));
        }
        LOGGER.info("chunky.maxWorkingCount: {}", value);
    }

}
