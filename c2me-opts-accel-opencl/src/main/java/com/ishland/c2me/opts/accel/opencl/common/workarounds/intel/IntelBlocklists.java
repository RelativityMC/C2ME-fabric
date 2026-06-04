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
import org.lwjgl.opencl.INTELDeviceAttributeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntelBlocklists {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntelBlocklists.class);

    public static boolean isARL_S(OpenCLDeviceMetadata metadata) {
        if (!IntelWorkarounds.isUsingIntelGPU(metadata)) {
            return false;
        }

        if (metadata.deviceCaps.cl_intel_device_attribute_query) {
            int pciId = CLUtil.getDeviceInfoInt(metadata.devicePtr, INTELDeviceAttributeQuery.CL_DEVICE_ID_INTEL);
            return pciId == 0x7D67 || pciId == 0xB640;
        } else {
            LOGGER.warn("Unable to determine microarchitecture level for device {}: cl_intel_device_attribute_query not supported", metadata);
            return false;
        }
    }

}
