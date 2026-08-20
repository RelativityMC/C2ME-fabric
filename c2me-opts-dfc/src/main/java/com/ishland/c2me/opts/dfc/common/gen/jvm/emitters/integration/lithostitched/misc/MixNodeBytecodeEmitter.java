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

package com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class MixNodeBytecodeEmitter implements BytecodeEmitter<MixNode> {
    public static final MixNodeBytecodeEmitter INSTANCE = new MixNodeBytecodeEmitter();

    private MixNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(MixNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = BytecodeEmitter.toASMType(returnType);

        ValuesMethodDef input = context.newSingleMethod(node.input);
        ValuesMethodDef argument1 = context.newSingleMethod(node.argument1);
        ValuesMethodDef argument2 = context.newSingleMethod(node.argument2);

        int inputVar = localVarConsumer.createLocalVariable("input", asmType.getDescriptor());
        context.callDelegateSingle(m, input, returnType);
        m.store(inputVar, asmType);

        Label gt = new Label();
        Label lt = new Label();
        Label epilogue = new Label();

        m.load(inputVar, asmType);
        switch (returnType) {
            case F64 -> m.dconst(0.0);
            case F32 -> m.fconst(0.0F);
        }
        m.cmpg(asmType);
        m.ifgt(gt);
        context.callDelegateSingle(m, argument1, returnType);
        m.goTo(epilogue);

        m.visitLabel(gt);
        m.load(inputVar, asmType);
        switch (returnType) {
            case F64 -> m.dconst(1.0);
            case F32 -> m.fconst(1.0F);
        }
        m.cmpl(asmType);
        m.iflt(lt);
        context.callDelegateSingle(m, argument2, returnType);
        m.goTo(epilogue);

        m.visitLabel(lt);
        context.callDelegateSingle(m, argument1, returnType);
        switch (returnType) {
            case F64 -> m.dconst(1.0);
            case F32 -> m.fconst(1.0F);
        }
        m.load(inputVar, asmType);
        m.sub(asmType);
        m.mul(asmType);

        context.callDelegateSingle(m, argument2, returnType);
        m.load(inputVar, asmType);
        m.mul(asmType);

        m.add(asmType);

        m.visitLabel(epilogue);
        m.areturn(asmType);
    }

    @Override
    public void doBytecodeGenMulti(MixNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = BytecodeEmitter.toASMType(returnType);

        ValuesMethodDef input = context.newMultiMethod(node.input);
        ValuesMethodDef argument1 = context.newSingleMethod(node.argument1);
        ValuesMethodDef argument2 = context.newSingleMethod(node.argument2);

        context.callDelegateMulti(m, input, returnType);

        int inputVar = localVarConsumer.createLocalVariable("input", asmType.getDescriptor());

        context.doCountedLoop(m, localVarConsumer, idx -> {
            Label gt = new Label();
            Label lt = new Label();
            Label epilogue = new Label();

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(asmType);
            m.store(inputVar, asmType);

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            {
                m.load(inputVar, asmType);
                switch (returnType) {
                    case F64 -> m.dconst(0.0);
                    case F32 -> m.fconst(0.0F);
                }
                m.cmpg(asmType);
                m.ifgt(gt);
                context.callDelegateSingleFromMulti(m, argument1, idx, returnType);
                m.goTo(epilogue);

                m.visitLabel(gt);
                m.load(inputVar, asmType);
                switch (returnType) {
                    case F64 -> m.dconst(1.0);
                    case F32 -> m.fconst(1.0F);
                }
                m.cmpl(asmType);
                m.iflt(lt);
                context.callDelegateSingleFromMulti(m, argument2, idx, returnType);
                m.goTo(epilogue);

                m.visitLabel(lt);
                context.callDelegateSingleFromMulti(m, argument1, idx, returnType);
                switch (returnType) {
                    case F64 -> m.dconst(1.0);
                    case F32 -> m.fconst(1.0F);
                }
                m.load(inputVar, asmType);
                m.sub(asmType);
                m.mul(asmType);

                context.callDelegateSingleFromMulti(m, argument2, idx, returnType);
                m.load(inputVar, asmType);
                m.mul(asmType);

                m.add(asmType);
            }

            m.visitLabel(epilogue);
            m.astore(asmType);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
