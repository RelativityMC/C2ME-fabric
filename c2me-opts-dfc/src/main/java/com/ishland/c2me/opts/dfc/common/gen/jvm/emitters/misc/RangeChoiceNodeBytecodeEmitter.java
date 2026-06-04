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

import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class RangeChoiceNodeBytecodeEmitter implements BytecodeEmitter<RangeChoiceNode> {
    public static final RangeChoiceNodeBytecodeEmitter INSTANCE = new RangeChoiceNodeBytecodeEmitter();

    private RangeChoiceNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(RangeChoiceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD inputMethod = context.newSingleMethod(node.input);
        ValuesMethodDefD whenInRangeMethod = context.newSingleMethod(node.whenInRange);
        ValuesMethodDefD whenOutOfRangeMethod = context.newSingleMethod(node.whenOutOfRange);

        int inputValue = localVarConsumer.createLocalVariable("inputValue", Type.DOUBLE_TYPE.getDescriptor());
        context.callDelegateSingle(m, inputMethod);
        m.store(inputValue, Type.DOUBLE_TYPE);

        Label whenOutOfRangeLabel = new Label();
        Label end = new Label();

        m.load(inputValue, Type.DOUBLE_TYPE);
        m.dconst(node.minInclusive);
        m.cmpl(Type.DOUBLE_TYPE);
        m.iflt(whenOutOfRangeLabel); // inputValue < minInclusive
        m.load(inputValue, Type.DOUBLE_TYPE);
        m.dconst(node.maxExclusive);
        m.cmpg(Type.DOUBLE_TYPE);
        m.ifge(whenOutOfRangeLabel); // inputValue >= maxExclusive

        if (whenInRangeMethod.equals(inputMethod)) {
            m.load(inputValue, Type.DOUBLE_TYPE);
        } else {
            context.callDelegateSingle(m, whenInRangeMethod);
        }
        m.goTo(end);

        m.visitLabel(whenOutOfRangeLabel);
        if (whenOutOfRangeMethod.equals(inputMethod)) {
            m.load(inputValue, Type.DOUBLE_TYPE);
        } else {
            context.callDelegateSingle(m, whenOutOfRangeMethod);
        }

        m.visitLabel(end);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(RangeChoiceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD inputSingle = context.newSingleMethod(node.input);
        ValuesMethodDefD whenInRangeSingle = context.newSingleMethod(node.whenInRange);
        ValuesMethodDefD whenOutOfRangeSingle = context.newSingleMethod(node.whenOutOfRange);
        ValuesMethodDefD inputMulti = context.newMultiMethod(node.input);
//        String whenInRangeMulti = context.newMultiMethod(this.whenInRange);
//        String whenOutOfRangeMulti = context.newMultiMethod(this.whenOutOfRange);

        context.callDelegateMulti(m, inputMulti);

        context.doCountedLoop(m, localVarConsumer, idx -> {
            Label whenOutOfRangeLabel = new Label();
            Label end = new Label();

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

//            m.load(inputValue, Type.DOUBLE_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.DOUBLE_TYPE);
            m.dconst(node.minInclusive);
            m.cmpl(Type.DOUBLE_TYPE);
            m.iflt(whenOutOfRangeLabel); // inputValue < minInclusive
//            m.load(inputValue, Type.DOUBLE_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.DOUBLE_TYPE);
            m.dconst(node.maxExclusive);
            m.cmpg(Type.DOUBLE_TYPE);
            m.ifge(whenOutOfRangeLabel); // inputValue >= maxExclusive

//            context.callDelegateSingle(m, whenInRangeSingle);
            if (whenInRangeSingle.equals(inputSingle)) {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);
            } else {
                context.callDelegateSingleFromMulti(m, whenInRangeSingle, idx);
            }
            m.goTo(end);

            m.visitLabel(whenOutOfRangeLabel);
            if (whenOutOfRangeSingle.equals(inputSingle)) {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);
            } else {
                context.callDelegateSingleFromMulti(m, whenOutOfRangeSingle, idx);
            }

            m.visitLabel(end);
            m.astore(Type.DOUBLE_TYPE);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
