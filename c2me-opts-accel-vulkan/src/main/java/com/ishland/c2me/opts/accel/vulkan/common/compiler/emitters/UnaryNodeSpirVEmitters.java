package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.*;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

import static com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.BinaryNodeSpirVEmitters.glsl;

public class UnaryNodeSpirVEmitters {

    public static void register(CodeGenRegistry<SpirVEmitter<? extends AstNode>> registry) {
        registry.registerExactMatch(AbsNode.class, AbsNodeEmitter.INSTANCE);
        registry.registerExactMatch(CubeNode.class, CubeNodeEmitter.INSTANCE);
        registry.registerExactMatch(NegMulNode.class, NegMulNodeEmitter.INSTANCE);
        registry.registerExactMatch(SquareNode.class, SquareNodeEmitter.INSTANCE);
        registry.registerExactMatch(SqueezeNode.class, SqueezeNodeEmitter.INSTANCE);
    }

    public static abstract class AbstractGenericUnaryNodeSpirVEmitter<T extends AbstractUnaryNode> implements SpirVEmitter<T> {

        @Override
        public int doSpirVGen(T node, SpirVGenFunctionContext context) {
            return genBody(node, context, context.getDelegateVar(context.newVarF64(node.operand)));
        }

        protected abstract int genBody(T node, SpirVGenFunctionContext context, int operand);
    }

    public static class AbsNodeEmitter extends AbstractGenericUnaryNodeSpirVEmitter<AbsNode> {
        public static final AbsNodeEmitter INSTANCE = new AbsNodeEmitter();

        private AbsNodeEmitter() {
        }

        @Override
        protected int genBody(AbsNode node, SpirVGenFunctionContext context, int operand) {
            return glsl(context, SpirV.Glsl450.F_ABS, operand);
        }
    }

    public static class SquareNodeEmitter extends AbstractGenericUnaryNodeSpirVEmitter<SquareNode> {
        public static final SquareNodeEmitter INSTANCE = new SquareNodeEmitter();

        private SquareNodeEmitter() {
        }

        @Override
        protected int genBody(SquareNode node, SpirVGenFunctionContext context, int operand) {
            return context.op(SpirV.OP_F_MUL, context.getGlobalContext().typeFloat(64), operand, operand);
        }
    }

    public static class CubeNodeEmitter extends AbstractGenericUnaryNodeSpirVEmitter<CubeNode> {
        public static final CubeNodeEmitter INSTANCE = new CubeNodeEmitter();

        private CubeNodeEmitter() {
        }

        @Override
        protected int genBody(CubeNode node, SpirVGenFunctionContext context, int operand) {
            int f64 = context.getGlobalContext().typeFloat(64);
            int squared = context.op(SpirV.OP_F_MUL, f64, operand, operand);
            return context.op(SpirV.OP_F_MUL, f64, squared, operand);
        }
    }

    public static class NegMulNodeEmitter extends AbstractGenericUnaryNodeSpirVEmitter<NegMulNode> {
        public static final NegMulNodeEmitter INSTANCE = new NegMulNodeEmitter();

        private NegMulNodeEmitter() {
        }

        @Override
        protected int genBody(NegMulNode node, SpirVGenFunctionContext context, int operand) {
            // v > 0.0 ? v : v * negMul -- both arms are cheap, so OpSelect beats a branch.
            int f64 = context.getGlobalContext().typeFloat(64);
            int bool = context.getGlobalContext().typeBool();
            int positive = context.op(SpirV.OP_F_ORD_GREATER_THAN, bool, operand,
                    context.getGlobalContext().constF64(0.0));
            int scaled = context.op(SpirV.OP_F_MUL, f64, operand,
                    context.getGlobalContext().constF64(node.negMul));
            return context.op(SpirV.OP_SELECT, f64, positive, operand, scaled);
        }
    }

    public static class SqueezeNodeEmitter extends AbstractGenericUnaryNodeSpirVEmitter<SqueezeNode> {
        public static final SqueezeNodeEmitter INSTANCE = new SqueezeNodeEmitter();

        private SqueezeNodeEmitter() {
        }

        @Override
        protected int genBody(SqueezeNode node, SpirVGenFunctionContext context, int operand) {
            // v = clamp(x, -1, 1); v / 2.0 - v * v * v / 24.0
            int f64 = context.getGlobalContext().typeFloat(64);
            int v = glsl(context, SpirV.Glsl450.F_CLAMP, operand,
                    context.getGlobalContext().constF64(-1.0),
                    context.getGlobalContext().constF64(1.0));
            int half = context.op(SpirV.OP_F_DIV, f64, v, context.getGlobalContext().constF64(2.0));
            int squared = context.op(SpirV.OP_F_MUL, f64, v, v);
            int cubed = context.op(SpirV.OP_F_MUL, f64, squared, v);
            int scaled = context.op(SpirV.OP_F_DIV, f64, cubed, context.getGlobalContext().constF64(24.0));
            return context.op(SpirV.OP_F_SUB, f64, half, scaled);
        }
    }

}
