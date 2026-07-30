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

import com.ishland.c2me.opts.dfc.common.ast.misc.InterpolatedNoiseSamplerNode;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

public class InterpolatedNoiseSamplerNodeOpenCLCEmitter implements OpenCLCEmitter<InterpolatedNoiseSamplerNode> {
    public static final InterpolatedNoiseSamplerNodeOpenCLCEmitter INSTANCE = new InterpolatedNoiseSamplerNodeOpenCLCEmitter();

    private InterpolatedNoiseSamplerNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(InterpolatedNoiseSamplerNode node, @UnknownNullability OpenCLCGenContext context) {
        int offset = context.allocGlobalConstDataObject(node.sampler);
        return "global const interpolated_noise_sampler_t * restrict data = ptr_shift_global(ctx.const_data, " + offset + ");\n" +
                "return math_noise_perlin_interpolated_sample_global_noinline(data, ctx.x, ctx.y, ctx.z);\n";
    }
}
