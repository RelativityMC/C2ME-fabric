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
import com.ishland.c2me.opts.dfc.common.ast.misc.LerpNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import static com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter.toASMType;

public class LerpNodeBytecodeEmitter implements BytecodeEmitter<LerpNode> {
    public static final LerpNodeBytecodeEmitter INSTANCE = new LerpNodeBytecodeEmitter();

    private LerpNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(LerpNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = toASMType(returnType);

        ValuesMethodDef deltaMethod = context.newSingleMethod(node.delta);
        ValuesMethodDef startMethod = context.newSingleMethod(node.start);
        ValuesMethodDef endMethod = context.newSingleMethod(node.end);

        int alphaVar = localVarConsumer.createLocalVariable("alpha", asmType.getDescriptor());
//        int startVar = localVarConsumer.createLocalVariable("start", asmType.getDescriptor());
//        int endVar = localVarConsumer.createLocalVariable("end", asmType.getDescriptor());

        Label alphaNotZero = new Label();
        Label alphaNotOne = new Label();
        Label epilogue = new Label();

        context.callDelegateSingle(m, deltaMethod, returnType);
        m.store(alphaVar, asmType);
        m.load(alphaVar, asmType);
        switch (returnType) {
            case F64 -> m.dconst(0.0);
            case F32 -> m.fconst(0.0F);
        }
        m.cmpl(asmType);
        m.ifne(alphaNotZero);
        // alpha == 0.0
        context.callDelegateSingle(m, startMethod, returnType);
        m.goTo(epilogue);
        m.visitLabel(alphaNotZero);
        m.load(alphaVar, asmType);
        switch (returnType) {
            case F64 -> m.dconst(1.0);
            case F32 -> m.fconst(1.0F);
        }
        m.cmpl(asmType);
        m.ifne(alphaNotOne);
        // alpha == 1.0
        context.callDelegateSingle(m, endMethod, returnType);
        m.goTo(epilogue);
        m.visitLabel(alphaNotOne);
        // else
        m.load(alphaVar, asmType);
        context.callDelegateSingle(m, startMethod, returnType);
        context.callDelegateSingle(m, endMethod, returnType);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeMathHelperLerp",
                Type.getMethodDescriptor(asmType, asmType, asmType, asmType),
                false
        );
        m.visitLabel(epilogue);
        m.areturn(asmType);
    }

    @Override
    public void doBytecodeGenMulti(LerpNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = toASMType(returnType);

        ValuesMethodDef deltaMethod = context.newMultiMethod(node.delta);
        ValuesMethodDef startMethod = context.newSingleMethod(node.start);
        ValuesMethodDef endMethod = context.newSingleMethod(node.end);

        context.callDelegateMulti(m, deltaMethod, returnType);

        context.doCountedLoop(m, localVarConsumer, idx -> {
            int alphaVar = localVarConsumer.createLocalVariable("alpha", asmType.getDescriptor());
//            int startVar = localVarConsumer.createLocalVariable("start", asmType.getDescriptor());
//            int endVar = localVarConsumer.createLocalVariable("end", asmType.getDescriptor());

            Label alphaNotZero = new Label();
            Label alphaNotOne = new Label();
            Label epilogue = new Label();

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(asmType);
            m.store(alphaVar, asmType);

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            {
                m.load(alphaVar, asmType);
                switch (returnType) {
                    case F64 -> m.dconst(0.0);
                    case F32 -> m.fconst(0.0F);
                }
                m.cmpl(asmType);
                m.ifne(alphaNotZero);
                // alpha == 0.0
                context.callDelegateSingleFromMulti(m, startMethod, idx, returnType);
                m.goTo(epilogue);
                m.visitLabel(alphaNotZero);
                m.load(alphaVar, asmType);
                switch (returnType) {
                    case F64 -> m.dconst(1.0);
                    case F32 -> m.fconst(1.0F);
                }
                m.cmpl(asmType);
                m.ifne(alphaNotOne);
                // alpha == 1.0
                context.callDelegateSingleFromMulti(m, endMethod, idx, returnType);
                m.goTo(epilogue);
                m.visitLabel(alphaNotOne);
                // else
                m.load(alphaVar, asmType);
                context.callDelegateSingleFromMulti(m, startMethod, idx, returnType);
                context.callDelegateSingleFromMulti(m, endMethod, idx, returnType);
                m.invokestatic(
                        Type.getInternalName(InvocationShim.class),
                        "invokeMathHelperLerp",
                        Type.getMethodDescriptor(asmType, asmType, asmType, asmType),
                        false
                );
                m.visitLabel(epilogue);
            }
            m.astore(asmType);

        });

        m.areturn(Type.VOID_TYPE);
    }
}
