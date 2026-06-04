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

import com.ishland.c2me.opts.accel.opencl.ModuleEntryPoint;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLDataUtil;
import com.ishland.c2me.opts.accel.opencl.common.compiler.GeneratedCLSource;
import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.io.InputStream;

@Mixin(OpenCLCGen.ContextImpl.class)
public class MixinOpenCLGenContext {

    @ModifyReturnValue(method = "build", at = @At("RETURN"), remap = false)
    private static GeneratedCLSource modifySource(GeneratedCLSource original) {
        try (InputStream in = ModuleEntryPoint.class.getClassLoader().getResourceAsStream("clsources/c2me_opencl_ext_math.cl")) {
            if (in == null) throw new NullPointerException("Resource not found");
            String header = new String(in.readAllBytes());
            return new GeneratedCLSource(
                    original.getOrdinal(),
                    header + original.getGeneratedSource(),
                    original.getConstData(),
                    CLDataUtil.transformGlobalDynamicDataOffsets(original.getGlobalDynamicDataOffsets()),
                    original.getFlatCachePrefills(),
                    original.getCache2dPrefills(),
                    original.getInterpolatorPrefills(),
                    original.getDefines(),
                    original.getBiomeMappings(),
                    original.getDumpedPath()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
