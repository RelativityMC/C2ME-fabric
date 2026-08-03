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

package com.ishland.c2me.opts.accel.opencl.common.compiler.emitters;

import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbstractUnaryNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SquareNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqueezeNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

public class UnaryNodeOpenCLCEmitters {

    public static abstract class AbstractGenericUnaryNodeOpenCLCEmitter<T extends AbstractUnaryNode> implements OpenCLCEmitter<T> {

        @Override
        public String doCLGen(T node, OpenCLCGenFunctionContext context, String storeTo) {
            StringBuilder sb = new StringBuilder();
            ValuesMethodDefF64 operand = context.newVarF64(node.operand);
            genBody(node, context, storeTo, sb, operand);
            return sb.toString();
        }

        protected abstract void genBody(T node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefF64 operand);

    }

    public static class AbsNodeEmitter extends AbstractGenericUnaryNodeOpenCLCEmitter<AbsNode> {
        public static final AbsNodeEmitter INSTANCE = new AbsNodeEmitter();

        private AbsNodeEmitter() {
        }

        @Override
        protected void genBody(AbsNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefF64 operand) {
            sb.append(storeTo).append(" = fabs(").append(context.getDelegateVar(operand)).append(");\n");
        }
    }

    public static class CubeNodeEmitter extends AbstractGenericUnaryNodeOpenCLCEmitter<CubeNode> {
        public static final CubeNodeEmitter INSTANCE = new CubeNodeEmitter();

        private CubeNodeEmitter() {
        }

        @Override
        protected void genBody(CubeNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefF64 operand) {
            sb
                    .append("double v = ").append(context.getDelegateVar(operand)).append(";\n")
                    .append(storeTo).append(" = v * v * v;\n");
        }
    }

    public static class NegMulNodeEmitter extends AbstractGenericUnaryNodeOpenCLCEmitter<NegMulNode> {
        public static final NegMulNodeEmitter INSTANCE = new NegMulNodeEmitter();

        private NegMulNodeEmitter() {
        }

        @Override
        protected void genBody(NegMulNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefF64 operand) {
            sb
                    .append("double v = ").append(context.getDelegateVar(operand)).append(";\n")
                    .append(storeTo).append(" = v > 0.0 ? v : v * ").append(OpenCLCGen.literal(node.negMul)).append(";\n");
        }
    }

    public static class SquareNodeEmitter extends AbstractGenericUnaryNodeOpenCLCEmitter<SquareNode> {
        public static final SquareNodeEmitter INSTANCE = new SquareNodeEmitter();

        private SquareNodeEmitter() {
        }

        @Override
        protected void genBody(SquareNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefF64 operand) {
            sb
                    .append("double v = ").append(context.getDelegateVar(operand)).append(";\n")
                    .append(storeTo).append(" = v * v;\n");
        }
    }

    public static class SqueezeNodeEmitter extends AbstractGenericUnaryNodeOpenCLCEmitter<SqueezeNode> {
        public static final SqueezeNodeEmitter INSTANCE = new SqueezeNodeEmitter();

        private SqueezeNodeEmitter() {
        }

        @Override
        protected void genBody(SqueezeNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefF64 operand) {
            sb
                    .append("double v = clamp(").append(context.getDelegateVar(operand)).append(", -1.0, 1.0);\n")
                    .append(storeTo).append(" = v / 2.0 - v * v * v / 24.0;\n");
        }
    }

    public static void register(CodeGenRegistry<OpenCLCEmitter<? extends AstNode>> registry) {
        registry.registerExactMatch(AbsNode.class, AbsNodeEmitter.INSTANCE);
        registry.registerExactMatch(CubeNode.class, CubeNodeEmitter.INSTANCE);
        registry.registerExactMatch(NegMulNode.class, NegMulNodeEmitter.INSTANCE);
        registry.registerExactMatch(SquareNode.class, SquareNodeEmitter.INSTANCE);
        registry.registerExactMatch(SqueezeNode.class, SqueezeNodeEmitter.INSTANCE);
    }

}
