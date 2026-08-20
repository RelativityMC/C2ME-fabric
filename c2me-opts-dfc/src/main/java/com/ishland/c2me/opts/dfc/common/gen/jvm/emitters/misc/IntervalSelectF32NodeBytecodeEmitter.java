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

import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF32Node;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.flowsched.util.Assertions;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Arrays;

public class IntervalSelectF32NodeBytecodeEmitter implements BytecodeEmitter<IntervalSelectF32Node> {
    public static final IntervalSelectF32NodeBytecodeEmitter INSTANCE = new IntervalSelectF32NodeBytecodeEmitter();

    private IntervalSelectF32NodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(IntervalSelectF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF32 inputMethod = context.newSingleMethodF32(node.input);

        Label endLabel = new Label();

        context.callDelegateSingle(m, inputMethod);
        ValuesMethodDefF32[] delegates = Arrays.stream(node.functions).map(context::newSingleMethodF32).toArray(ValuesMethodDefF32[]::new);
        genBinarySearch(
                node.thresholds, delegates,
                context, m, endLabel, 0, node.thresholds.length
        );

        for (ValuesMethodDefF32 delegate : delegates) {
            Assertions.assertTrue(delegate == null);
        }

        m.visitLabel(endLabel);
        m.areturn(Type.FLOAT_TYPE);
    }

    private static void genBinarySearch(float[] thresholds, ValuesMethodDefF32[] delegates, BytecodeGen.Context context, InstructionAdapter m, Label endLabel, int fromIndex, int toIndex) {
        Assertions.assertTrue(fromIndex < toIndex);

        int mid = (fromIndex + toIndex - 1) >>> 1;
        float midVal = thresholds[mid];

        Label geLabel = new Label();
        m.dup();
        m.fconst(midVal);
        m.cmpg(Type.FLOAT_TYPE);
        m.ifge(geLabel);

        if (fromIndex == mid) {
            m.pop();
            context.callDelegateSingle(m, delegates[fromIndex]);
            m.goTo(endLabel);
            delegates[fromIndex] = null;
        } else {
            genBinarySearch(thresholds, delegates, context, m, endLabel, fromIndex, mid);
        }

        m.visitLabel(geLabel);

        if (mid + 1 == toIndex) {
            m.pop();
            context.callDelegateSingle(m, delegates[toIndex]);
            m.goTo(endLabel);
            delegates[toIndex] = null;
        } else {
            genBinarySearch(thresholds, delegates, context, m, endLabel, mid + 1, toIndex);
        }
    }

    @Override
    public void doBytecodeGenMulti(IntervalSelectF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
