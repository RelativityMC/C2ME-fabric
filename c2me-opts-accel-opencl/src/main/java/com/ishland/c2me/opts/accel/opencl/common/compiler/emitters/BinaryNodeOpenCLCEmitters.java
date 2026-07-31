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
import com.ishland.c2me.opts.dfc.common.ast.binary.AbstractBinaryNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.DivNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

public class BinaryNodeOpenCLCEmitters {

    public static abstract class AbstractGenericBinaryNodeOpenCLCEmitter<T extends AbstractBinaryNode> implements OpenCLCEmitter<T> {

        @Override
        public String doCLGen(T node, OpenCLCGenFunctionContext context, String storeTo) {
            StringBuilder sb = new StringBuilder();
            ValuesMethodDefD leftMethod = context.newVar(node.left);
            ValuesMethodDefD rightMethod = context.newVar(node.right);
            genBody(node, context, storeTo, sb, leftMethod, rightMethod);
            return sb.toString();
        }

        public abstract void genBody(T node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right);

    }

    public static class AddNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<AddNode> {
        public static final AddNodeEmitter INSTANCE = new AddNodeEmitter();

        private AddNodeEmitter() {
        }

        @Override
        public void genBody(AddNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append(storeTo).append(" = ").append(context.getDelegateVar(left)).append(" + ").append(context.getDelegateVar(right)).append(";\n");
        }
    }

    public static class DivNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<DivNode> {
        public static final DivNodeEmitter INSTANCE = new DivNodeEmitter();

        private DivNodeEmitter() {
        }

        @Override
        public void genBody(DivNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append(storeTo).append(" = ").append(context.getDelegateVar(left)).append(" / ").append(context.getDelegateVar(right)).append(";\n");
        }
    }

    public static class MaxNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MaxNode> {
        public static final MaxNodeEmitter INSTANCE = new MaxNodeEmitter();

        private MaxNodeEmitter() {
        }

        @Override
        public void genBody(MaxNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append(storeTo).append(" = fmax(").append(context.getDelegateVar(left)).append(", ").append(context.getDelegateVar(right)).append(");\n");
        }
    }

    public static class MaxShortNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MaxShortNode> {
        public static final MaxShortNodeEmitter INSTANCE = new MaxShortNodeEmitter();

        private MaxShortNodeEmitter() {
        }

        @Override
        public String doCLGen(MaxShortNode node, OpenCLCGenFunctionContext context, String storeTo) {
            StringBuilder sb = new StringBuilder();
            ValuesMethodDefD leftMethod = context.newVar(node.left);

            sb.append("const double _left = ").append(context.getDelegateVar(leftMethod)).append(";\n");
            sb.append("if (_left >= ").append(OpenCLCGen.literal(node.rightMax)).append(") {\n");
            sb.append("    ").append(storeTo).append(" = _left;\n");
            sb.append("} else {\n");

            OpenCLCGenFunctionContext forked = context.fork();
            ValuesMethodDefD rightMethod = forked.newVar(node.right);
            sb.append(forked.getBody().indent(4));
            sb.append("    ").append(storeTo).append(" = fmax(_left, ").append(forked.getDelegateVar(rightMethod)).append(");\n");

            sb.append("}\n");
            return sb.toString();
        }

        @Override
        public void genBody(MaxShortNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MinNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MinNode> {
        public static final MinNodeEmitter INSTANCE = new MinNodeEmitter();

        private MinNodeEmitter() {
        }

        @Override
        public void genBody(MinNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append(storeTo).append(" = fmin(").append(context.getDelegateVar(left)).append(", ").append(context.getDelegateVar(right)).append(");\n");
        }
    }

    public static class MinShortNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MinShortNode> {
        public static final MinShortNodeEmitter INSTANCE = new MinShortNodeEmitter();

        private MinShortNodeEmitter() {
        }

        @Override
        public String doCLGen(MinShortNode node, OpenCLCGenFunctionContext context, String storeTo) {
            StringBuilder sb = new StringBuilder();
            ValuesMethodDefD leftMethod = context.newVar(node.left);

            sb.append("const double _left = ").append(context.getDelegateVar(leftMethod)).append(";\n");
            sb.append("if (_left <= ").append(OpenCLCGen.literal(node.rightMin)).append(") {\n");
            sb.append("    ").append(storeTo).append(" = _left;\n");
            sb.append("} else {\n");

            OpenCLCGenFunctionContext forked = context.fork();
            ValuesMethodDefD rightMethod = forked.newVar(node.right);
            sb.append(forked.getBody().indent(4));
            sb.append("    ").append(storeTo).append(" = fmin(_left, ").append(forked.getDelegateVar(rightMethod)).append(");\n");

            sb.append("}\n");
            return sb.toString();
        }

        @Override
        public void genBody(MinShortNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MulNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MulNode> {
        public static final MulNodeEmitter INSTANCE = new MulNodeEmitter();

        private MulNodeEmitter() {
        }

        @Override
        public String doCLGen(MulNode node, OpenCLCGenFunctionContext context, String storeTo) {
            StringBuilder sb = new StringBuilder();
            if (node.left instanceof ConstantNode) { // (0.0 * x) should already be optimized out
                ValuesMethodDefD leftMethod = context.newVar(node.left);
                ValuesMethodDefD rightMethod = context.newVar(node.right);
                sb.append(storeTo).append(" = ").append(context.getDelegateVar(leftMethod)).append(" * ").append(context.getDelegateVar(rightMethod)).append(";\n");
            } else {
                ValuesMethodDefD leftMethod = context.newVar(node.left);
                sb.append("const double _left = ").append(context.getDelegateVar(leftMethod)).append(";\n");

                sb.append("if (_left == 0.0) {\n");
                sb.append("    ").append(storeTo).append(" = 0.0;\n");
                sb.append("} else {\n");

                OpenCLCGenFunctionContext forked = context.fork();
                ValuesMethodDefD rightMethod = forked.newVar(node.right);
                sb.append(forked.getBody().indent(4));
                sb.append("    ").append(storeTo).append(" = _left * ").append(forked.getDelegateVar(rightMethod)).append(";\n");

                sb.append("}\n");
            }
            return sb.toString();
        }

        @Override
        public void genBody(MulNode node, OpenCLCGenFunctionContext context, String storeTo, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            throw new UnsupportedOperationException();
        }
    }

    public static void register(CodeGenRegistry<OpenCLCEmitter<? extends AstNode>> registry) {
        registry.registerExactMatch(AddNode.class, AddNodeEmitter.INSTANCE);
        registry.registerExactMatch(DivNode.class, DivNodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxNode.class, MaxNodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxShortNode.class, MaxShortNodeEmitter.INSTANCE);
        registry.registerExactMatch(MinNode.class, MinNodeEmitter.INSTANCE);
        registry.registerExactMatch(MinShortNode.class, MinShortNodeEmitter.INSTANCE);
        registry.registerExactMatch(MulNode.class, MulNodeEmitter.INSTANCE);
    }

}
