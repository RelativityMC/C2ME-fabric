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

package com.ishland.c2me.opts.accel.opencl.common.workarounds.nvidia;

import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.NVDeviceAttributeQuery;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

public class NvidiaWorkarounds {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvidiaWorkarounds.class);

    public static boolean isNvidia(OpenCLDeviceMetadata metadata) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer vendorIdBuf = stack.callocInt(1);
            CLUtil.checkCLError(CL12.clGetDeviceInfo(metadata.devicePtr, CL12.CL_DEVICE_VENDOR_ID, vendorIdBuf, null));
            int vendorId = vendorIdBuf.get(0);
            return vendorId == 0x10de;
        }
    }

    public static boolean isOlderThanSM50(OpenCLDeviceMetadata metadata) {
        if (!isNvidia(metadata)) return false;

        if (metadata.deviceCaps.cl_nv_device_attribute_query) {
            int computeCapabilityMajor = CLUtil.getDeviceInfoInt(metadata.devicePtr, NVDeviceAttributeQuery.CL_DEVICE_COMPUTE_CAPABILITY_MAJOR_NV);
            return computeCapabilityMajor < 5;
        } else {
            LOGGER.warn("Unable to determine compute capability for device {}: cl_nv_device_attribute_query not supported", metadata);
            return false;
        }
    }
}
