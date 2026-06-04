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

package com.ishland.c2me.opts.accel.opencl.common.workarounds.intel;

import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.INTELDeviceAttributeQuery;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

public class IntelWorkarounds {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntelWorkarounds.class);

    public static final int DUMMY_GEN9_IP_VERSION = 0x90000; // something the Gen9 Windows driver reports

    public static final IntSet gen9IPs = IntSet.of(
            0x2400009, 0x2404009, 0x2408009, 0x240c000, 0x2410000, 0x2414000, 0x2418000, 0x241c000,
            DUMMY_GEN9_IP_VERSION
    );

    public static boolean isUsingIntelGPU(OpenCLDeviceMetadata metadata) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long deviceType = CLUtil.getDeviceInfoLong(metadata.devicePtr, CL12.CL_DEVICE_TYPE);
            int vendorId = CLUtil.getDeviceInfoInt(metadata.devicePtr, CL12.CL_DEVICE_VENDOR_ID);
            return deviceType == CL12.CL_DEVICE_TYPE_GPU && vendorId == 0x8086;
        }
    }

    public static boolean isUsingNEORuntime(OpenCLDeviceMetadata metadata) {
        if (!isUsingIntelGPU(metadata)) return false;

        String deviceVersion = CLUtil.getDeviceInfoStringUTF8(metadata.devicePtr, CL12.CL_DEVICE_VERSION);
        return deviceVersion.contains("NEO");
    }

    public static boolean isUsingGen9OnWindows(OpenCLDeviceMetadata metadata) {
        return PlatformDependent.isWindows() && isUsingGen9(metadata);
    }

    public static boolean isUsingGen9(OpenCLDeviceMetadata metadata) {
        if (!isUsingIntelGPU(metadata)) return false;

        if (metadata.deviceCaps.cl_intel_device_attribute_query) {
            int ipVersion = CLUtil.getDeviceInfoInt(metadata.devicePtr, INTELDeviceAttributeQuery.CL_DEVICE_IP_VERSION_INTEL);
            return gen9IPs.contains(ipVersion);
        } else {
            LOGGER.warn("Unable to determine microarchitecture level for device {}: cl_intel_device_attribute_query not supported", metadata);
            String deviceName = CLUtil.getDeviceInfoStringUTF8(metadata.devicePtr, CL12.CL_DEVICE_NAME);
            return deviceName.equals("Intel(R) HD Graphics 500") ||
                    deviceName.equals("Intel(R) HD Graphics 505") ||
                    deviceName.equals("Intel(R) HD Graphics 510") ||
                    deviceName.equals("Intel(R) HD Graphics 515") ||
                    deviceName.equals("Intel(R) HD Graphics 520") ||
                    deviceName.equals("Intel(R) HD Graphics 530") ||
                    deviceName.equals("Intel(R) HD Graphics P530") ||
                    deviceName.equals("Intel(R) Iris(R) Graphics 540") ||
                    deviceName.equals("Intel(R) Iris(R) Graphics 550") ||
                    deviceName.equals("Intel(R) Iris(TM) Pro Graphics 580") ||
                    deviceName.equals("Intel(R) Iris(TM) Pro Graphics P555") ||
                    deviceName.equals("Intel(R) Iris(TM) Pro Graphics P580") ||
                    deviceName.equals("Intel(R) HD Graphics 610") ||
                    deviceName.equals("Intel(R) HD Graphics 615") ||
                    deviceName.equals("Intel(R) HD Graphics 620") ||
                    deviceName.equals("Intel(R) HD Graphics 630") ||
                    deviceName.equals("Intel(R) HD Graphics P630") ||
                    deviceName.equals("Intel(R) Iris(R) Plus Graphics") || // some devices just use this generic name
                    deviceName.equals("Intel(R) Iris(R) Plus Graphics 640") ||
                    deviceName.equals("Intel(R) Iris(R) Plus Graphics 645") ||
                    deviceName.equals("Intel(R) Iris(R) Plus Graphics 650") ||
                    deviceName.equals("Intel(R) Iris(R) Plus Graphics 655") ||
                    deviceName.equals("Intel(R) UHD Graphics") || // some devices just use this generic name
                    deviceName.equals("Intel(R) UHD Graphics 600") ||
                    deviceName.equals("Intel(R) UHD Graphics 605") ||
                    deviceName.equals("Intel(R) UHD Graphics 610") ||
                    deviceName.equals("Intel(R) UHD Graphics 615") ||
                    deviceName.equals("Intel(R) UHD Graphics 617") ||
                    deviceName.equals("Intel(R) UHD Graphics 620") ||
                    deviceName.equals("Intel(R) UHD Graphics 630") ||
                    deviceName.equals("Intel(R) UHD Graphics P630");
        }
    }

    public static boolean isUsingIntelOnLinux(OpenCLDeviceMetadata metadata) {
        return !PlatformDependent.isWindows() && isUsingIntelGPU(metadata);
    }

    public static boolean isUsingIntelOnWindows(OpenCLDeviceMetadata metadata) {
        return PlatformDependent.isWindows() && isUsingIntelGPU(metadata);
    }

}
