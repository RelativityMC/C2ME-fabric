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

import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

public class GenericShiftedNoiseNodeOpenCLCEmitter implements OpenCLCEmitter<GenericShiftedNoiseNode> {
    public static final GenericShiftedNoiseNodeOpenCLCEmitter INSTANCE = new GenericShiftedNoiseNodeOpenCLCEmitter();

    private GenericShiftedNoiseNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(GenericShiftedNoiseNode node, OpenCLCGenFunctionContext context, String storeTo) {
        if (node.noise.noise() == null) {
            return storeTo + " = 0.0;\n";
        }

        ValuesMethodDefD inputXMethod = context.newVar(node.inputX);
        ValuesMethodDefD inputYMethod = context.newVar(node.inputY);
        ValuesMethodDefD inputZMethod = context.newVar(node.inputZ);
        int offset = context.getGlobalContext().allocGlobalConstDataObject(node.noise.noise());
        return "global const double_octave_sampler_data_t * restrict data = ptr_shift_global(ctx.const_data, " + offset + ");\n" +
                storeTo + " = math_noise_perlin_double_octave_sample_global_noinline(data, " +
                context.getDelegateVar(inputXMethod) + "," +
                context.getDelegateVar(inputYMethod) + "," +
                context.getDelegateVar(inputZMethod) + ");\n";
    }
}
