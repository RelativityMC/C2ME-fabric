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
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class NvidiaWorkarounds {

    public static boolean isNvidia(OpenCLDeviceMetadata metadata) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer vendorIdBuf = stack.callocInt(1);
            CLUtil.checkCLError(CL12.clGetDeviceInfo(metadata.devicePtr, CL12.CL_DEVICE_VENDOR_ID, vendorIdBuf, null));
            int vendorId = vendorIdBuf.get(0);
            return vendorId == 0x10de;
        }
    }

}
