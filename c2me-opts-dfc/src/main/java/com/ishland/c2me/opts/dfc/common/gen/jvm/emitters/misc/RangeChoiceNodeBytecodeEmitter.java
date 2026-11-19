/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class RangeChoiceNodeBytecodeEmitter implements BytecodeEmitter<RangeChoiceNode> {
    public static final RangeChoiceNodeBytecodeEmitter INSTANCE = new RangeChoiceNodeBytecodeEmitter();

    private RangeChoiceNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(RangeChoiceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 inputMethod = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 whenInRangeMethod = context.newSingleMethodF64(node.whenInRange);
        ValuesMethodDefF64 whenOutOfRangeMethod = context.newSingleMethodF64(node.whenOutOfRange);

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
        ValuesMethodDefF64 inputSingle = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 whenInRangeSingle = context.newSingleMethodF64(node.whenInRange);
        ValuesMethodDefF64 whenOutOfRangeSingle = context.newSingleMethodF64(node.whenOutOfRange);
        ValuesMethodDefF64 inputMulti = context.newMultiMethodF64(node.input);
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
