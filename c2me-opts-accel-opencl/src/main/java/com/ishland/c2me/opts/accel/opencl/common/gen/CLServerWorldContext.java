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

import com.ishland.c2me.opts.accel.opencl.common.gen.cache.Stage1Cache;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.opencl.common.compiler.GeneratedCLSource;
import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CLServerWorldContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLServerWorldContext.class);

    private final ArrayList<DeviceWithProgram> openDevices = new ArrayList<>();
    private final ReferenceOpenHashSet<Pair<OpenCLDevice, CompletableFuture<Void>>> pendingCompilations = new ReferenceOpenHashSet<>();

    private final Stage1Cache stage1Cache;

    private final CLServerGlobalContext globalContext;
    private final String description;
    private final GeneratedCLSource generatedCLSource;
    private final GenerationShapeConfig generationShapeConfig;
    private final CLBlockStateMappings clBlockStateMappings;

    public CLServerWorldContext(CLServerGlobalContext globalContext, String description, GeneratedCLSource generatedCLSource, GenerationShapeConfig generationShapeConfig, CLBlockStateMappings clBlockStateMappings) {
        this.globalContext = globalContext;
        this.description = description;
        this.generatedCLSource = Objects.requireNonNull(generatedCLSource);
        this.generationShapeConfig = Objects.requireNonNull(generationShapeConfig);
        this.clBlockStateMappings = Objects.requireNonNull(clBlockStateMappings);
        this.stage1Cache = new Stage1Cache(this);

        this.globalContext.registerWorld(this);
    }

    public void addDevice(OpenCLDevice device) {
        synchronized (this.pendingCompilations) {
            for (Pair<OpenCLDevice, CompletableFuture<Void>> pendingCompilation : this.pendingCompilations) {
                if (pendingCompilation.left() == device) {
                    return;
                }
            }
            LOGGER.info("Compiling program for {} for device {}", this.description, device);
            CompletableFuture<Void> future = device.compileProgramAsync(this.description, this.generatedCLSource).thenAccept(program -> {
                long clBuffer;
                ByteBuffer byteBuffer = MemoryUtil.memAlloc(this.generatedCLSource.getConstData().length);
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    byteBuffer.put(this.generatedCLSource.getConstData());
                    byteBuffer.rewind();
                    IntBuffer errorCodeRet = stack.callocInt(1);
                    clBuffer = CL12.clCreateBuffer(device.getContext(), CL12.CL_MEM_READ_ONLY | CL12.CL_MEM_COPY_HOST_PTR, byteBuffer, errorCodeRet);
                } finally {
                    MemoryUtil.memFree(byteBuffer);
                }
                synchronized (this.openDevices) {
                    this.openDevices.add(new DeviceWithProgram(device, program, clBuffer));
                }
                LOGGER.info("Compiled program for {} for device {}", this.description, device);
            }).exceptionally(throwable -> {
                LOGGER.error("Failed to compile program for device {}", device, throwable);
                return null;
            });
            Pair<OpenCLDevice, CompletableFuture<Void>> pair = Pair.of(device, future);
            this.pendingCompilations.add(pair);
            future.handle((_, _) -> {
                try {
                    boolean releaseProgram = false;
                    synchronized (this.pendingCompilations) {
                        if (!this.pendingCompilations.remove(pair)) {
                            // cancelled, release program
                            releaseProgram = true;
                        }
                    }
                    this.globalContext.signalNotEmpty();
                    if (releaseProgram) {
                        this.removeDevice(device);
                    }
                } catch (Throwable t) {
                    LOGGER.error("Failed to remove pending compilation", t);
                }
                return null;
            });
        }
    }

    public void removeDevice(OpenCLDevice device) {
        synchronized (this.pendingCompilations) {
            for (Pair<OpenCLDevice, CompletableFuture<Void>> pendingCompilation : this.pendingCompilations) {
                if (pendingCompilation.left() == device) {
                    this.pendingCompilations.remove(pendingCompilation);
                    return;
                }
            }
        }
        synchronized (this.openDevices) {
            for (DeviceWithProgram deviceWithProgram : this.openDevices) {
                if (deviceWithProgram.device() == device) {
                    removeDevice0(deviceWithProgram);
                    this.openDevices.remove(deviceWithProgram);
                    return;
                }
            }
        }
    }

    private void removeDevice0(DeviceWithProgram deviceWithProgram) {
        for (long program : deviceWithProgram.program()) {
            if (program != 0L) {
                try {
                    CLUtil.checkCLError(CL12.clReleaseProgram(program));
                } catch (Throwable t) {
                    LOGGER.error("Failed to release program for device {}", deviceWithProgram.device(), t);
                }
            }
        }
        try {
            CLUtil.checkCLError(CL12.clReleaseMemObject(deviceWithProgram.programConstDataBuffer()));
        } catch (Throwable t) {
            LOGGER.error("Failed to release buffer for device {}", deviceWithProgram.device(), t);
        }
        LOGGER.info("Released program for {} for device {}", this.description, deviceWithProgram.device());
    }

    public Pair<OpenCLDevice.BorrowedCommandQueue, DeviceWithProgram> tryBorrowCommandQueue() {
        synchronized (this.openDevices) {
            int size = this.openDevices.size();
            if (size == 0) {
                return null;
            }
            DeviceWithProgram leastTask = this.openDevices.getFirst();
            for (int i = 1; i < size; i++) {
                DeviceWithProgram current = this.openDevices.get(i);
                if (current.device.getPermits() > leastTask.device.getPermits()) {
                    leastTask = current;
                }
            }
            OpenCLDevice.BorrowedCommandQueue borrowed = leastTask.device().borrowCommandQueue();
            if (borrowed != null) {
                return Pair.of(borrowed, leastTask);
            }
            return null;
        }
    }

    public CompletableFuture<Pair<OpenCLDevice.BorrowedCommandQueue, DeviceWithProgram>> borrowCommandQueue() {
        CompletableFuture<Pair<OpenCLDevice.BorrowedCommandQueue, DeviceWithProgram>> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                this.globalContext.takeLock.lock();
                try {
                    while (true) {
                        Pair<OpenCLDevice.BorrowedCommandQueue, DeviceWithProgram> borrowed = this.tryBorrowCommandQueue();
                        if (borrowed != null) {
                            future.complete(borrowed);
                            return;
                        } else {
                            this.globalContext.notEmpty.await();
                        }
                    }
                } finally {
                    this.globalContext.takeLock.unlock();
                }
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public void releaseAllDevices() {
        synchronized (this.pendingCompilations) {
            this.pendingCompilations.clear();
        }
        synchronized (this.openDevices) {
            for (DeviceWithProgram deviceWithProgram : this.openDevices) {
                removeDevice0(deviceWithProgram);
            }
            this.openDevices.clear();
        }
        this.globalContext.unregisterWorld(this);
    }

    public GenerationShapeConfig getGenerationShapeConfig() {
        return this.generationShapeConfig;
    }

    public Stage1Cache getEstimateSurfaceHeightCache() {
        return this.stage1Cache;
    }

    public GeneratedCLSource getGeneratedCLSource() {
        return this.generatedCLSource;
    }

    public CLBlockStateMappings getClBlockStateMappings() {
        return this.clBlockStateMappings;
    }

    public record DeviceWithProgram(OpenCLDevice device, long[] program, long programConstDataBuffer) {

        public DeviceWithProgram(OpenCLDevice device, EnumMap<OpenCLCGen.ProgramType, Long> program, long programConstDataBuffer) {
            this(device, enumMap2Array(program), programConstDataBuffer);
        }

        public long getProgram(OpenCLCGen.ProgramType type) {
            long l = this.program[type.ordinal()];
            if (l == 0L) {
                throw new IllegalStateException("Program for type " + type + " is not available");
            }
            return l;
        }

        private static long[] enumMap2Array(EnumMap<OpenCLCGen.ProgramType, Long> program) {
            long[] array = new long[OpenCLCGen.ProgramType.values().length];
            for (OpenCLCGen.ProgramType type : OpenCLCGen.ProgramType.values()) {
                array[type.ordinal()] = program.getOrDefault(type, 0L);
            }
            return array;
        }

    }

}
