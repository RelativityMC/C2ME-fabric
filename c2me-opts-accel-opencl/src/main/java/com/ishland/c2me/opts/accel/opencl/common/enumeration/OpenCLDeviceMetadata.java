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

package com.ishland.c2me.opts.accel.opencl.common.enumeration;

import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import com.ishland.c2me.opts.accel.opencl.common.workarounds.intel.IntelWorkarounds;
import org.lwjgl.opencl.CL30;
import org.lwjgl.opencl.CLCapabilities;

import java.util.Objects;
import java.util.UUID;

public class OpenCLDeviceMetadata {

    public final long platformPtr;
    public final long devicePtr;
    public final CLCapabilities platformCaps;
    public final CLCapabilities deviceCaps;
    public final UUID deviceUUID;
    public final UUID driverUUID;

    public final boolean supportsNonUniformWorkgroups;
    public final boolean isIntelNeoRuntime;

    OpenCLDeviceMetadata(long platformPtr, long devicePtr, CLCapabilities platformCaps, CLCapabilities deviceCaps, UUID deviceUUID, UUID driverUUID) {
        this.platformPtr = platformPtr;
        this.devicePtr = devicePtr;
        this.platformCaps = platformCaps;
        this.deviceCaps = deviceCaps;
        this.deviceUUID = deviceUUID;
        this.driverUUID = driverUUID;

        this.supportsNonUniformWorkgroups = this.deviceCaps.OpenCL30 && CLUtil.getDeviceInfoBoolean(this.devicePtr, CL30.CL_DEVICE_NON_UNIFORM_WORK_GROUP_SUPPORT);
        this.isIntelNeoRuntime = IntelWorkarounds.isUsingNEORuntime(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        OpenCLDeviceMetadata that = (OpenCLDeviceMetadata) object;
        return Objects.equals(deviceUUID, that.deviceUUID) && Objects.equals(driverUUID, that.driverUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceUUID, driverUUID);
    }

    @Override
    public String toString() {
        return "OpenCLDeviceMetadata{" +
                "platformCaps=" + platformCaps +
                ", deviceCaps=" + deviceCaps +
                ", deviceUUID=" + deviceUUID +
                ", driverUUID=" + driverUUID +
                '}';
    }
}
