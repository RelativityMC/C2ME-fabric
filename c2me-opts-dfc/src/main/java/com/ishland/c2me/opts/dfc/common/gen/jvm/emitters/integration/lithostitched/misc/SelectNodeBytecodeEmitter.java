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

import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.SelectNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.flowsched.util.Assertions;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Arrays;

public class SelectNodeBytecodeEmitter implements BytecodeEmitter<SelectNode> {
    public static final SelectNodeBytecodeEmitter INSTANCE = new SelectNodeBytecodeEmitter();

    private SelectNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(SelectNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        Assertions.assertTrue(node.minima.length == node.functions.length);
        Assertions.assertTrue(node.maxima.length == node.functions.length);

        ValuesMethodDefF64 inputMethod = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 fallbackMethod = context.newSingleMethodF64(node.fallback);
        ValuesMethodDefF64[] delegates = Arrays.stream(node.functions).map(context::newSingleMethodF64).toArray(ValuesMethodDefF64[]::new);

        int inputValue = localVarConsumer.createLocalVariable("inputValue", Type.DOUBLE_TYPE.getDescriptor());
        context.callDelegateSingle(m, inputMethod);
        m.store(inputValue, Type.DOUBLE_TYPE);

        for (int i = 0; i < delegates.length; i++) {
            Label nextLabel = new Label();

            m.load(inputValue, Type.DOUBLE_TYPE);
            m.dconst(node.minima[i]);
            m.cmpl(Type.DOUBLE_TYPE);
            m.iflt(nextLabel);

            m.load(inputValue, Type.DOUBLE_TYPE);
            m.dconst(node.maxima[i]);
            m.cmpg(Type.DOUBLE_TYPE);
            m.ifgt(nextLabel);

            if (delegates[i].equals(inputMethod)) {
                m.load(inputValue, Type.DOUBLE_TYPE);
            } else {
                context.callDelegateSingle(m, delegates[i]);
            }
            m.areturn(Type.DOUBLE_TYPE);

            m.visitLabel(nextLabel);
        }

        if (fallbackMethod.equals(inputMethod)) {
            m.load(inputValue, Type.DOUBLE_TYPE);
        } else {
            context.callDelegateSingle(m, fallbackMethod);
        }
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(SelectNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        Assertions.assertTrue(node.minima.length == node.functions.length);
        Assertions.assertTrue(node.maxima.length == node.functions.length);

        ValuesMethodDefF64 inputSingle = context.newMultiMethodF64(node.input);
        ValuesMethodDefF64 inputMulti = context.newMultiMethodF64(node.input);
        ValuesMethodDefF64 fallbackSingle = context.newMultiMethodF64(node.fallback);
        ValuesMethodDefF64[] delegates = Arrays.stream(node.functions).map(context::newMultiMethodF64).toArray(ValuesMethodDefF64[]::new);

        context.callDelegateMulti(m, inputMulti);

        context.doCountedLoop(m, localVarConsumer, idx -> {
            Label end = new Label();

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            for (int i = 0; i < delegates.length; i++) {
                Label nextLabel = new Label();

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);
                m.dconst(node.minima[i]);
                m.cmpl(Type.DOUBLE_TYPE);
                m.iflt(nextLabel);

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);
                m.dconst(node.maxima[i]);
                m.cmpg(Type.DOUBLE_TYPE);
                m.ifgt(nextLabel);

                if (delegates[i].equals(inputSingle)) {
                    m.load(1, InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);
                    m.aload(Type.DOUBLE_TYPE);
                } else {
                    context.callDelegateSingleFromMulti(m, delegates[i], idx);
                }
                m.goTo(end);

                m.visitLabel(nextLabel);
            }

            if (fallbackSingle.equals(inputSingle)) {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);
            } else {
                context.callDelegateSingleFromMulti(m, fallbackSingle, idx);
            }

            m.visitLabel(end);
            m.astore(Type.DOUBLE_TYPE);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
