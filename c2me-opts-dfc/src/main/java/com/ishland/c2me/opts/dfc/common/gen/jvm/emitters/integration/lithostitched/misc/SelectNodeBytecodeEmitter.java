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
        Assertions.assertTrue(node.mins.length == node.functions.length);
        Assertions.assertTrue(node.maxs.length == node.functions.length);

        ValuesMethodDefF64 inputMethod = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 fallbackMethod = context.newSingleMethodF64(node.fallback);
        ValuesMethodDefF64[] delegates = Arrays.stream(node.functions).map(context::newSingleMethodF64).toArray(ValuesMethodDefF64[]::new);

        int inputValue = localVarConsumer.createLocalVariable("inputValue", Type.DOUBLE_TYPE.getDescriptor());
        context.callDelegateSingle(m, inputMethod);
        m.store(inputValue, Type.DOUBLE_TYPE);

        for (int i = 0; i < delegates.length; i++) {
            Label nextLabel = new Label();

            m.load(inputValue, Type.DOUBLE_TYPE);
            m.dconst(node.mins[i]);
            m.cmpl(Type.DOUBLE_TYPE);
            m.iflt(nextLabel);

            m.load(inputValue, Type.DOUBLE_TYPE);
            m.dconst(node.maxs[i]);
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
        Assertions.assertTrue(node.mins.length == node.functions.length);
        Assertions.assertTrue(node.maxs.length == node.functions.length);

        ValuesMethodDefF64 inputSingle = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 inputMulti = context.newMultiMethodF64(node.input);
        ValuesMethodDefF64 fallbackSingle = context.newSingleMethodF64(node.fallback);
        ValuesMethodDefF64[] delegates = Arrays.stream(node.functions).map(context::newSingleMethodF64).toArray(ValuesMethodDefF64[]::new);

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
                m.dconst(node.mins[i]);
                m.cmpl(Type.DOUBLE_TYPE);
                m.iflt(nextLabel);

                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);
                m.dconst(node.maxs[i]);
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
