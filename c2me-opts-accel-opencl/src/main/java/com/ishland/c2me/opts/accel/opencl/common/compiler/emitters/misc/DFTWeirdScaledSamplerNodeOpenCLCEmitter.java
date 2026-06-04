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

package com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.noise.DFTWeirdScaledSamplerNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

public class DFTWeirdScaledSamplerNodeOpenCLCEmitter implements OpenCLCEmitter<DFTWeirdScaledSamplerNode> {
    public static final DFTWeirdScaledSamplerNodeOpenCLCEmitter INSTANCE = new DFTWeirdScaledSamplerNodeOpenCLCEmitter();

    private DFTWeirdScaledSamplerNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(DFTWeirdScaledSamplerNode node, @UnknownNullability OpenCLCGenContext context) {
        ValuesMethodDefD inputMethod = context.newMethod(node.input);
        StringBuilder builder = new StringBuilder();
        if (node.noise.noise() != null) {
            int offset = context.allocGlobalConstDataObject(node.noise.noise());
            builder.append("double v = ").append(context.callDelegate(inputMethod)).append(";\n");
            switch (node.mapper) {
                case TYPE1 -> builder.append("double d = df_caveScaler_scaleTunnels(v);\n");
                case TYPE2 -> builder.append("double d = df_caveScaler_scaleCaves(v);\n");
            }
            builder.append("global const double_octave_sampler_data_t * restrict data = ptr_shift_global(ctx.const_data, ").append(offset).append(");\n");
            builder.append("return d * fabs(math_noise_perlin_double_octave_sample_global_noinline(data, (double) ctx.x / d, (double) ctx.y / d, (double) ctx.z / d));\n");
        } else {
            builder.append("double v = ").append(context.callDelegate(inputMethod)).append(";\n");
            builder.append("return d * 0.0;\n");
        }
        return builder.toString();
    }
}
