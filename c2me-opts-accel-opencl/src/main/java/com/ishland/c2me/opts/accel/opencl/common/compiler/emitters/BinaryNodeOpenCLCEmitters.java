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
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

public class BinaryNodeOpenCLCEmitters {

    public static abstract class AbstractGenericBinaryNodeOpenCLCEmitter<T extends AbstractBinaryNode> implements OpenCLCEmitter<T> {

        @Override
        public final String doCLGen(T node, @UnknownNullability OpenCLCGenContext context) {
            StringBuilder sb = new StringBuilder();
            ValuesMethodDefD leftMethod = context.newMethod(node.left);
            ValuesMethodDefD rightMethod = context.newMethod(node.right);
            genBody(node, context, sb, leftMethod, rightMethod);
            return sb.toString();
        }

        public abstract void genBody(T node, OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right);

    }

    public static class AddNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<AddNode> {
        public static final AddNodeEmitter INSTANCE = new AddNodeEmitter();

        private AddNodeEmitter() {
        }

        @Override
        public void genBody(AddNode node, @UnknownNullability OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append("return ").append(context.callDelegate(left)).append(" + ").append(context.callDelegate(right)).append(";\n");
        }
    }

    public static class DivNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<DivNode> {
        public static final DivNodeEmitter INSTANCE = new DivNodeEmitter();

        private DivNodeEmitter() {
        }

        @Override
        public void genBody(DivNode node, @UnknownNullability OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append("return ").append(context.callDelegate(left)).append(" / ").append(context.callDelegate(right)).append(";\n");
        }
    }

    public static class MaxNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MaxNode> {
        public static final MaxNodeEmitter INSTANCE = new MaxNodeEmitter();

        private MaxNodeEmitter() {
        }

        @Override
        public void genBody(MaxNode node, @UnknownNullability OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append("return fmax(").append(context.callDelegate(left)).append(", ").append(context.callDelegate(right)).append(");\n");
        }
    }

    public static class MaxShortNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MaxShortNode> {
        public static final MaxShortNodeEmitter INSTANCE = new MaxShortNodeEmitter();

        private MaxShortNodeEmitter() {
        }

        @Override
        public void genBody(MaxShortNode node, @UnknownNullability OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append("const double _left = ").append(context.callDelegate(left)).append(";\n");
            sb.append("return _left >= ").append(OpenCLCGen.literal(node.rightMax))
                    .append(" ? _left : fmax(_left, ").append(context.callDelegate(right)).append(");\n");
        }
    }

    public static class MinNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MinNode> {
        public static final MinNodeEmitter INSTANCE = new MinNodeEmitter();

        private MinNodeEmitter() {
        }

        @Override
        public void genBody(MinNode node, @UnknownNullability OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append("return fmin(").append(context.callDelegate(left)).append(", ").append(context.callDelegate(right)).append(");\n");
        }
    }

    public static class MinShortNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MinShortNode> {
        public static final MinShortNodeEmitter INSTANCE = new MinShortNodeEmitter();

        private MinShortNodeEmitter() {
        }

        @Override
        public void genBody(MinShortNode node, @UnknownNullability OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            sb.append("const double _left = ").append(context.callDelegate(left)).append(";\n");
            sb.append("return _left <= ").append(OpenCLCGen.literal(node.rightMin))
                    .append(" ? _left : fmin(_left, ").append(context.callDelegate(right)).append(");\n");
        }
    }

    public static class MulNodeEmitter extends AbstractGenericBinaryNodeOpenCLCEmitter<MulNode> {
        public static final MulNodeEmitter INSTANCE = new MulNodeEmitter();

        private MulNodeEmitter() {
        }

        @Override
        public void genBody(MulNode node, OpenCLCGenContext context, StringBuilder sb, ValuesMethodDefD left, ValuesMethodDefD right) {
            if (left.isConst()) { // (0.0 * x) should already be optimized out
                sb.append("return ").append(context.callDelegate(left)).append(" * ").append(context.callDelegate(right)).append(";\n");
            } else {
                sb.append("const double _left = ").append(context.callDelegate(left)).append(";\n");
                sb.append("return _left == 0.0 ? 0.0 : _left * ").append(context.callDelegate(right)).append(";\n");
            }
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
