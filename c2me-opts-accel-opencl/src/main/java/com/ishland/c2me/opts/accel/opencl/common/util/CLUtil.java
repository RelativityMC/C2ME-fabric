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

package com.ishland.c2me.opts.accel.opencl.common.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.KHRDeviceUUID;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

import static org.lwjgl.opencl.CL10.CL_SUCCESS;
import static org.lwjgl.opencl.CL10.clGetDeviceInfo;
import static org.lwjgl.opencl.CL10.clGetPlatformInfo;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class CLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLUtil.class);

    public static void checkCLError(IntBuffer errcode) {
        checkCLError(errcode.get(errcode.position()));
    }

    public static void checkCLError(int errcode) {
        if (errcode != CL_SUCCESS) {
            throw new RuntimeException(String.format("OpenCL error [%d]", errcode));
        }
    }

    private static void printPlatformInfo(long platform, String param_name, int param) {
        System.out.println(param_name + ": " + getPlatformInfoStringUTF8(platform, param));
    }

    private static void printDeviceInfo(long device, String param_name, int param) {
        System.out.println("\t" + param_name + ": " + getDeviceInfoStringUTF8(device, param));
    }

    public static String getPlatformInfoStringUTF8(long cl_platform_id, int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            checkCLError(clGetPlatformInfo(cl_platform_id, param_name, (ByteBuffer) null, pp));
            int bytes = (int) pp.get(0);

            ByteBuffer buffer = stack.malloc(bytes);
            checkCLError(clGetPlatformInfo(cl_platform_id, param_name, buffer, null));

            return MemoryUtil.memUTF8(buffer, bytes - 1);
        } catch (Throwable t) {
            LOGGER.error("Failed to get platform info {} for {}", param_name, cl_platform_id, t);
            return "N/A";
        }
    }

    public static int getDeviceInfoInt(long cl_device_id, int param_name) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pl = stack.mallocInt(1);
            checkCLError(clGetDeviceInfo(cl_device_id, param_name, pl, null));
            return pl.get(0);
        }
    }

    public static long getDeviceInfoLong(long cl_device_id, int param_name) {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pl = stack.mallocLong(1);
            checkCLError(clGetDeviceInfo(cl_device_id, param_name, pl, null));
            return pl.get(0);
        }
    }

    public static long getDeviceInfoPointer(long cl_device_id, int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            checkCLError(clGetDeviceInfo(cl_device_id, param_name, pp, null));
            return pp.get(0);
        }
    }

    public static String getDeviceInfoStringUTF8(long cl_device_id, int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            checkCLError(clGetDeviceInfo(cl_device_id, param_name, (ByteBuffer) null, pp));
            int bytes = (int) pp.get(0);

            ByteBuffer buffer = stack.malloc(bytes);
            checkCLError(clGetDeviceInfo(cl_device_id, param_name, buffer, null));

            return memUTF8(buffer, bytes - 1);
        } catch (Throwable t) {
            LOGGER.error("Failed to get device info {} for {}", param_name, cl_device_id, t);
            return "N/A";
        }
    }

    public static UUID getDeviceUUID(long cl_device_id) {
        try (MemoryStack stack = stackPush()) {
            ByteBuffer buffer = stack.malloc(KHRDeviceUUID.CL_UUID_SIZE_KHR);
            checkCLError(clGetDeviceInfo(cl_device_id, KHRDeviceUUID.CL_DEVICE_UUID_KHR, buffer, null));
            buffer.rewind();
            return new UUID(buffer.getLong(), buffer.getLong());
        }
    }

    public static UUID getDriverUUID(long cl_device_id) {
        try (MemoryStack stack = stackPush()) {
            ByteBuffer buffer = stack.malloc(KHRDeviceUUID.CL_UUID_SIZE_KHR);
            checkCLError(clGetDeviceInfo(cl_device_id, KHRDeviceUUID.CL_DRIVER_UUID_KHR, buffer, null));
            buffer.rewind();
            return new UUID(buffer.getLong(), buffer.getLong());
        }
    }

    public static boolean getDeviceInfoBoolean(long cl_device_id, int param_name) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pl = stack.callocInt(1);
            checkCLError(clGetDeviceInfo(cl_device_id, param_name, pl, null));
            return pl.get(0) != 0;
        }
    }

}
