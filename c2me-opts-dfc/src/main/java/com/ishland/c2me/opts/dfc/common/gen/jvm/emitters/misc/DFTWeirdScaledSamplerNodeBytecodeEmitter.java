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

import com.ishland.c2me.base.mixin.access.IDensityFunctionsCaveScaler;
import com.ishland.c2me.opts.dfc.common.ast.noise.DFTWeirdScaledSamplerNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class DFTWeirdScaledSamplerNodeBytecodeEmitter implements BytecodeEmitter<DFTWeirdScaledSamplerNode> {
    public static final DFTWeirdScaledSamplerNodeBytecodeEmitter INSTANCE = new DFTWeirdScaledSamplerNodeBytecodeEmitter();

    private DFTWeirdScaledSamplerNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(DFTWeirdScaledSamplerNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 inputMethod = context.newSingleMethodF64(node.input);
        String noiseField = context.newField(DensityFunction.Noise.class, node.noise);
        int scale = localVarConsumer.createLocalVariable("scale", Type.DOUBLE_TYPE.getDescriptor());

        context.callDelegateSingle(m, inputMethod);

        switch (node.mapper) {
            case TYPE1 -> m.invokestatic(
                    Type.getInternalName(IDensityFunctionsCaveScaler.class),
                    "invokeScaleTunnels",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    true
            );
            case TYPE2 -> m.invokestatic(
                    Type.getInternalName(IDensityFunctionsCaveScaler.class),
                    "invokeScaleCaves",
                    Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                    true
            );
            default -> throw new UnsupportedOperationException(String.format("Unknown mapper %s", node.mapper));
        }

        m.store(scale, Type.DOUBLE_TYPE);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, noiseField, Type.getDescriptor(DensityFunction.Noise.class));

        m.load(1, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
        m.load(scale, Type.DOUBLE_TYPE);
        m.div(Type.DOUBLE_TYPE);

        m.load(2, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
        m.load(scale, Type.DOUBLE_TYPE);
        m.div(Type.DOUBLE_TYPE);

        m.load(3, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
        m.load(scale, Type.DOUBLE_TYPE);
        m.div(Type.DOUBLE_TYPE);

        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeDensityFunctionNoiseSample",
                Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.getType(DensityFunction.Noise.class), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                false
        );
        m.invokestatic(
                Type.getInternalName(Math.class),
                "abs",
                "(D)D",
                false
        );
        m.load(scale, Type.DOUBLE_TYPE);
        m.mul(Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(DFTWeirdScaledSamplerNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF64 inputMethod = context.newMultiMethodF64(node.input);
        String noiseField = context.newField(DensityFunction.Noise.class, node.noise);

        context.callDelegateMulti(m, inputMethod);
        context.doCountedLoop(m, localVarConsumer, idx -> {
            int scale = localVarConsumer.createLocalVariable("scale", Type.DOUBLE_TYPE.getDescriptor());

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.DOUBLE_TYPE);

                switch (node.mapper) {
                    case TYPE1 -> m.invokestatic(
                            Type.getInternalName(IDensityFunctionsCaveScaler.class),
                            "invokeScaleTunnels",
                            Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                            true
                    );
                    case TYPE2 -> m.invokestatic(
                            Type.getInternalName(IDensityFunctionsCaveScaler.class),
                            "invokeScaleCaves",
                            Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                            true
                    );
                    default -> throw new UnsupportedOperationException(String.format("Unknown mapper %s", node.mapper));
                }

                m.store(scale, Type.DOUBLE_TYPE);

                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(context.className, noiseField, Type.getDescriptor(DensityFunction.Noise.class));

                m.load(2, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
                m.load(scale, Type.DOUBLE_TYPE);
                m.div(Type.DOUBLE_TYPE);

                m.load(3, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
                m.load(scale, Type.DOUBLE_TYPE);
                m.div(Type.DOUBLE_TYPE);

                m.load(4, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
                m.load(scale, Type.DOUBLE_TYPE);
                m.div(Type.DOUBLE_TYPE);

                m.invokestatic(
                        Type.getInternalName(InvocationShim.class),
                        "invokeDensityFunctionNoiseSample",
                        Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.getType(DensityFunction.Noise.class), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE),
                        false
                );
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "abs",
                        "(D)D",
                        false
                );

                m.load(scale, Type.DOUBLE_TYPE);
                m.mul(Type.DOUBLE_TYPE);
            }

            m.astore(Type.DOUBLE_TYPE);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
