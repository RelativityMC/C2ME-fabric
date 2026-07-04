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

package com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.integration.lithostitched.misc;

import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.ShiftNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

public class ShiftNodeOpenCLCEmitter implements OpenCLCEmitter<ShiftNode> {
    public static final ShiftNodeOpenCLCEmitter INSTANCE = new ShiftNodeOpenCLCEmitter();

    private ShiftNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(ShiftNode node, @UnknownNullability OpenCLCGenContext context) {
        ValuesMethodDefD input = context.newMethod(node.input);
        ValuesMethodDefD inputX = context.newMethod(node.inputX);
        ValuesMethodDefD inputY = context.newMethod(node.inputY);
        ValuesMethodDefD inputZ = context.newMethod(node.inputZ);

        return "double shiftX = " + context.callDelegate(inputX) + ";\n"
                + "double shiftY = " + context.callDelegate(inputY) + ";\n"
                + "double shiftZ = " + context.callDelegate(inputZ) + ";\n"
                + "sample_int32_ctx_t shiftCtx = make_sample_int32_ctx(ctx.const_data, ctx.rw_data, shiftX, shiftY, shiftZ, ctx.sample_flags);\n"
                + "return " + input.generatedMethod() + "(shiftCtx);\n";
    }
}
