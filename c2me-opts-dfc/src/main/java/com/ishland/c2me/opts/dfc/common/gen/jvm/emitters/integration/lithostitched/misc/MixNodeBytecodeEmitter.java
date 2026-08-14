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
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
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
        ValuesMethodDefF64 input = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 argument1 = context.newSingleMethodF64(node.argument1);
        ValuesMethodDefF64 argument2 = context.newSingleMethodF64(node.argument2);

        int inputVar = localVarConsumer.createLocalVariable("input", Type.DOUBLE_TYPE.getDescriptor());
        context.callDelegateSingle(m, input);
        m.store(inputVar, Type.DOUBLE_TYPE);

        Label gt = new Label();
        Label lt = new Label();
        Label epilogue = new Label();

        m.load(inputVar, Type.DOUBLE_TYPE);
        m.dconst(0.0);
        m.cmpg(Type.DOUBLE_TYPE);
        m.ifgt(gt);
        context.callDelegateSingle(m, argument1);
        m.goTo(epilogue);

        m.visitLabel(gt);
        m.load(inputVar, Type.DOUBLE_TYPE);
        m.dconst(1.0);
        m.cmpl(Type.DOUBLE_TYPE);
        m.iflt(lt);
        context.callDelegateSingle(m, argument2);
        m.goTo(epilogue);

        m.visitLabel(lt);
        context.callDelegateSingle(m, argument1);
        m.dconst(1.0);
        m.load(inputVar, Type.DOUBLE_TYPE);
        m.sub(Type.DOUBLE_TYPE);
        m.mul(Type.DOUBLE_TYPE);

        context.callDelegateSingle(m, argument2);
        m.load(inputVar, Type.DOUBLE_TYPE);
        m.mul(Type.DOUBLE_TYPE);

        m.add(Type.DOUBLE_TYPE);

        m.visitLabel(epilogue);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(MixNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 input = context.newMultiMethodF64(node.input);
        ValuesMethodDefF64 argument1 = context.newSingleMethodF64(node.argument1);
        ValuesMethodDefF64 argument2 = context.newSingleMethodF64(node.argument2);

        context.callDelegateMulti(m, input);

        int inputVar = localVarConsumer.createLocalVariable("input", Type.DOUBLE_TYPE.getDescriptor());

        context.doCountedLoop(m, localVarConsumer, idx -> {
            Label gt = new Label();
            Label lt = new Label();
            Label epilogue = new Label();

            m.load(1, Type.DOUBLE_TYPE);
            m.load(idx, Type.INT_TYPE);
            m.aload(Type.DOUBLE_TYPE);
            m.store(inputVar, Type.DOUBLE_TYPE);

            m.load(1, Type.DOUBLE_TYPE);
            m.load(idx, Type.INT_TYPE);

            {
                m.load(inputVar, Type.DOUBLE_TYPE);
                m.dconst(0.0);
                m.cmpg(Type.DOUBLE_TYPE);
                m.ifgt(gt);
                context.callDelegateSingleFromMulti(m, argument1, idx);
                m.goTo(epilogue);

                m.visitLabel(gt);
                m.load(inputVar, Type.DOUBLE_TYPE);
                m.dconst(1.0);
                m.cmpl(Type.DOUBLE_TYPE);
                m.iflt(lt);
                context.callDelegateSingleFromMulti(m, argument2, idx);
                m.goTo(epilogue);

                m.visitLabel(lt);
                context.callDelegateSingleFromMulti(m, argument1, idx);
                m.dconst(1.0);
                m.load(inputVar, Type.DOUBLE_TYPE);
                m.sub(Type.DOUBLE_TYPE);
                m.mul(Type.DOUBLE_TYPE);

                context.callDelegateSingleFromMulti(m, argument2, idx);
                m.load(inputVar, Type.DOUBLE_TYPE);
                m.mul(Type.DOUBLE_TYPE);

                m.add(Type.DOUBLE_TYPE);
            }

            m.visitLabel(epilogue);
            m.astore(Type.DOUBLE_TYPE);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
