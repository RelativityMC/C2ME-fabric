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

import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class Multi2SingleNodeBytecodeEmitter implements BytecodeEmitter<Multi2SingleNode> {
    public static final Multi2SingleNodeBytecodeEmitter INSTANCE = new Multi2SingleNodeBytecodeEmitter();

    private Multi2SingleNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(Multi2SingleNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDef nextMethod = context.newSingleMethod(node.next);

        context.callDelegateSingle(m, nextMethod, nextMethod.returnType());
        switch (nextMethod.returnType()) {
            case F64 -> m.areturn(Type.DOUBLE_TYPE);
            case F32 -> m.areturn(Type.FLOAT_TYPE);
        }
    }

    @Override
    public void doBytecodeGenMulti(Multi2SingleNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
