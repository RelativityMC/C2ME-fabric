/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.dfc.common.gen.jvm.emitters;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbstractUnaryNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CeilNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CosNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.FloorNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.LogNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulF32Node;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulF64Node;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegateNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.RoundHalfUpNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.RoundTowardsZeroNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SignumNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SinNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqrtNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SquareNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqueezeNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import static com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter.toASMType;

public class UnaryNodeBytecodeEmitters {

    public abstract static class AbstractGenericUnaryNodeBytecodeEmitter<T extends AbstractUnaryNode> implements BytecodeEmitter<T> {
        @Override
        public final void doBytecodeGenSingle(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            AstNode.ReturnType returnType = node.getReturnType();
            Type asmType = toASMType(returnType);
            ValuesMethodDef operandMethod = context.newSingleMethod(node.operand);
            context.callDelegateSingle(m, operandMethod, returnType);
            this.bytecodeGenInstruction(node, m, localVarConsumer, returnType);
            m.areturn(asmType);
        }

        @Override
        public final void doBytecodeGenMulti(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            AstNode.ReturnType returnType = node.getReturnType();
            Type asmType = toASMType(returnType);
            ValuesMethodDef operandMethod = context.newMultiMethod(node.operand);
            context.callDelegateMulti(m, operandMethod, returnType);
            context.doCountedLoop(m, localVarConsumer, idx -> {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.dup2();
                m.aload(asmType);
                this.bytecodeGenInstruction(node, m, localVarConsumer, returnType);
                m.astore(asmType);
            });
            m.areturn(Type.VOID_TYPE);
        }

        public abstract void bytecodeGenInstruction(T node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType);
    }

    public static class AbsNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<AbsNode> {
        public static final AbsNodeEmitter INSTANCE = new AbsNodeEmitter();

