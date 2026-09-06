package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters;

import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.*;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.util.TreeUtils;

public class BinaryNodeSpirVEmitters {

    static int glsl(SpirVGenFunctionContext context, int instruction, int... operands) {
        return context.extInst(context.getGlobalContext().extInstImport(SpirV.Glsl450.NAME),
                instruction, context.getGlobalContext().typeFloat(64), operands);
    }

    public static void register(CodeGenRegistry<SpirVEmitter<? extends AstNode>> registry) {
        registry.registerExactMatch(AddNode.class, AddNodeEmitter.INSTANCE);
        registry.registerExactMatch(DivNode.class, DivNodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxNode.class, MaxNodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxShortNode.class, MaxShortNodeEmitter.INSTANCE);
        registry.registerExactMatch(MinNode.class, MinNodeEmitter.INSTANCE);
        registry.registerExactMatch(MinShortNode.class, MinShortNodeEmitter.INSTANCE);
        registry.registerExactMatch(MulNode.class, MulNodeEmitter.INSTANCE);
    }

    public static abstract class AbstractGenericBinaryNodeSpirVEmitter<T extends AbstractBinaryNode> implements SpirVEmitter<T> {

        @Override
        public int doSpirVGen(T node, SpirVGenFunctionContext context) {
            ValuesMethodDefF64 leftMethod = context.newVarF64(node.left);
            ValuesMethodDefF64 rightMethod = context.newVarF64(node.right);
            return genBody(node, context, context.getDelegateVar(leftMethod), context.getDelegateVar(rightMethod));
        }

        public abstract int genBody(T node, SpirVGenFunctionContext context, int left, int right);
    }

    /**
     * Nodes that skip evaluating their right operand. The OpenCL backend wrote an {@code if};
     * here that is a selection construct writing into a {@code Function}-storage variable,
     * which avoids hand-building {@code OpPhi} and is removed by driver mem2reg.
     */
    private static abstract class AbstractShortCircuitEmitter<T extends AbstractBinaryNode> implements SpirVEmitter<T> {

        @Override
        public int doSpirVGen(T node, SpirVGenFunctionContext context) {
            int f64 = context.getGlobalContext().typeFloat(64);
            int bool = context.getGlobalContext().typeBool();

            int left = context.getDelegateVar(context.newVarF64(node.left));
            int result = context.newLocal(f64);
            int condition = shortCircuitCondition(node, context, bool, left);

            int shortLabel = context.newLabel();
            int longLabel = context.newLabel();
            int mergeLabel = context.newLabel();

            context.selectionMerge(mergeLabel);
            context.branchConditional(condition, shortLabel, longLabel);

            context.label(shortLabel);
            context.store(result, shortCircuitValue(node, context, f64, left));
            context.branch(mergeLabel);

            context.label(longLabel);
            int right;
            if (!Config.preserveAllControlFlows) {
                // Forking scopes the value cache to this arm: a value defined here does not
                // dominate uses after the merge, so it must not be reused outside.
                SpirVGenFunctionContext arm =
                        TreeUtils.hasNonTrivialChildrenUntilBranch(node.right) ? context.fork() : context;
                right = arm.getDelegateVar(arm.newVarF64(node.right));
            } else {
                right = context.getDelegateVar(
                        context.getGlobalContext().newMethodF64(node.right, context.getVariant()));
            }
            context.store(result, combine(node, context, f64, left, right));
            context.branch(mergeLabel);

            context.label(mergeLabel);
            return context.load(f64, result);
        }

        protected abstract int shortCircuitCondition(T node, SpirVGenFunctionContext context, int bool, int left);

        protected abstract int shortCircuitValue(T node, SpirVGenFunctionContext context, int f64, int left);

        protected abstract int combine(T node, SpirVGenFunctionContext context, int f64, int left, int right);
    }

    public static class AddNodeEmitter extends AbstractGenericBinaryNodeSpirVEmitter<AddNode> {
        public static final AddNodeEmitter INSTANCE = new AddNodeEmitter();

        private AddNodeEmitter() {
        }

        @Override
        public int genBody(AddNode node, SpirVGenFunctionContext context, int left, int right) {
            return context.op(SpirV.OP_F_ADD, context.getGlobalContext().typeFloat(64), left, right);
        }
    }

