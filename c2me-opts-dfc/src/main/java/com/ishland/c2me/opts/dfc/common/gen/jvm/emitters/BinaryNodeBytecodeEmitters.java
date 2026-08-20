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
import com.ishland.c2me.opts.dfc.common.ast.binary.AbstractBinaryNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.DivNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortF32Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortF64Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortF32Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortF64Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.PowNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import static com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter.toASMType;

public class BinaryNodeBytecodeEmitters {

    public abstract static class AbstractGenericBinaryNodeBytecodeEmitter<T extends AbstractBinaryNode> implements BytecodeEmitter<T> {
        @Override
        public final void doBytecodeGenSingle(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            AstNode.ReturnType returnType = node.getReturnType();

            ValuesMethodDef leftMethod = context.newSingleMethod(node.left);
            ValuesMethodDef rightMethod = context.newSingleMethod(node.right);

            context.callDelegateSingle(m, leftMethod, returnType);
            context.callDelegateSingle(m, rightMethod, returnType);

            this.bytecodeGenSingleBody(node, m, localVarConsumer, returnType);
        }

        @Override
        public final void doBytecodeGenMulti(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            AstNode.ReturnType returnType = node.getReturnType();
            ValuesMethodDef leftMethod = context.newMultiMethod(node.left);
            ValuesMethodDef rightMethod = context.newMultiMethod(node.right);

            if (leftMethod.isConst()) {
                context.callDelegateMulti(m, rightMethod, returnType);
                context.doCountedLoop(m, localVarConsumer, idx -> bytecodeGenConstMultiBody(node, context, m, localVarConsumer, idx, leftMethod, returnType));
            } else {
                String arrDesc = switch (returnType) {
                    case F64 -> Type.getDescriptor(double[].class);
                    case F32 -> Type.getDescriptor(float[].class);
                };
                int res1 = localVarConsumer.createLocalVariable("res1", arrDesc);

                m.load(6, InstructionAdapter.OBJECT_TYPE);
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.arraylength();
                m.iconst(0);
                switch (returnType) {
                    case F64 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getDoubleArray", Type.getMethodDescriptor(Type.getType(double[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
                    case F32 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getFloatArray", Type.getMethodDescriptor(Type.getType(float[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
                }
                m.store(res1, InstructionAdapter.OBJECT_TYPE);
                context.callDelegateMulti(m, leftMethod, returnType);
                context.callDelegateMulti(m, rightMethod, res1, returnType);

                context.doCountedLoop(m, localVarConsumer, idx -> bytecodeGenMultiBody(node, m, localVarConsumer, idx, res1, returnType));

                m.load(6, InstructionAdapter.OBJECT_TYPE);
                m.load(res1, InstructionAdapter.OBJECT_TYPE);
                switch (returnType) {
                    case F64 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class)));
                    case F32 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(float[].class)));
                }
            }

            m.areturn(Type.VOID_TYPE);
        }

        protected abstract void bytecodeGenInstruction(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType);

        protected void bytecodeGenSingleBody(T node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            this.bytecodeGenInstruction(m, localVarConsumer, returnType);
            m.areturn(toASMType(returnType));
        }

        protected void bytecodeGenMultiBody(T node, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, int idx, int res1, AstNode.ReturnType returnType) {
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.dup2();
            m.aload(toASMType(returnType));
            m.load(res1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(toASMType(returnType));
            this.bytecodeGenInstruction(m, localVarConsumer, returnType);
            m.astore(toASMType(returnType));
        }

        protected void bytecodeGenConstMultiBody(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, int idx, ValuesMethodDef constLeft, AstNode.ReturnType returnType) {
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            context.callDelegateSingle(m, constLeft, returnType);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(toASMType(returnType));
            this.bytecodeGenInstruction(m, localVarConsumer, returnType);
            m.astore(toASMType(returnType));
        }

    }

    public static class AddNodeEmitter extends AbstractGenericBinaryNodeBytecodeEmitter<AddNode> {
        public static final AddNodeEmitter INSTANCE = new AddNodeEmitter();

        private AddNodeEmitter() {
        }

        @Override
        protected void bytecodeGenInstruction(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.add(toASMType(returnType));
        }
    }

    public static class DivNodeEmitter extends AbstractGenericBinaryNodeBytecodeEmitter<DivNode> {
        public static final DivNodeEmitter INSTANCE = new DivNodeEmitter();

        private DivNodeEmitter() {
        }

        @Override
        protected void bytecodeGenInstruction(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.div(toASMType(returnType));
        }
    }

    public static class MaxNodeEmitter extends AbstractGenericBinaryNodeBytecodeEmitter<MaxNode> {
        public static final MaxNodeEmitter INSTANCE = new MaxNodeEmitter();

        private MaxNodeEmitter() {
        }

        @Override
        protected void bytecodeGenInstruction(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "max",
                    Type.getMethodDescriptor(toASMType(returnType), toASMType(returnType), toASMType(returnType)),
                    false
            );
        }
    }

    public static class MaxShortF32NodeEmitter implements BytecodeEmitter<MaxShortF32Node> {
        public static final MaxShortF32NodeEmitter INSTANCE = new MaxShortF32NodeEmitter();

        private MaxShortF32NodeEmitter() {
        }

        @Override
        public void doBytecodeGenSingle(MaxShortF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF32 leftMethod = context.newSingleMethodF32(node.left);
            ValuesMethodDefF32 rightMethod = context.newSingleMethodF32(node.right);

            Label minLabel = new Label();

            context.callDelegateSingle(m, leftMethod);
            m.dup();
            m.fconst(node.rightMax);
            m.cmpl(Type.FLOAT_TYPE);
            m.ifle(minLabel);
            m.areturn(Type.FLOAT_TYPE);

            m.visitLabel(minLabel);
            context.callDelegateSingle(m, rightMethod);
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "max",
                    Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                    false
            );
            m.areturn(Type.FLOAT_TYPE);
        }

        @Override
        public void doBytecodeGenMulti(MaxShortF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF32 leftMethod = context.newMultiMethodF32(node.left);
            ValuesMethodDefF32 rightMethodSingle = context.newSingleMethodF32(node.right);
            context.callDelegateMulti(m, leftMethod);

            context.doCountedLoop(m, localVarConsumer, idx -> {
                Label minLabel = new Label();
                Label end = new Label();

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);

                m.dup();
                m.fconst(node.rightMax);
                m.cmpl(Type.FLOAT_TYPE);
                m.ifle(minLabel);
                m.goTo(end);

                m.visitLabel(minLabel);
                context.callDelegateSingleFromMulti(m, rightMethodSingle, idx);
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "max",
                        Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                        false
                );

                m.visitLabel(end);
                m.astore(Type.FLOAT_TYPE);
            });

            m.areturn(Type.VOID_TYPE);
        }
    }

    public static class MaxShortF64NodeEmitter implements BytecodeEmitter<MaxShortF64Node> {
        public static final MaxShortF64NodeEmitter INSTANCE = new MaxShortF64NodeEmitter();

        private MaxShortF64NodeEmitter() {
        }

        @Override
        public void doBytecodeGenSingle(MaxShortF64Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF64 leftMethod = context.newSingleMethodF64(node.left);
            ValuesMethodDefF64 rightMethod = context.newSingleMethodF64(node.right);

            Label minLabel = new Label();

            context.callDelegateSingle(m, leftMethod);
            m.dup2();
            m.dconst(node.rightMax);
            m.cmpl(Type.DOUBLE_TYPE);
            m.ifle(minLabel);
            m.areturn(Type.DOUBLE_TYPE);

            m.visitLabel(minLabel);
            context.callDelegateSingle(m, rightMethod);
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "max",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            m.areturn(Type.DOUBLE_TYPE);
        }

        @Override
        public void doBytecodeGenMulti(MaxShortF64Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF64 leftMethod = context.newMultiMethodF64(node.left);
            ValuesMethodDefF64 rightMethodSingle = context.newSingleMethodF64(node.right);
            context.callDelegateMulti(m, leftMethod);

            context.doCountedLoop(m, localVarConsumer, idx -> {
                Label minLabel = new Label();
                Label end = new Label();

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);

                m.dup2();
                m.dconst(node.rightMax);
                m.cmpl(Type.DOUBLE_TYPE);
                m.ifle(minLabel);
                m.goTo(end);

                m.visitLabel(minLabel);
                context.callDelegateSingleFromMulti(m, rightMethodSingle, idx);
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "max",
                        Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                        false
                );

                m.visitLabel(end);
                m.astore(Type.DOUBLE_TYPE);
            });

            m.areturn(Type.VOID_TYPE);
        }
    }

    public static class MinNodeEmitter extends AbstractGenericBinaryNodeBytecodeEmitter<MinNode> {
        public static final MinNodeEmitter INSTANCE = new MinNodeEmitter();

        private MinNodeEmitter() {
        }

        @Override
        protected void bytecodeGenInstruction(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "min",
                    Type.getMethodDescriptor(toASMType(returnType), toASMType(returnType), toASMType(returnType)),
                    false
            );
        }
    }

    public static class MinShortF32NodeEmitter implements BytecodeEmitter<MinShortF32Node> {
        public static final MinShortF32NodeEmitter INSTANCE = new MinShortF32NodeEmitter();

        private MinShortF32NodeEmitter() {
        }

        @Override
        public void doBytecodeGenSingle(MinShortF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF32 leftMethod = context.newSingleMethodF32(node.left);
            ValuesMethodDefF32 rightMethod = context.newSingleMethodF32(node.right);

            Label minLabel = new Label();

            context.callDelegateSingle(m, leftMethod);
            m.dup();
            m.fconst(node.rightMin);
            m.cmpg(Type.FLOAT_TYPE);
            m.ifge(minLabel);
            m.areturn(Type.FLOAT_TYPE);

            m.visitLabel(minLabel);
            context.callDelegateSingle(m, rightMethod);
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "min",
                    Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                    false
            );
            m.areturn(Type.FLOAT_TYPE);
        }

        @Override
        public void doBytecodeGenMulti(MinShortF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF32 leftMethod = context.newMultiMethodF32(node.left);
            ValuesMethodDefF32 rightMethodSingle = context.newSingleMethodF32(node.right);
            context.callDelegateMulti(m, leftMethod);

            context.doCountedLoop(m, localVarConsumer, idx -> {
                Label minLabel = new Label();
                Label end = new Label();

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);

                m.dup();
                m.fconst(node.rightMin);
                m.cmpg(Type.FLOAT_TYPE);
                m.ifge(minLabel);
                m.goTo(end);

                m.visitLabel(minLabel);
                context.callDelegateSingleFromMulti(m, rightMethodSingle, idx);
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "min",
                        Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                        false
                );

                m.visitLabel(end);
                m.astore(Type.FLOAT_TYPE);
            });

            m.areturn(Type.VOID_TYPE);
        }
    }

    public static class MinShortF64NodeEmitter implements BytecodeEmitter<MinShortF64Node> {
        public static final MinShortF64NodeEmitter INSTANCE = new MinShortF64NodeEmitter();

        private MinShortF64NodeEmitter() {
        }

        @Override
        public void doBytecodeGenSingle(MinShortF64Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF64 leftMethod = context.newSingleMethodF64(node.left);
            ValuesMethodDefF64 rightMethod = context.newSingleMethodF64(node.right);

            Label minLabel = new Label();

            context.callDelegateSingle(m, leftMethod);
            m.dup2();
            m.dconst(node.rightMin);
            m.cmpg(Type.DOUBLE_TYPE);
            m.ifge(minLabel);
            m.areturn(Type.DOUBLE_TYPE);

            m.visitLabel(minLabel);
            context.callDelegateSingle(m, rightMethod);
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "min",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            m.areturn(Type.DOUBLE_TYPE);
        }

        @Override
        public void doBytecodeGenMulti(MinShortF64Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            ValuesMethodDefF64 leftMethod = context.newMultiMethodF64(node.left);
            ValuesMethodDefF64 rightMethodSingle = context.newSingleMethodF64(node.right);
            context.callDelegateMulti(m, leftMethod);

            context.doCountedLoop(m, localVarConsumer, idx -> {
                Label minLabel = new Label();
                Label end = new Label();

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);

                m.dup2();
                m.dconst(node.rightMin);
                m.cmpg(Type.DOUBLE_TYPE);
                m.ifge(minLabel);
                m.goTo(end);

                m.visitLabel(minLabel);
                context.callDelegateSingleFromMulti(m, rightMethodSingle, idx);
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "min",
                        Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                        false
                );

                m.visitLabel(end);
                m.astore(Type.DOUBLE_TYPE);
            });

            m.areturn(Type.VOID_TYPE);
        }
    }

    public static class MulNodeEmitter implements BytecodeEmitter<MulNode> {
        public static final MulNodeEmitter INSTANCE = new MulNodeEmitter();

        private MulNodeEmitter() {
        }

        @Override
        public void doBytecodeGenSingle(MulNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            AstNode.ReturnType returnType = node.getReturnType();
            Type asmType = toASMType(returnType);
            ValuesMethodDef leftMethod = context.newSingleMethod(node.left);
            ValuesMethodDef rightMethod = context.newSingleMethod(node.right);

            if (leftMethod.isConst()) {
                if (leftMethod instanceof ValuesMethodDefF64 leftF64) {
                    if (leftF64.constValue() == 0.0) {
                        m.dconst(0.0);
                    } else {
                        m.dconst(leftF64.constValue());
                        context.callDelegateSingle(m, (ValuesMethodDefF64) rightMethod);
                        m.mul(Type.DOUBLE_TYPE);
                    }
                } else if (leftMethod instanceof ValuesMethodDefF32 leftF32) {
                    if (leftF32.constValue() == 0.0F) {
                        m.fconst(0.0F);
                    } else {
                        m.fconst(leftF32.constValue());
                        context.callDelegateSingle(m, (ValuesMethodDefF32) rightMethod);
                        m.mul(Type.FLOAT_TYPE);
                    }
                }
            } else {
                Label notZero = new Label();

                context.callDelegateSingle(m, leftMethod, returnType);
                switch (returnType) {
                    case F64 -> {
                        m.dup2();
                        m.dconst(0.0);
                    }
                    case F32 -> {
                        m.dup();
                        m.fconst(0.0F);
                    }
                }
                m.cmpl(asmType);
                m.ifne(notZero);
                switch (returnType) {
                    case F64 -> m.dconst(0.0);
                    case F32 -> m.fconst(0.0F);
                }
                m.areturn(asmType);

                m.visitLabel(notZero);
                context.callDelegateSingle(m, rightMethod, returnType);
                m.mul(asmType);
            }

            m.areturn(asmType);
        }

        @Override
        public void doBytecodeGenMulti(MulNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
            AstNode.ReturnType returnType = node.getReturnType();
            Type asmType = toASMType(returnType);
            ValuesMethodDef leftMethod = context.newMultiMethod(node.left);
            if (leftMethod.isConst()) {
                if (leftMethod instanceof ValuesMethodDefF64 leftF64 && leftF64.constValue() == 0.0) {
                    context.callDelegateMulti(m, leftMethod, returnType);
                } else if (leftMethod instanceof ValuesMethodDefF32 leftF32 && leftF32.constValue() == 0.0F) {
                    context.callDelegateMulti(m, leftMethod, returnType);
                } else {
                    ValuesMethodDef rightMethod = context.newMultiMethod(node.right);

                    context.callDelegateMulti(m, rightMethod, returnType);

                    context.doCountedLoop(m, localVarConsumer, idx -> {
                        m.load(1, InstructionAdapter.OBJECT_TYPE);
                        m.load(idx, Type.INT_TYPE);

                        context.callDelegateSingle(m, leftMethod, returnType); // should always emit const
                        m.load(1, InstructionAdapter.OBJECT_TYPE);
                        m.load(idx, Type.INT_TYPE);
                        m.aload(asmType);
                        m.mul(asmType);

                        m.astore(asmType);
                    });
                }
            } else {
                ValuesMethodDef rightMethodSingle = context.newSingleMethod(node.right);
                context.callDelegateMulti(m, leftMethod, returnType);

                context.doCountedLoop(m, localVarConsumer, idx -> {
                    Label minLabel = new Label();
                    Label end = new Label();

                    m.load(1, InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);

                    m.load(1, InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);
                    m.aload(asmType);

                    switch (returnType) {
                        case F64 -> {
                            m.dup2();
                            m.dconst(0.0);
                        }
                        case F32 -> {
                            m.dup();
                            m.fconst(0.0F);
                        }
                    }
                    m.cmpl(asmType);
                    m.ifne(minLabel);
                    switch (returnType) {
                        case F64 -> {
                            m.pop2();
                            m.dconst(0.0);
                        }
                        case F32 -> {
                            m.pop();
                            m.fconst(0.0F);
                        }
                    }
                    m.goTo(end);

                    m.visitLabel(minLabel);
                    context.callDelegateSingleFromMulti(m, rightMethodSingle, idx, returnType);
                    m.mul(asmType);

                    m.visitLabel(end);
                    m.astore(asmType);
                });
            }

            m.areturn(Type.VOID_TYPE);
        }
    }

    public static class PowNodeEmitter extends AbstractGenericBinaryNodeBytecodeEmitter<PowNode> {
        public static final PowNodeEmitter INSTANCE = new PowNodeEmitter();

        private PowNodeEmitter() {
        }

        @Override
        protected void bytecodeGenInstruction(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode.ReturnType returnType) {
            Type asmType = toASMType(returnType);
            int leftRes = localVarConsumer.createLocalVariable("leftRes", asmType.getDescriptor());
            int rightRes = localVarConsumer.createLocalVariable("rightRes", asmType.getDescriptor());
            m.store(rightRes, asmType);
            m.store(leftRes, asmType);
            m.load(leftRes, asmType);
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(asmType, Type.DOUBLE_TYPE);
            }
            m.load(rightRes, asmType);
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(asmType, Type.DOUBLE_TYPE);
            }
            m.invokestatic(
                    Type.getInternalName(Math.class),
                    "pow",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    false
            );
            if (returnType != AstNode.ReturnType.F64) {
                m.cast(Type.DOUBLE_TYPE, asmType);
            }
        }
    }

    public static void register(CodeGenRegistry<BytecodeEmitter<?>> registry) {
        registry.registerExactMatch(AddNode.class, AddNodeEmitter.INSTANCE);
        registry.registerExactMatch(DivNode.class, DivNodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxNode.class, MaxNodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxShortF32Node.class, MaxShortF32NodeEmitter.INSTANCE);
        registry.registerExactMatch(MaxShortF64Node.class, MaxShortF64NodeEmitter.INSTANCE);
        registry.registerExactMatch(MinNode.class, MinNodeEmitter.INSTANCE);
        registry.registerExactMatch(MinShortF32Node.class, MinShortF32NodeEmitter.INSTANCE);
        registry.registerExactMatch(MinShortF64Node.class, MinShortF64NodeEmitter.INSTANCE);
        registry.registerExactMatch(MulNode.class, MulNodeEmitter.INSTANCE);
        registry.registerExactMatch(PowNode.class, PowNodeEmitter.INSTANCE);
    }

}
