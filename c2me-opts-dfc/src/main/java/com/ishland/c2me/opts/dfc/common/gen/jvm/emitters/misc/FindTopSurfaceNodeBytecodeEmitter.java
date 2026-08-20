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
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import static com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter.toASMType;

public class FindTopSurfaceNodeBytecodeEmitter implements BytecodeEmitter<FindTopSurfaceNode> {
    public static final FindTopSurfaceNodeBytecodeEmitter INSTANCE = new FindTopSurfaceNodeBytecodeEmitter();

    private FindTopSurfaceNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(FindTopSurfaceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        AstNode.ReturnType returnType = node.getReturnType();
        Type asmType = toASMType(returnType);

        ValuesMethodDef densityMethod = context.newSingleMethod(node.density);
        ValuesMethodDef upperBoundMethod = context.newSingleMethod(node.upperBound);
        ValuesMethodDef lowerBoundMethod = context.newSingleMethod(node.lowerBound);

        int topCellBlockY = localVarConsumer.createLocalVariable("topCellBlockY", Type.INT_TYPE.getDescriptor());
        int lowerBoundEval = localVarConsumer.createLocalVariable("lowerBoundEval", Type.INT_TYPE.getDescriptor());
        context.callDelegateSingle(m, upperBoundMethod, returnType);
        switch (returnType) {
            case F64 -> m.dconst(node.cellHeight);
            case F32 -> m.fconst(node.cellHeight);
        }
        m.div(asmType);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeFloor",
                Type.getMethodDescriptor(Type.INT_TYPE, asmType),
                false
        );
        m.iconst(node.cellHeight);
        m.mul(Type.INT_TYPE);
        m.store(topCellBlockY, Type.INT_TYPE);

        context.callDelegateSingle(m, lowerBoundMethod, returnType);
        m.cast(asmType, Type.INT_TYPE);
        m.store(lowerBoundEval, Type.INT_TYPE);

        Label loopStart = new Label();
        Label loopEnd = new Label();

        int y1 = localVarConsumer.createLocalVariable("y1", Type.INT_TYPE.getDescriptor());
        m.load(topCellBlockY, Type.INT_TYPE);
        m.store(y1, Type.INT_TYPE);

        m.visitLabel(loopStart);

        m.load(y1, Type.INT_TYPE);
        m.load(lowerBoundEval, Type.INT_TYPE);
        m.ificmple(loopEnd);
        if (densityMethod.isConst()) {
            context.callDelegateSingle(m, densityMethod, returnType);
        } else {
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.load(1, Type.INT_TYPE);
            m.load(y1, Type.INT_TYPE);
            m.load(3, Type.INT_TYPE);
            m.getstatic(
                    Type.getInternalName(EvalType.class),
                    "NORMAL",
                    Type.getDescriptor(EvalType.class)
            );
            m.load(5, InstructionAdapter.OBJECT_TYPE);
            String desc = switch (returnType) {
                case F64 -> BytecodeGen.Context.SINGLE_DESC_F64;
                case F32 -> BytecodeGen.Context.SINGLE_DESC_F32;
            };
            m.invokevirtual(context.className, densityMethod.generatedMethod(), desc, false);
        }
        switch (returnType) {
            case F64 -> m.dconst(0.0);
            case F32 -> m.fconst(0.0F);
        }
        m.cmpl(asmType);

        Label notSatisfied = new Label();
        m.ifle(notSatisfied);
        m.load(y1, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, asmType);
        m.areturn(asmType);
        m.visitLabel(notSatisfied);

        m.load(y1, Type.INT_TYPE);
        m.iconst(node.cellHeight);
        m.sub(Type.INT_TYPE);
        m.store(y1, Type.INT_TYPE);
        m.goTo(loopStart);

        m.visitLabel(loopEnd);

        m.load(lowerBoundEval, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, asmType);
        m.areturn(asmType);
    }

    @Override
    public void doBytecodeGenMulti(FindTopSurfaceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
