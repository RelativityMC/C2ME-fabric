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

import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

public class FindTopSurfaceNodeOpenCLCEmitter implements OpenCLCEmitter<FindTopSurfaceNode> {
    public static final FindTopSurfaceNodeOpenCLCEmitter INSTANCE = new FindTopSurfaceNodeOpenCLCEmitter();

    private FindTopSurfaceNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(FindTopSurfaceNode node, @UnknownNullability OpenCLCGenContext context) {
        ValuesMethodDefD densityMethod = context.newMethod(node.density);
        ValuesMethodDefD upperBoundMethod = context.newMethod(node.upperBound);
        ValuesMethodDefD lowerBoundMethod = context.newMethod(node.lowerBound);

        StringBuilder b = new StringBuilder();
        b.append("int32_t topCellBlockY = ((int32_t) floor(").append(context.callDelegate(upperBoundMethod)).append(" / ").append(node.cellHeight).append(")) * ").append(node.cellHeight).append(";\n");
        b.append("int32_t lowerBoundEval = (int32_t) ").append(context.callDelegate(lowerBoundMethod)).append(";\n");
        b.append("for (int32_t y1 = topCellBlockY; y1 > lowerBoundEval; y1 -= ").append(node.cellHeight).append(") {\n");

        b.append("    if (");
        if (densityMethod.isConst()) {
            b.append(OpenCLCGen.literal(densityMethod.constValue()));
        } else {
            b.append(densityMethod.generatedMethod()).append("(make_sample_int32_ctx(ctx.const_data, ctx.rw_data, ctx.x, y1, ctx.z, 0))");
        }
        b.append(" > 0.0) {\n");

        b.append("        return (double) y1;\n");
        b.append("    }\n");
        b.append("}\n");
        b.append("return (double) lowerBoundEval;\n");

        return b.toString();
    }
}
