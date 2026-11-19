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
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.jvm.vif.EachApplierVanillaInterface;
import com.ishland.c2me.opts.dfc.common.gen.jvm.vif.NoisePosVanillaInterface;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class DelegateNodeBytecodeEmitter<E extends DelegateNode> implements BytecodeEmitter<E> {
    private static final DelegateNodeBytecodeEmitter<DelegateNode> INSTANCE = new DelegateNodeBytecodeEmitter<>();

    private DelegateNodeBytecodeEmitter() {
    }

    public static <E1 extends DelegateNode> DelegateNodeBytecodeEmitter<E1> instance() {
        return (DelegateNodeBytecodeEmitter<E1>) INSTANCE;
    }

    @Override
    public void doBytecodeGenSingle(DelegateNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        String newField = context.newField(DensityFunction.class, node.getDelegate());

        int borrowedNoisePos = localVarConsumer.createLocalVariable("noisePos", Type.getDescriptor(NoisePosVanillaInterface.class));

        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.load(1, Type.INT_TYPE);
        m.load(2, Type.INT_TYPE);
        m.load(3, Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getNoisePosVanillaInterface", DfcObjectCache.GET_NOISE_POS_VANILLA_INTERFACE_DESC);
        m.store(borrowedNoisePos, InstructionAdapter.OBJECT_TYPE);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, newField, Type.getDescriptor(DensityFunction.class));
        m.load(borrowedNoisePos, InstructionAdapter.OBJECT_TYPE);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeDensityFunctionSample",
                Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.getType(DensityFunction.class), Type.getType(DensityFunction.NoisePos.class)),
                false
        );

        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.load(borrowedNoisePos, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(NoisePosVanillaInterface.class)));

        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(DelegateNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        String newField = context.newField(DensityFunction.class, node.getDelegate());

        int borrowedNoisePos = localVarConsumer.createLocalVariable("noisePos", Type.getDescriptor(NoisePosVanillaInterface.class));

        Label moreThanTwoLabel = new Label();

        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.arraylength();
        m.iconst(1);
        m.ificmpgt(moreThanTwoLabel);

        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.iconst(0);

        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.iconst(0);
        m.aload(Type.INT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.iconst(0);
        m.aload(Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.iconst(0);
        m.aload(Type.INT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getNoisePosVanillaInterface", DfcObjectCache.GET_NOISE_POS_VANILLA_INTERFACE_DESC);
        m.store(borrowedNoisePos, InstructionAdapter.OBJECT_TYPE);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, newField, Type.getDescriptor(DensityFunction.class));
        m.load(borrowedNoisePos, InstructionAdapter.OBJECT_TYPE);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeDensityFunctionSample",
                Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.getType(DensityFunction.class), Type.getType(DensityFunction.NoisePos.class)),
                false
        );

        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.load(borrowedNoisePos, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(NoisePosVanillaInterface.class)));

        m.astore(Type.DOUBLE_TYPE);
        m.areturn(Type.VOID_TYPE);

        m.visitLabel(moreThanTwoLabel);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, newField, Type.getDescriptor(DensityFunction.class));
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.anew(Type.getType(EachApplierVanillaInterface.class));
        m.dup();
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.load(6, InstructionAdapter.OBJECT_TYPE);
        m.invokespecial(Type.getInternalName(EachApplierVanillaInterface.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class), Type.getType(DfcObjectCache.class)), false);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeDensityFunctionFill",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(DensityFunction.class), Type.getType(double[].class), Type.getType(DensityFunction.EachApplier.class)),
                false
        );
        m.areturn(Type.VOID_TYPE);
    }
}
