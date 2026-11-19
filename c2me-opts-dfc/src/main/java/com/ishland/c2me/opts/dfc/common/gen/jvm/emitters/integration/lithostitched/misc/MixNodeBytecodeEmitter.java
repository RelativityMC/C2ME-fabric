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
