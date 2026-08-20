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

package com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RoundingDFNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.UnaryNodeBytecodeEmitters;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import static com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter.toASMType;

public class RoundingDFNodeBytecodeEmitter implements BytecodeEmitter<RoundingDFNode> {
    public static final RoundingDFNodeBytecodeEmitter INSTANCE = new RoundingDFNodeBytecodeEmitter();

    private RoundingDFNodeBytecodeEmitter() {
    }

    private static UnaryNodeBytecodeEmitters.AbstractGenericUnaryNodeBytecodeEmitter<?> toEmitter(RoundingDFNode.RoundingUnaryOperation operation) {
        return switch (operation) {
            case FLOOR -> UnaryNodeBytecodeEmitters.FloorNodeEmitter.INSTANCE;
            case ROUND_HALF_UP -> UnaryNodeBytecodeEmitters.RoundHalfUpNodeEmitter.INSTANCE;
            case CEIL -> UnaryNodeBytecodeEmitters.CeilNodeEmitter.INSTANCE;
            case ROUND_TOWARDS_ZERO -> UnaryNodeBytecodeEmitters.RoundTowardsZeroNodeEmitter.INSTANCE;
        };
    }

    @Override
    public void doBytecodeGenSingle(RoundingDFNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = toASMType(returnType);

        ValuesMethodDef inputMethod = context.newSingleMethod(node.input);
        ValuesMethodDef multipleMethod = context.newSingleMethod(node.multiple);

        int inputVar = localVarConsumer.createLocalVariable("input", asmType.getDescriptor());
        int multipleVar = localVarConsumer.createLocalVariable("multiple", asmType.getDescriptor());

        context.callDelegateSingle(m, inputMethod, returnType);
        m.store(inputVar, asmType);
        context.callDelegateSingle(m, multipleMethod, returnType);
        m.store(multipleVar, asmType);

        Label nonZeroMultiple = new Label();
        Label epilogue = new Label();

        m.load(multipleVar, asmType);
        switch (returnType) {
            case F64 -> m.dconst(0.0);
            case F32 -> m.fconst(0.0F);
        }
        m.cmpl(asmType);
        m.ifne(nonZeroMultiple);
        m.load(inputVar, asmType);
        m.goTo(epilogue);
        m.visitLabel(nonZeroMultiple);
        m.load(inputVar, asmType);
        m.load(multipleVar, asmType);
        m.div(asmType);
        toEmitter(node.operation).bytecodeGenInstruction(null, m, localVarConsumer, returnType);
        m.load(multipleVar, asmType);
        m.mul(asmType);
        m.visitLabel(epilogue);
        m.areturn(asmType);
    }

    @Override
    public void doBytecodeGenMulti(RoundingDFNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = toASMType(returnType);

        ValuesMethodDef inputMethod = context.newMultiMethod(node.input);
        ValuesMethodDef multipleMethod = context.newMultiMethod(node.multiple);

        int multiples = localVarConsumer.createLocalVariable("multiples", Type.getDescriptor(double[].class));

        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.arraylength();
        m.iconst(0);
        switch (returnType) {
            case F64 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getDoubleArray", Type.getMethodDescriptor(Type.getType(double[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
            case F32 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getFloatArray", Type.getMethodDescriptor(Type.getType(float[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
        }
        m.store(multiples, InstructionAdapter.OBJECT_TYPE);
        context.callDelegateMulti(m, inputMethod, returnType);
        context.callDelegateMulti(m, multipleMethod, multiples, returnType);

        context.doCountedLoop(m, localVarConsumer, idx -> {
            Label nonZeroMultiple = new Label();
            Label epilogue = new Label();

            int inputVar = localVarConsumer.createLocalVariable("input", asmType.getDescriptor());
            int multipleVar = localVarConsumer.createLocalVariable("multiple", asmType.getDescriptor());

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(asmType);
            m.store(inputVar, asmType);

            m.load(multiples, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(asmType);
            m.store(multipleVar, asmType);

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            {
                m.load(multipleVar, asmType);
                switch (returnType) {
                    case F64 -> m.dconst(0.0);
                    case F32 -> m.fconst(0.0F);
                }
                m.cmpl(asmType);
                m.ifne(nonZeroMultiple);
                m.load(inputVar, asmType);
                m.goTo(epilogue);
                m.visitLabel(nonZeroMultiple);
                m.load(inputVar, asmType);
                m.load(multipleVar, asmType);
                m.div(asmType);
                toEmitter(node.operation).bytecodeGenInstruction(null, m, localVarConsumer, returnType);
                m.load(multipleVar, asmType);
                m.mul(asmType);
                m.visitLabel(epilogue);
            }
            m.astore(asmType);
        });

        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.load(multiples, InstructionAdapter.OBJECT_TYPE);
        switch (returnType) {
            case F64 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class)));
            case F32 -> m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(float[].class)));
        }

        m.areturn(Type.VOID_TYPE);
    }
}
