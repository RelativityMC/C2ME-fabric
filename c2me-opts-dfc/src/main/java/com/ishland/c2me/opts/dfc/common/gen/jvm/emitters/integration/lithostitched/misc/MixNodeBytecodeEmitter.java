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

import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class MixNodeBytecodeEmitter implements BytecodeEmitter<MixNode> {
    public static final MixNodeBytecodeEmitter INSTANCE = new MixNodeBytecodeEmitter();

    private MixNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(MixNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD input = context.newSingleMethod(node.input);
        ValuesMethodDefD argument1 = context.newSingleMethod(node.argument1);
        ValuesMethodDefD argument2 = context.newSingleMethod(node.argument2);

        int v = localVarConsumer.createLocalVariable("v", Type.DOUBLE_TYPE.getDescriptor());
        context.callDelegateSingle(m, input);
        m.store(v, Type.DOUBLE_TYPE);

        context.callDelegateSingle(m, argument1);
        m.dconst(1.0);
        m.load(v, Type.DOUBLE_TYPE);
        m.sub(Type.DOUBLE_TYPE);
        m.mul(Type.DOUBLE_TYPE);

        context.callDelegateSingle(m, argument2);
        m.load(v, Type.DOUBLE_TYPE);
        m.mul(Type.DOUBLE_TYPE);

        m.add(Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(MixNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD input = context.newMultiMethod(node.input);
        ValuesMethodDefD argument1 = context.newMultiMethod(node.argument1);
        ValuesMethodDefD argument2 = context.newSingleMethod(node.argument2);

        int argument1Values = localVarConsumer.createLocalVariable("argument1Values", Type.getDescriptor(double[].class));

        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.arraylength();
        m.iconst(0);
        m.invokevirtual(Type.getInternalName(ArrayCache.class), "getDoubleArray", Type.getMethodDescriptor(Type.getType(double[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE), false);
        m.store(argument1Values, InstructionAdapter.OBJECT_TYPE);

        context.callDelegateMulti(m, input);
        context.callDelegateMulti(m, argument1, argument1Values);

        context.doCountedLoop(m, localVarConsumer, idx -> {
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            m.load(argument1Values, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.DOUBLE_TYPE);
            m.dconst(1.0);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.DOUBLE_TYPE);
            m.sub(Type.DOUBLE_TYPE);
            m.mul(Type.DOUBLE_TYPE);

            context.callDelegateSingleFromMulti(m, argument2, idx);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.DOUBLE_TYPE);
            m.mul(Type.DOUBLE_TYPE);

            m.add(Type.DOUBLE_TYPE);
            m.astore(Type.DOUBLE_TYPE);
        });

        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.load(argument1Values, InstructionAdapter.OBJECT_TYPE);
        m.invokevirtual(Type.getInternalName(ArrayCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class)), false);

        m.areturn(Type.VOID_TYPE);
    }
}
