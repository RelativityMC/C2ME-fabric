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

package com.ishland.c2me.opts.accel.opencl.mixin;

import com.ishland.c2me.opts.accel.opencl.common.Config;
import com.ishland.c2me.opts.accel.opencl.common.ducks.MinecraftServerExtension;
import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceLocator;
import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLServerGlobalContext;
import com.ishland.c2me.opts.accel.opencl.common.progress.GlobalProgressStash;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MinecraftServerExtension {

    @Shadow @Final private static Logger LOGGER;

    @Unique
    private CLServerGlobalContext c2me$clContext;

    @Inject(method = "runServer", at = @At("HEAD"))
    private void preRunServer(CallbackInfo ci) {
        try {
            if (this.c2me$clContext != null) {
                throw new IllegalStateException("Context already exists?");
            }
            this.c2me$clContext = new CLServerGlobalContext();
            List<OpenCLDeviceMetadata> metadataList = OpenCLDeviceLocator.enumerateAll();
            boolean openedAnyDevice = false;
            for (OpenCLDeviceMetadata openCLDeviceMetadata : metadataList) {
                if (!Config.deviceUUIDWhitelist.isEmpty() && !Config.deviceUUIDWhitelist.contains(openCLDeviceMetadata.deviceUUID)) {
                    LOGGER.info("Skipping OpenCL device {} since it's not in the whitelist", openCLDeviceMetadata.deviceUUID);
                    continue;
                }
                if (Config.deviceUUIDBlacklist.contains(openCLDeviceMetadata.deviceUUID)) {
                    LOGGER.info("Skipping OpenCL device {} since it's in the blacklist", openCLDeviceMetadata.deviceUUID);
                    continue;
                }
                this.c2me$clContext.openDevice(openCLDeviceMetadata);
                openedAnyDevice = true;
            }
            if (!openedAnyDevice) {
                LOGGER.warn("No OpenCL devices found");
                if (!Config.allowIncompatibilityFallback) {
                    throw new IllegalStateException("No OpenCL devices found");
                }
                return;
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize OpenCL context", t);
            this.c2me$clContext = null;
            if (!Config.allowIncompatibilityFallback) {
                GlobalProgressStash.PROGRESS_TEXT = String.format("Failed to initialize OpenCL context, see logs for details: %s", t);
                throw t;
            }
        }
    }

    @Inject(method = "shutdown", at = @At("RETURN"))
    private void postStopServer(CallbackInfo ci) {
        try {
            this.c2me$clContext.closeAllDevices();
            this.c2me$clContext = null;
        } catch (Throwable t) {
            LOGGER.error("Failed to release OpenCL context", t);
        }
    }

    @Override
    public CLServerGlobalContext c2me$getCLContext() {
        return this.c2me$clContext;
    }
}