    public static class DivNodeEmitter extends AbstractGenericBinaryNodeSpirVEmitter<DivNode> {
        public static final DivNodeEmitter INSTANCE = new DivNodeEmitter();

        private DivNodeEmitter() {
        }

        @Override
        public int genBody(DivNode node, SpirVGenFunctionContext context, int left, int right) {
            return context.op(SpirV.OP_F_DIV, context.getGlobalContext().typeFloat(64), left, right);
        }
    }

    public static class MaxNodeEmitter extends AbstractGenericBinaryNodeSpirVEmitter<MaxNode> {
        public static final MaxNodeEmitter INSTANCE = new MaxNodeEmitter();

        private MaxNodeEmitter() {
        }

        @Override
        public int genBody(MaxNode node, SpirVGenFunctionContext context, int left, int right) {
            return glsl(context, SpirV.Glsl450.F_MAX, left, right);
        }
    }

    public static class MinNodeEmitter extends AbstractGenericBinaryNodeSpirVEmitter<MinNode> {
        public static final MinNodeEmitter INSTANCE = new MinNodeEmitter();

        private MinNodeEmitter() {
        }

        @Override
        public int genBody(MinNode node, SpirVGenFunctionContext context, int left, int right) {
            return glsl(context, SpirV.Glsl450.F_MIN, left, right);
        }
    }

    public static class MaxShortNodeEmitter extends AbstractShortCircuitEmitter<MaxShortNode> {
        public static final MaxShortNodeEmitter INSTANCE = new MaxShortNodeEmitter();

        private MaxShortNodeEmitter() {
        }

        @Override
        protected int shortCircuitCondition(MaxShortNode node, SpirVGenFunctionContext context, int bool, int left) {
            return context.op(SpirV.OP_F_ORD_GREATER_THAN_EQUAL, bool, left,
                    context.getGlobalContext().constF64(node.rightMax));
        }

        @Override
        protected int shortCircuitValue(MaxShortNode node, SpirVGenFunctionContext context, int f64, int left) {
            return left;
        }

        @Override
        protected int combine(MaxShortNode node, SpirVGenFunctionContext context, int f64, int left, int right) {
            return glsl(context, SpirV.Glsl450.F_MAX, left, right);
        }
    }

    public static class MinShortNodeEmitter extends AbstractShortCircuitEmitter<MinShortNode> {
        public static final MinShortNodeEmitter INSTANCE = new MinShortNodeEmitter();

        private MinShortNodeEmitter() {
        }

        @Override
        protected int shortCircuitCondition(MinShortNode node, SpirVGenFunctionContext context, int bool, int left) {
            return context.op(SpirV.OP_F_ORD_LESS_THAN_EQUAL, bool, left,
                    context.getGlobalContext().constF64(node.rightMin));
        }

        @Override
        protected int shortCircuitValue(MinShortNode node, SpirVGenFunctionContext context, int f64, int left) {
            return left;
        }

        @Override
        protected int combine(MinShortNode node, SpirVGenFunctionContext context, int f64, int left, int right) {
            return glsl(context, SpirV.Glsl450.F_MIN, left, right);
        }
    }

    public static class MulNodeEmitter extends AbstractShortCircuitEmitter<MulNode> {
        public static final MulNodeEmitter INSTANCE = new MulNodeEmitter();

        private MulNodeEmitter() {
        }

        @Override
        public int doSpirVGen(MulNode node, SpirVGenFunctionContext context) {
            if (node.left instanceof ConstantNode) {
                int left = context.getDelegateVar(context.newVarF64(node.left));
                int right = context.getDelegateVar(context.newVarF64(node.right));
                return context.op(SpirV.OP_F_MUL, context.getGlobalContext().typeFloat(64), left, right);
            }
            return super.doSpirVGen(node, context);
        }

        @Override
        protected int shortCircuitCondition(MulNode node, SpirVGenFunctionContext context, int bool, int left) {
            return context.op(SpirV.OP_F_ORD_EQUAL, bool, left, context.getGlobalContext().constF64(0.0));
        }

        @Override
        protected int shortCircuitValue(MulNode node, SpirVGenFunctionContext context, int f64, int left) {
            return context.getGlobalContext().constF64(0.0);
        }

        @Override
        protected int combine(MulNode node, SpirVGenFunctionContext context, int f64, int left, int right) {
            return context.op(SpirV.OP_F_MUL, f64, left, right);
        }
    }

}
