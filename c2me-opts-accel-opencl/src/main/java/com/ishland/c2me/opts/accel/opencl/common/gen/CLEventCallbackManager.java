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

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLEventCallback;
import org.lwjgl.opencl.CLEventCallbackI;
import org.lwjgl.system.NativeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

public class CLEventCallbackManager implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLEventCallbackManager.class);

    private final Long2ReferenceOpenHashMap<CLEventCallbackI> callbacks = new Long2ReferenceOpenHashMap<>();
    private final CLEventCallback instance = CLEventCallback.create(this::invokeInternal);

    private long ordinal = 0;
    private boolean open = true;

    public void registerCallback(@NativeType("cl_event") long event, @NativeType("cl_int") int command_exec_callback_type, @NativeType("void (*) (cl_event, cl_int, void *)") CLEventCallbackI pfn_notify) {
        long user_data;
        synchronized (this) {
            if (!this.open) {
                throw new IllegalStateException("CLEventCallbackManager is closed");
            }
            user_data = this.ordinal++;
            this.callbacks.put(user_data, pfn_notify);
        }
        CL12.clSetEventCallback(event, command_exec_callback_type, this.instance, user_data);
    }

    private void invokeInternal(@NativeType("cl_event") long event, @NativeType("cl_int") int event_command_exec_status, @NativeType("void *") long user_data) {
        CLEventCallbackI callbackI;
        synchronized (this) {
            callbackI = this.callbacks.remove(user_data);
            if (!this.open && callbackI != null && this.callbacks.isEmpty()) {
                this.instance.close();
            }
        }

        if (callbackI == null) {
            LOGGER.error("Dangling callback ID {}, invoked by driver twice?", user_data);
            return;
        }
        try {
            callbackI.invoke(event, event_command_exec_status, user_data);
        } catch (Throwable t) {
            LOGGER.error("Error in callback ID {}", user_data, t);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (this.open && this.callbacks.isEmpty()) {
                this.instance.close();
            }
            this.open = false;
        }
    }
}
