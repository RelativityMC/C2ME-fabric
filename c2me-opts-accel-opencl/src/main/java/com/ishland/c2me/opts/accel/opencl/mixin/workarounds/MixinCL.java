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

package com.ishland.c2me.opts.accel.opencl.mixin.workarounds;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = org.lwjgl.opencl.CL.class, remap = false)
public class MixinCL {

    @ModifyArg(method = "createPlatformCapabilities", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opencl/CL10;nclGetDeviceIDs(JJIJJ)I"), index = 1, require = 2)
    private static long modifyDeviceType(long device_type) {
        if (device_type == 0xffffffffffffffffL) {
            // this is a workaround for java sign-extending the device type
            return 0xffffffffL;
        }
        return device_type;
    }

}
