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

package com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.conversion;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class ToF32NodeBytecodeEmitter implements BytecodeEmitter<ToF32Node> {
    public static final ToF32NodeBytecodeEmitter INSTANCE = new ToF32NodeBytecodeEmitter();

    private ToF32NodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(ToF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDef nextMethod = context.newSingleMethod(node.next);
        context.callDelegateSingle(m, nextMethod, nextMethod.returnType());
        switch (nextMethod.returnType()) {
            case F32 -> {
            }
            case F64 -> m.cast(Type.DOUBLE_TYPE, Type.FLOAT_TYPE);
        }
        m.areturn(Type.FLOAT_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(ToF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDef nextMethod = context.newMultiMethod(node.next);

        switch (nextMethod.returnType()) {
            case F32 -> {
                context.callDelegateMulti(m, nextMethod, AstNode.ReturnType.F32);
            }
            case F64 -> {
                int res1 = localVarConsumer.createLocalVariable("res1", Type.getDescriptor(double[].class));

                m.load(6, InstructionAdapter.OBJECT_TYPE);
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.arraylength();
                m.iconst(0);
                m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getDoubleArray", Type.getMethodDescriptor(Type.getType(double[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
                m.store(res1, InstructionAdapter.OBJECT_TYPE);

                context.callDelegateMulti(m, nextMethod, res1, nextMethod.returnType());

                context.doCountedLoop(m, localVarConsumer, idx -> {
                    m.load(1, InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);

                    m.load(res1, InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);
                    m.aload(Type.DOUBLE_TYPE);

                    m.cast(Type.DOUBLE_TYPE, Type.FLOAT_TYPE);
                    m.astore(Type.FLOAT_TYPE);
                });

                m.load(6, InstructionAdapter.OBJECT_TYPE);
                m.load(res1, InstructionAdapter.OBJECT_TYPE);
                m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class)));
            }
        }

        m.areturn(Type.VOID_TYPE);
    }
}
