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

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class RepositionNodeBytecodeEmitter implements BytecodeEmitter<RepositionNode> {
    public static final RepositionNodeBytecodeEmitter INSTANCE = new RepositionNodeBytecodeEmitter();

    private RepositionNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(RepositionNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 input = context.newSingleMethodF64(node.input);
        ValuesMethodDefF64 inputX = context.newSingleMethodF64(node.inputX);
        ValuesMethodDefF64 inputY = context.newSingleMethodF64(node.inputY);
        ValuesMethodDefF64 inputZ = context.newSingleMethodF64(node.inputZ);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        context.callDelegateSingle(m, inputX);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        context.callDelegateSingle(m, inputY);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        context.callDelegateSingle(m, inputZ);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        m.getstatic(Type.getInternalName(EvalType.class), "NORMAL", Type.getDescriptor(EvalType.class));
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokevirtual(context.className, input.generatedMethod(), BytecodeGen.Context.SINGLE_DESC_F64, false);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(RepositionNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        // low usage frequency
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
