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

package com.ishland.c2me.opts.accel.opencl.common.workarounds.amd;

import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import io.netty.util.internal.PlatformDependent;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class AMDBlocklists {

    public static boolean isUsingBrokenGCNOnOfficialAMDDriver(OpenCLDeviceMetadata metadata) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer vendorIdBuf = stack.callocInt(1);
            CLUtil.checkCLError(CL12.clGetDeviceInfo(metadata.devicePtr, CL12.CL_DEVICE_VENDOR_ID, vendorIdBuf, null));
            int vendorId = vendorIdBuf.get(0);
            if (vendorId != 0x1002) return false;
            String deviceName = CLUtil.getDeviceInfoStringUTF8(metadata.devicePtr, CL12.CL_DEVICE_NAME);

            if (!PlatformDependent.isWindows() && deviceName.startsWith("gfx90c")) {
                return false; // gfx90c is not broken on linux
            }

            return deviceName.startsWith("gfx9") ||
                    deviceName.startsWith("gfx8") ||
                    deviceName.startsWith("gfx7") ||
                    deviceName.startsWith("gfx6") ||
                    deviceName.equals("Ellesmere");
        }
    }

    public static boolean isUsingAM5IntegratedGPU(OpenCLDeviceMetadata metadata) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer vendorIdBuf = stack.callocInt(1);
            CLUtil.checkCLError(CL12.clGetDeviceInfo(metadata.devicePtr, CL12.CL_DEVICE_VENDOR_ID, vendorIdBuf, null));
            int vendorId = vendorIdBuf.get(0);
            if (vendorId != 0x1002) return false;
            String deviceName = CLUtil.getDeviceInfoStringUTF8(metadata.devicePtr, CL12.CL_DEVICE_NAME);

            return deviceName.equals("gfx1036");
        }
    }

}
