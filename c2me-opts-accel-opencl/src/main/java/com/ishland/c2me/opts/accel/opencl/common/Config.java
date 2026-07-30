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

package com.ishland.c2me.opts.accel.opencl.common;

import com.ishland.c2me.base.common.config.ConfigSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;

public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    public static final int maxConcurrentTasksPerDevice = (int) new ConfigSystem.ConfigAccessor()
            .key("openclAccel.maxConcurrentTasksPerDevice")
            .comment("""
                    Maximum number of concurrent tasks per device
                    Increasing this may increase the performance and will increase the VRAM usage
                    """)
            .getLong(32, 32, ConfigSystem.LongChecks.THREAD_COUNT);

    public static final boolean lowPriorityQueues = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.lowPriorityQueues")
            .comment("""
                    Whether to attempt to lower the priority of the command queues
                    This may reduce the fps drop caused by the OpenCL acceleration
                    
                    Requires cl_khr_priority_hints and cl_khr_throttle_hints
                    """)
            .getBoolean(true, true);

    public static final boolean preferFastCompilation = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.preferFastCompilation")
            .comment("""
                    Whether to prefer fast compilation with noinline hints
                    """)
            .getBoolean(true, true);

    public static final boolean allowCPUDevices = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.allowCPUDevices")
            .comment("""
                    Whether to allow the usage of CPU OpenCL devices
                    """)
            .getBoolean(false, false);

    public static final boolean allowGPUDevices = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.allowGPUDevices")
            .comment("""
                    Whether to allow the usage of GPU OpenCL devices
                    """)
            .getBoolean(true, false);

    public static final boolean allowAcceleratorDevices = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.allowAcceleratorDevices")
            .comment("""
                    Whether to allow the usage of Accelerator OpenCL devices
                    """)
            .getBoolean(true, false);

    public static final boolean allowIncompatibilityFallback = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.allowIncompatibilityFallback")
            .comment("""
                    Whether to allow falling back to non-OpenCL world generation if OpenCL initialization fails
                    """)
            .getBoolean(false, false);

    public static final boolean disableBuiltinDeviceBlocklist = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.disableBuiltinDeviceBlocklist")
            .comment("""
                    Overrides the built-in blocklist and enables acceleration on unsupported configurations
                    """)
            .getBoolean(false, false);

    public static final boolean enableIntelFastCompilation = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.enableIntelFastCompilation")
            .comment("""
                    Enables fast compilation options for intel GPUs
                    """)
            .getBoolean(true, false);

    public static final boolean enableNvidiaFastCompilation = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.enableNvidiaFastCompilation")
            .comment("""
                    Enables fast compilation options for nvidia GPUs
                    
                    This *will* decrease GPU-bound throughput by roughly 20%
                    """)
            .getBoolean(true, false);

    public static final boolean useSmallerBatches = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.useSmallerBatches")
            .comment("""
                    Whether to use smaller batches in worldgen
                    
                    Smaller batches helps older iGPUs to avoid tripping GPU timeouts,
                    but it *will* reduce scheduling efficiency for modern GPUs
                    """)
            .getBoolean(false, false);

    public static final boolean useDevicePrioritization = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.useDevicePrioritization")
            .comment("""
                    Whether to use the default device prioritization strategy
                    
                    For stability reasons:
                    - Disable Intel GPUs if anything else is present on Windows
                    - Disable Intel Gen9 iGPUs if anything else is present on Linux
                    - Disable Nvidia GPUs if anything else is present on Linux
                    """)
            .getBoolean(true, false);

    public static final boolean doExplicitFlushes = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.doExplicitFlushes")
            .comment("""
                    Whether to perform explicit flushes after every batch
                    """)
            .getBoolean(false, false);

    public static final String deviceUUIDBlacklistRaw = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.deviceUUIDBlacklist")
            .comment("""
                    A comma-separated list of device UUIDs to blacklist
                    You can find the UUIDs in the log when the OpenCL devices are enumerated
                    Example: "951e5ce5-ccec-4a37-9ece-d0a800662d8f,3e825b77-fa62-403e-9155-aed1c014b2a2"
                    """)
            .getString("", "");

    public static final String deviceUUIDWhitelistRaw = new ConfigSystem.ConfigAccessor()
            .key("openclAccel.deviceUUIDWhitelist")
            .comment("""
                    A comma-separated list of device UUIDs to whitelist
                    If non-empty, only the devices in this list will be used
                    You can find the UUIDs in the log when the OpenCL devices are enumerated
                    Example: "951e5ce5-ccec-4a37-9ece-d0a800662d8f,3e825b77-fa62-403e-9155-aed1c014b2a2"
                    """)
            .getString("", "");

    public static final HashSet<UUID> deviceUUIDBlacklist = new HashSet<>();
    public static final HashSet<UUID> deviceUUIDWhitelist = new HashSet<>();

    static {
        for (String s : deviceUUIDBlacklistRaw.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                try {
                    deviceUUIDBlacklist.add(UUID.fromString(trimmed));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in openclAccel.deviceUUIDBlacklist: {}", trimmed);
                }
            }
        }
        for (String s : deviceUUIDWhitelistRaw.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                try {
                    deviceUUIDWhitelist.add(UUID.fromString(trimmed));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in openclAccel.deviceUUIDWhitelist: {}", trimmed);
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
            value = 192;
            System.setProperty("chunky.maxWorkingCount", Integer.toString(value));
        }
        LOGGER.info("chunky.maxWorkingCount: {}", value);
    }

}
