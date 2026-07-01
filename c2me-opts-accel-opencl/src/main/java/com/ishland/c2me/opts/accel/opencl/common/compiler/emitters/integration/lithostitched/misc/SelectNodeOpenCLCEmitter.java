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

import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.SelectNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.flowsched.util.Assertions;

import java.util.Arrays;

public class SelectNodeOpenCLCEmitter implements OpenCLCEmitter<SelectNode> {
    public static final SelectNodeOpenCLCEmitter INSTANCE = new SelectNodeOpenCLCEmitter();

    private SelectNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(SelectNode node, OpenCLCGenContext context) {
        Assertions.assertTrue(node.minima.length == node.functions.length);
        Assertions.assertTrue(node.maxima.length == node.functions.length);

        ValuesMethodDefD inputMethod = context.newMethod(node.input);
        ValuesMethodDefD fallbackMethod = context.newMethod(node.fallback);
        ValuesMethodDefD[] delegates = Arrays.stream(node.functions).map(context::newMethod).toArray(ValuesMethodDefD[]::new);

        StringBuilder sb = new StringBuilder();
        sb.append("double v = ").append(context.callDelegate(inputMethod)).append(";\n");
        for (int i = 0; i < delegates.length; i++) {
            if (i == 0) {
                sb.append("if (");
            } else {
                sb.append("else if (");
            }
            sb
                    .append("v >= ").append(OpenCLCGen.literal(node.minima[i]))
                    .append(" && v <= ").append(OpenCLCGen.literal(node.maxima[i]))
                    .append(") {\n")
                    .append("    return ").append(context.callDelegate(delegates[i])).append(";\n")
                    .append("}\n");
        }
        sb.append("return ").append(context.callDelegate(fallbackMethod)).append(";");
        return sb.toString();
    }
}
