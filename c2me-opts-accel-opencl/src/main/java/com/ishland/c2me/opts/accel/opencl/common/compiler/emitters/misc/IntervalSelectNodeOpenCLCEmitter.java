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
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.util.TreeUtils;
import com.ishland.flowsched.util.Assertions;

public class IntervalSelectNodeOpenCLCEmitter implements OpenCLCEmitter<IntervalSelectNode> {
    public static final IntervalSelectNodeOpenCLCEmitter INSTANCE = new IntervalSelectNodeOpenCLCEmitter();

    private IntervalSelectNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(IntervalSelectNode node, OpenCLCGenFunctionContext context, String storeTo) {
        StringBuilder sb = new StringBuilder();

        ValuesMethodDefF64 inputMethod = context.newVarF64(node.input);
        AstNode[] delegates = node.functions.clone();

        for (AstNode subtree : TreeUtils.findLargestCommonSubtrees(delegates)) {
            context.newVar(subtree);
        }

        sb.append("double v = ").append(context.getDelegateVar(inputMethod)).append(";\n");
        sb.append("double res;\n");
        sb.append(genBinarySearch(node.thresholds, delegates, context, 0, node.thresholds.length));
        sb.append(storeTo).append(" = res;\n");
        return sb.toString();
    }

    private static String genBinarySearch(double[] thresholds, AstNode[] delegates, OpenCLCGenFunctionContext context, int fromIndex, int toIndex) {
        Assertions.assertTrue(fromIndex < toIndex);

        int mid = (fromIndex + toIndex - 1) >>> 1;
        double midVal = thresholds[mid];

        StringBuilder sb = new StringBuilder();
        sb.append("if (v < ").append(OpenCLCGen.literal(midVal)).append(") {\n");

        if (fromIndex == mid) {
            OpenCLCGenFunctionContext forked = context.fork();
            ValuesMethodDefF64 newVar = forked.newVarF64(delegates[fromIndex]);
            sb.append(forked.getBody().indent(4));
            sb.append("    ").append("res = ").append(forked.getDelegateVar(newVar)).append(";\n");
            delegates[fromIndex] = null;
        } else {
            sb.append(genBinarySearch(thresholds, delegates, context, fromIndex, mid).indent(4));
        }

        sb.append("} else {\n");

        if (mid + 1 == toIndex) {
            OpenCLCGenFunctionContext forked = context.fork();
            ValuesMethodDefF64 newVar = forked.newVarF64(delegates[toIndex]);
            sb.append(forked.getBody().indent(4));
            sb.append("    ").append("res = ").append(forked.getDelegateVar(newVar)).append(";\n");
            delegates[toIndex] = null;
        } else {
            sb.append(genBinarySearch(thresholds, delegates, context, mid + 1, toIndex).indent(4));
        }

        sb.append("}\n");

        return sb.toString();
    }
}
