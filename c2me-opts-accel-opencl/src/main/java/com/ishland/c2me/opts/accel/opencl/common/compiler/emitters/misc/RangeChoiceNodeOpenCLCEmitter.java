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

import com.ishland.c2me.opts.accel.opencl.common.Config;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.util.TreeUtils;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class RangeChoiceNodeOpenCLCEmitter implements OpenCLCEmitter<RangeChoiceNode> {
    public static final RangeChoiceNodeOpenCLCEmitter INSTANCE = new RangeChoiceNodeOpenCLCEmitter();

    private RangeChoiceNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(RangeChoiceNode node, OpenCLCGenFunctionContext context, String storeTo) {
        StringBuilder sb = new StringBuilder();

        if (!Config.preserveAllControlFlows) {
            for (AstNode subtree : TreeUtils.findLargestCommonSubtrees(node.whenInRange, node.whenOutOfRange)) {
                context.newVar(subtree);
            }
        }

        ValuesMethodDefF64 input = context.newVarF64(node.input);
        sb.append("double v = ").append(context.getDelegateVar(input)).append(";\n");

        sb.append("if (v >= ").append(literal(node.minInclusive)).append(" && v < ").append(literal(node.maxExclusive)).append(") {\n");
        emitCall(context, node.whenInRange, sb, storeTo);
        sb.append("} else {\n");
        emitCall(context, node.whenOutOfRange, sb, storeTo);
        sb.append("}\n");

        return sb.toString();
    }

    private static void emitCall(OpenCLCGenFunctionContext context, AstNode node, StringBuilder sb, String storeTo) {
        if (!Config.preserveAllControlFlows) {
            if (TreeUtils.hasNonTrivialChildrenUntilBranchIgnoringMul(node)) {
                OpenCLCGenFunctionContext forked = context.fork();
                ValuesMethodDefF64 whenInRange = forked.newVarF64(node);
                sb.append(forked.getBody().indent(4));
                sb.append("    ").append(storeTo).append(" = ").append(forked.getDelegateVar(whenInRange)).append(";\n");
            } else {
                ValuesMethodDefF64 whenInRange = context.newVarF64(node);
                sb.append("    ").append(storeTo).append(" = ").append(context.getDelegateVar(whenInRange)).append(";\n");
            }
        } else {
            ValuesMethodDefF64 whenInRange = context.getGlobalContext().newMethodF64(node, context.getVariant());
            sb.append("    ").append(storeTo).append(" = ").append(context.getGlobalContext().callDelegate(whenInRange)).append(";\n");
        }
    }
}
