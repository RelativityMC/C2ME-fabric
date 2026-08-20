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

import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF64Node;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class RangeChoiceF32NodeBytecodeEmitter implements BytecodeEmitter<RangeChoiceF32Node> {
    public static final RangeChoiceF32NodeBytecodeEmitter INSTANCE = new RangeChoiceF32NodeBytecodeEmitter();

    private RangeChoiceF32NodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(RangeChoiceF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF32 inputMethod = context.newSingleMethodF32(node.input);
        ValuesMethodDefF32 whenInRangeMethod = context.newSingleMethodF32(node.whenInRange);
        ValuesMethodDefF32 whenOutOfRangeMethod = context.newSingleMethodF32(node.whenOutOfRange);

        int inputValue = localVarConsumer.createLocalVariable("inputValue", Type.FLOAT_TYPE.getDescriptor());
        context.callDelegateSingle(m, inputMethod);
        m.store(inputValue, Type.FLOAT_TYPE);

        Label whenOutOfRangeLabel = new Label();
        Label end = new Label();

        m.load(inputValue, Type.FLOAT_TYPE);
        m.fconst(node.minInclusive);
        m.cmpl(Type.FLOAT_TYPE);
        m.iflt(whenOutOfRangeLabel); // inputValue < minInclusive
        m.load(inputValue, Type.FLOAT_TYPE);
        m.fconst(node.maxExclusive);
        m.cmpg(Type.FLOAT_TYPE);
        m.ifge(whenOutOfRangeLabel); // inputValue >= maxExclusive

        if (whenInRangeMethod.equals(inputMethod)) {
            m.load(inputValue, Type.FLOAT_TYPE);
        } else {
            context.callDelegateSingle(m, whenInRangeMethod);
        }
        m.goTo(end);

        m.visitLabel(whenOutOfRangeLabel);
        if (whenOutOfRangeMethod.equals(inputMethod)) {
            m.load(inputValue, Type.FLOAT_TYPE);
        } else {
            context.callDelegateSingle(m, whenOutOfRangeMethod);
        }

        m.visitLabel(end);
        m.areturn(Type.FLOAT_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(RangeChoiceF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF32 inputSingle = context.newSingleMethodF32(node.input);
        ValuesMethodDefF32 whenInRangeSingle = context.newSingleMethodF32(node.whenInRange);
        ValuesMethodDefF32 whenOutOfRangeSingle = context.newSingleMethodF32(node.whenOutOfRange);
        ValuesMethodDefF32 inputMulti = context.newMultiMethodF32(node.input);
//        String whenInRangeMulti = context.newMultiMethod(this.whenInRange);
//        String whenOutOfRangeMulti = context.newMultiMethod(this.whenOutOfRange);

        context.callDelegateMulti(m, inputMulti);

        context.doCountedLoop(m, localVarConsumer, idx -> {
            Label whenOutOfRangeLabel = new Label();
            Label end = new Label();

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.FLOAT_TYPE);
            m.fconst(node.minInclusive);
            m.cmpl(Type.FLOAT_TYPE);
            m.iflt(whenOutOfRangeLabel); // inputValue < minInclusive

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.FLOAT_TYPE);
            m.fconst(node.maxExclusive);
            m.cmpg(Type.FLOAT_TYPE);
            m.ifge(whenOutOfRangeLabel); // inputValue >= maxExclusive

//            context.callDelegateSingle(m, whenInRangeSingle);
            if (whenInRangeSingle.equals(inputSingle)) {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);
            } else {
                context.callDelegateSingleFromMulti(m, whenInRangeSingle, idx);
            }
            m.goTo(end);

            m.visitLabel(whenOutOfRangeLabel);
            if (whenOutOfRangeSingle.equals(inputSingle)) {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);
            } else {
                context.callDelegateSingleFromMulti(m, whenOutOfRangeSingle, idx);
            }

            m.visitLabel(end);
            m.astore(Type.FLOAT_TYPE);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