        private AbsNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(AbsNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "abs",
                    Type.getMethodDescriptor(toASMType(returnType), toASMType(returnType)),
                    false
            );
        }
    }

    public static class CeilNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<CeilNode> {
        public static final CeilNodeEmitter INSTANCE = new CeilNodeEmitter();

        private CeilNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(CeilNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(toASMType(returnType), Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "ceil",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, toASMType(returnType));
            }
        }
    }

    public static class CosNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<CosNode> {
        public static final CosNodeEmitter INSTANCE = new CosNodeEmitter();

        private CosNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(CosNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(toASMType(returnType), Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "cos",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, toASMType(returnType));
            }
        }
    }

    public static class CubeNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<CubeNode> {
        public static final CubeNodeEmitter INSTANCE = new CubeNodeEmitter();

        private CubeNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(CubeNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            switch (returnType) {
                case F64 -> {
                    m.dup2();
                    m.dup2();
                    m.mul(Type.DOUBLE_TYPE);
                    m.mul(Type.DOUBLE_TYPE);
                }
                case F32 -> {
                    m.dup();
                    m.dup();
                    m.mul(Type.FLOAT_TYPE);
                    m.mul(Type.FLOAT_TYPE);
                }
            }
        }
    }

    public static class FloorNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<FloorNode> {
        public static final FloorNodeEmitter INSTANCE = new FloorNodeEmitter();

        private FloorNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(FloorNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(toASMType(returnType), Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "floor",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, toASMType(returnType));
            }
        }
    }

    public static class LogNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<LogNode> {
        public static final LogNodeEmitter INSTANCE = new LogNodeEmitter();

        private LogNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(LogNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(toASMType(returnType), Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "log",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, toASMType(returnType));
            }
        }
    }

    public static class NegateNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<NegateNode> {
        public static final NegateNodeEmitter INSTANCE = new NegateNodeEmitter();

        private NegateNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(NegateNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.neg(toASMType(returnType));
        }
    }

    public static class NegMulF32NodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<NegMulF32Node> {
        public static final NegMulF32NodeEmitter INSTANCE = new NegMulF32NodeEmitter();

        private NegMulF32NodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(NegMulF32Node node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            int v = localVarConsumer.createLocalVariable("v", Type.FLOAT_TYPE.getDescriptor());
            m.store(v, Type.FLOAT_TYPE);

            Label negMulLabel = new Label();
            Label end = new Label();

            m.load(v, Type.FLOAT_TYPE);
            m.fconst(0.0F);
            m.cmpl(Type.FLOAT_TYPE);
            m.ifle(negMulLabel); // v <= 0.0
            m.load(v, Type.FLOAT_TYPE);
            m.goTo(end);
            m.visitLabel(negMulLabel);
            m.load(v, Type.FLOAT_TYPE);
            m.fconst(node.negMul);
            m.mul(Type.FLOAT_TYPE);
            m.visitLabel(end);
        }
    }

    public static class NegMulF64NodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<NegMulF64Node> {
        public static final NegMulF64NodeEmitter INSTANCE = new NegMulF64NodeEmitter();

        private NegMulF64NodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(NegMulF64Node node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            int v = localVarConsumer.createLocalVariable("v", Type.DOUBLE_TYPE.getDescriptor());
            m.store(v, Type.DOUBLE_TYPE);

            Label negMulLabel = new Label();
            Label end = new Label();

            m.load(v, Type.DOUBLE_TYPE);
            m.dconst(0.0);
            m.cmpl(Type.DOUBLE_TYPE);
            m.ifle(negMulLabel); // v <= 0.0
            m.load(v, Type.DOUBLE_TYPE);
            m.goTo(end);
            m.visitLabel(negMulLabel);
            m.load(v, Type.DOUBLE_TYPE);
            m.dconst(node.negMul);
            m.mul(Type.DOUBLE_TYPE);
            m.visitLabel(end);
        }
    }

    public static class RoundHalfUpNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<RoundHalfUpNode> {
        public static final RoundHalfUpNodeEmitter INSTANCE = new RoundHalfUpNodeEmitter();

        private RoundHalfUpNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(RoundHalfUpNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "round",
                    Type.getMethodDescriptor(toASMType(returnType), Type.INT_TYPE),
                    false
            );
            m.cast(Type.INT_TYPE, toASMType(returnType));
        }
    }

    public static class RoundTowardsZeroNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<RoundTowardsZeroNode> {
        public static final RoundTowardsZeroNodeEmitter INSTANCE = new RoundTowardsZeroNodeEmitter();

        private RoundTowardsZeroNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(RoundTowardsZeroNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            Label cmpFail = new Label();
            Label epilogue = new Label();

            Type asmType = toASMType(returnType);
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(asmType, Type.DOUBLE_TYPE);
            }
            m.dup2();
            m.dconst(0.0);
            m.cmpl(Type.DOUBLE_TYPE); // it is probably fine to convert this to a f64 comparison
            m.ifle(cmpFail);
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "floor",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            m.goTo(epilogue);
            m.visitLabel(cmpFail);
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "ceil",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            m.visitLabel(epilogue);
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, asmType);
            }
        }
    }

    public static class SignumNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<SignumNode> {
        public static final SignumNodeEmitter INSTANCE = new SignumNodeEmitter();

        private SignumNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(SignumNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "signum",
                    Type.getMethodDescriptor(toASMType(returnType), toASMType(returnType)),
                    false
            );
        }
    }

    public static class SinNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<SinNode> {
        public static final SinNodeEmitter INSTANCE = new SinNodeEmitter();

        private SinNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(SinNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(toASMType(returnType), Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "sin",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, toASMType(returnType));
            }
        }
    }

    public static class SqrtNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<SqrtNode> {
        public static final SqrtNodeEmitter INSTANCE = new SqrtNodeEmitter();

        private SqrtNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(SqrtNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(toASMType(returnType), Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "sqrt",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, toASMType(returnType));
            }
        }
    }

    public static class SquareNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<SquareNode> {
        public static final SquareNodeEmitter INSTANCE = new SquareNodeEmitter();

        private SquareNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(SquareNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            switch (returnType) {
                case F64 -> {
                    m.dup2();
                    m.mul(Type.DOUBLE_TYPE);
                }
                case F32 -> {
                    m.dup();
                    m.mul(Type.FLOAT_TYPE);
                }
            }
        }
    }

    public static class SqueezeNodeEmitter extends AbstractGenericUnaryNodeBytecodeEmitter<SqueezeNode> {
        public static final SqueezeNodeEmitter INSTANCE = new SqueezeNodeEmitter();

        private SqueezeNodeEmitter() {
        }

        @Override
        public void bytecodeGenInstruction(SqueezeNode node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            Type asmType = toASMType(returnType);

            switch (returnType) {
                case F64 -> {
                    m.dconst(-1.0); // min
                    m.invokestatic(
                            Type.getInternalName(Math.class),
                            "max",
                            Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                            false
                    );
                    m.dconst(1.0); // max
                    m.invokestatic(
                            Type.getInternalName(Math.class),
                            "min",
                            Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                            false
                    );
                }
                case F32 -> {
                    m.fconst(-1.0F); // min
                    m.invokestatic(
                            Type.getInternalName(Math.class),
                            "max",
                            Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                            false
                    );
                    m.fconst(1.0F); // max
                    m.invokestatic(
                            Type.getInternalName(Math.class),
                            "min",
                            Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                            false
                    );
                }
            }

            int v = localVarConsumer.createLocalVariable("v", asmType.getDescriptor());
            m.store(v, asmType);

            switch (returnType) {
                case F64 -> {
                    m.load(v, Type.DOUBLE_TYPE);
                    m.dconst(2.0);
                    m.div(Type.DOUBLE_TYPE);

                    m.load(v, Type.DOUBLE_TYPE);
                    m.dup2();
                    m.dup2();
                    m.mul(Type.DOUBLE_TYPE);
                    m.mul(Type.DOUBLE_TYPE);
                    m.dconst(24.0);
                    m.div(Type.DOUBLE_TYPE);

                    m.sub(Type.DOUBLE_TYPE);
                }
                case F32 -> {
                    m.load(v, Type.FLOAT_TYPE);
                    m.fconst(2.0F);
                    m.div(Type.FLOAT_TYPE);

                    m.load(v, Type.FLOAT_TYPE);
                    m.dup();
                    m.dup();
                    m.mul(Type.FLOAT_TYPE);
                    m.mul(Type.FLOAT_TYPE);
                    m.fconst(24.0F);
                    m.div(Type.FLOAT_TYPE);

                    m.sub(Type.FLOAT_TYPE);
                }
            }
        }
    }

    public static void register(CodeGenRegistry<BytecodeEmitter<?>> registry) {
        registry.registerExactMatch(AbsNode.class, AbsNodeEmitter.INSTANCE);
        registry.registerExactMatch(CeilNode.class, CeilNodeEmitter.INSTANCE);
        registry.registerExactMatch(CosNode.class, CosNodeEmitter.INSTANCE);
        registry.registerExactMatch(CubeNode.class, CubeNodeEmitter.INSTANCE);
        registry.registerExactMatch(FloorNode.class, FloorNodeEmitter.INSTANCE);
        registry.registerExactMatch(LogNode.class, LogNodeEmitter.INSTANCE);
        registry.registerExactMatch(NegateNode.class, NegateNodeEmitter.INSTANCE);
        registry.registerExactMatch(NegMulF32Node.class, NegMulF32NodeEmitter.INSTANCE);
        registry.registerExactMatch(NegMulF64Node.class, NegMulF64NodeEmitter.INSTANCE);
        registry.registerExactMatch(RoundHalfUpNode.class, RoundHalfUpNodeEmitter.INSTANCE);
        registry.registerExactMatch(RoundTowardsZeroNode.class, RoundTowardsZeroNodeEmitter.INSTANCE);
        registry.registerExactMatch(SignumNode.class, SignumNodeEmitter.INSTANCE);
        registry.registerExactMatch(SinNode.class, SinNodeEmitter.INSTANCE);
        registry.registerExactMatch(SqrtNode.class, SqrtNodeEmitter.INSTANCE);
        registry.registerExactMatch(SquareNode.class, SquareNodeEmitter.INSTANCE);
        registry.registerExactMatch(SqueezeNode.class, SqueezeNodeEmitter.INSTANCE);
    }

}
