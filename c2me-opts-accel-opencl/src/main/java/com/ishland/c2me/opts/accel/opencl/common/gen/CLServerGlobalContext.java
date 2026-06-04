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

package com.ishland.c2me.opts.accel.opencl.common.gen;

import com.google.common.collect.ImmutableList;
import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.shader_cache.ShaderCacheManager;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.lwjgl.opencl.CL12;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CLServerGlobalContext {

    private final ArrayList<OpenCLDevice> openDevices = new ArrayList<>();
    private final AtomicInteger currentDeviceIndex = new AtomicInteger(0);
    private final ReferenceArrayList<CLServerWorldContext> registeredWorlds = new ReferenceArrayList<>();
    final ShaderCacheManager shaderCacheManager = new ShaderCacheManager();
    final ReentrantLock takeLock = new ReentrantLock();
    final Condition notEmpty = this.takeLock.newCondition();

    public boolean openDevice(OpenCLDeviceMetadata device) {
        OpenCLDevice openCLDevice;
        synchronized (this) {
            for (OpenCLDevice openDevice : this.openDevices) {
                if (openDevice.getMetadata().deviceUUID.equals(device.deviceUUID)) {
                    return false;
                }
            }
            openCLDevice = new OpenCLDevice(this, device);
            this.openDevices.add(openCLDevice);
        }
        for (CLServerWorldContext world : this.registeredWorlds) {
            world.addDevice(openCLDevice);
        }
        return true;
    }

    public void closeDevice(OpenCLDeviceMetadata device) {
        OpenCLDevice openCLDevice = null;
        synchronized (this) {
            for (OpenCLDevice openDevice : this.openDevices) {
                if (openDevice.getMetadata().deviceUUID.equals(device.deviceUUID)) {
                    openCLDevice = openDevice;
                    this.openDevices.remove(openDevice);
                    break;
                }
            }
        }
        closeDevice0(openCLDevice);
    }

    private void closeDevice0(OpenCLDevice openCLDevice) {
        if (openCLDevice != null) {
            for (CLServerWorldContext world : this.registeredWorlds) {
                world.removeDevice(openCLDevice);
            }
            openCLDevice.close();
        }
    }

//    public OpenCLDevice.BorrowedCommandQueue borrowCommandQueue() {
//        int index = this.currentDeviceIndex.getAndIncrement();
//        synchronized (this) {
//            int size = this.openDevices.size();
//            if (size == 0) {
//                return null;
//            }
//            return this.openDevices.get(index % size).borrowCommandQueue();
//        }
//    }

    public synchronized void closeAllDevices() {
        for (OpenCLDevice openDevice : this.openDevices) {
            try {
                closeDevice0(openDevice);
                CLUtil.checkCLError(CL12.clUnloadPlatformCompiler(openDevice.getMetadata().platformPtr));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        this.openDevices.clear();
        CL12.clUnloadCompiler();
    }

    void registerWorld(CLServerWorldContext world) {
        this.registeredWorlds.add(world);
        for (OpenCLDevice openDevice : this.openDevices) {
            world.addDevice(openDevice);
        }
    }

    void unregisterWorld(CLServerWorldContext world) {
        this.registeredWorlds.remove(world);
        for (OpenCLDevice openDevice : this.openDevices) {
            world.removeDevice(openDevice);
        }
    }

    void signalNotEmpty() {
        this.takeLock.lock();
        try {
            this.notEmpty.signal();
        } finally {
            this.takeLock.unlock();
        }
    }

    public List<OpenCLDevice> getOpenDevices() {
        return ImmutableList.copyOf(this.openDevices);
    }

}
