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

import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.flowsched.util.Assertions;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Arrays;

public class IntervalSelectNodeBytecodeEmitter implements BytecodeEmitter<IntervalSelectNode> {
    public static final IntervalSelectNodeBytecodeEmitter INSTANCE = new IntervalSelectNodeBytecodeEmitter();

    private IntervalSelectNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(IntervalSelectNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 inputMethod = context.newSingleMethodF64(node.input);

        Label endLabel = new Label();

        context.callDelegateSingle(m, inputMethod);
        ValuesMethodDefF64[] delegates = Arrays.stream(node.functions).map(context::newSingleMethodF64).toArray(ValuesMethodDefF64[]::new);
        genBinarySearch(
                node.thresholds, delegates,
                context, m, endLabel, 0, node.thresholds.length
        );

        for (ValuesMethodDefF64 delegate : delegates) {
            Assertions.assertTrue(delegate == null);
        }

        m.visitLabel(endLabel);
        m.areturn(Type.DOUBLE_TYPE);
    }

    private static void genBinarySearch(double[] thresholds, ValuesMethodDefF64[] delegates, BytecodeGen.Context context, InstructionAdapter m, Label endLabel, int fromIndex, int toIndex) {
        Assertions.assertTrue(fromIndex < toIndex);

        int mid = (fromIndex + toIndex - 1) >>> 1;
        double midVal = thresholds[mid];

        Label geLabel = new Label();
        m.dup2();
        m.dconst(midVal);
        m.cmpg(Type.DOUBLE_TYPE);
        m.ifge(geLabel);

        if (fromIndex == mid) {
            m.pop2();
            context.callDelegateSingle(m, delegates[fromIndex]);
            m.goTo(endLabel);
            delegates[fromIndex] = null;
        } else {
            genBinarySearch(thresholds, delegates, context, m, endLabel, fromIndex, mid);
        }

        m.visitLabel(geLabel);

        if (mid + 1 == toIndex) {
            m.pop2();
            context.callDelegateSingle(m, delegates[toIndex]);
            m.goTo(endLabel);
            delegates[toIndex] = null;
        } else {
            genBinarySearch(thresholds, delegates, context, m, endLabel, mid + 1, toIndex);
        }
    }

    @Override
    public void doBytecodeGenMulti(IntervalSelectNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
