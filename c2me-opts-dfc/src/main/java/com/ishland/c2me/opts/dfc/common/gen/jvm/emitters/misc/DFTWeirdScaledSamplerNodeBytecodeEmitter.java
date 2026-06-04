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

import com.ishland.c2me.base.mixin.access.IDensityFunctionsCaveScaler;
import com.ishland.c2me.opts.dfc.common.ast.noise.DFTWeirdScaledSamplerNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class DFTWeirdScaledSamplerNodeBytecodeEmitter implements BytecodeEmitter<DFTWeirdScaledSamplerNode> {
    public static final DFTWeirdScaledSamplerNodeBytecodeEmitter INSTANCE = new DFTWeirdScaledSamplerNodeBytecodeEmitter();

    private DFTWeirdScaledSamplerNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(DFTWeirdScaledSamplerNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD inputMethod = context.newSingleMethod(node.input);
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
        ValuesMethodDefD inputMethod = context.newMultiMethod(node.input);
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
