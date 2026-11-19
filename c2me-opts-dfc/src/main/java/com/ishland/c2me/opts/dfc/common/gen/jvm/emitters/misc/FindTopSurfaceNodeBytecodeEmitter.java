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

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class FindTopSurfaceNodeBytecodeEmitter implements BytecodeEmitter<FindTopSurfaceNode> {
    public static final FindTopSurfaceNodeBytecodeEmitter INSTANCE = new FindTopSurfaceNodeBytecodeEmitter();

    private FindTopSurfaceNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(FindTopSurfaceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 densityMethod = context.newSingleMethodF64(node.density);
        ValuesMethodDefF64 upperBoundMethod = context.newSingleMethodF64(node.upperBound);
        ValuesMethodDefF64 lowerBoundMethod = context.newSingleMethodF64(node.lowerBound);

        int topCellBlockY = localVarConsumer.createLocalVariable("topCellBlockY", Type.INT_TYPE.getDescriptor());
        int lowerBoundEval = localVarConsumer.createLocalVariable("lowerBoundEval", Type.INT_TYPE.getDescriptor());
        context.callDelegateSingle(m, upperBoundMethod);
        m.dconst(node.cellHeight);
        m.div(Type.DOUBLE_TYPE);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeFloor",
                "(D)I",
                false
        );
        m.iconst(node.cellHeight);
        m.mul(Type.INT_TYPE);
        m.store(topCellBlockY, Type.INT_TYPE);

        context.callDelegateSingle(m, lowerBoundMethod);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        m.store(lowerBoundEval, Type.INT_TYPE);

        Label loopStart = new Label();
        Label loopEnd = new Label();

        int y1 = localVarConsumer.createLocalVariable("y1", Type.INT_TYPE.getDescriptor());
        m.load(topCellBlockY, Type.INT_TYPE);
        m.store(y1, Type.INT_TYPE);

        m.visitLabel(loopStart);

        m.load(y1, Type.INT_TYPE);
        m.load(lowerBoundEval, Type.INT_TYPE);
        m.ificmple(loopEnd);
        if (densityMethod.isConst()) {
            m.dconst(densityMethod.constValue());
        } else {
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.load(1, Type.INT_TYPE);
            m.load(y1, Type.INT_TYPE);
            m.load(3, Type.INT_TYPE);
            m.getstatic(
                    Type.getInternalName(EvalType.class),
                    "NORMAL",
                    Type.getDescriptor(EvalType.class)
            );
            m.load(5, InstructionAdapter.OBJECT_TYPE);
            m.invokevirtual(context.className, densityMethod.generatedMethod(), BytecodeGen.Context.SINGLE_DESC_F64, false);
        }
        m.dconst(0.0);
        m.cmpl(Type.DOUBLE_TYPE);

        Label notSatisfied = new Label();
        m.ifle(notSatisfied);
        m.load(y1, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
        m.visitLabel(notSatisfied);

        m.load(y1, Type.INT_TYPE);
        m.iconst(node.cellHeight);
        m.sub(Type.INT_TYPE);
        m.store(y1, Type.INT_TYPE);
        m.goTo(loopStart);

        m.visitLabel(loopEnd);

        m.load(lowerBoundEval, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(FindTopSurfaceNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
