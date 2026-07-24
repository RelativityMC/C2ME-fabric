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

import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.ShiftNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Arrays;

public class ShiftNodeBytecodeEmitter implements BytecodeEmitter<ShiftNode> {
    public static final ShiftNodeBytecodeEmitter INSTANCE = new ShiftNodeBytecodeEmitter();

    private ShiftNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(ShiftNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD input = context.newSingleMethod(node.input);
        ValuesMethodDefD inputX = context.newSingleMethod(node.inputX);
        ValuesMethodDefD inputY = context.newSingleMethod(node.inputY);
        ValuesMethodDefD inputZ = context.newSingleMethod(node.inputZ);

        if (input.isConst()) {
            m.dconst(input.constValue());
            m.areturn(Type.DOUBLE_TYPE);
            return;
        }

        int shiftX = localVarConsumer.createLocalVariable("shiftX", Type.INT_TYPE.getDescriptor());
        int shiftY = localVarConsumer.createLocalVariable("shiftY", Type.INT_TYPE.getDescriptor());
        int shiftZ = localVarConsumer.createLocalVariable("shiftZ", Type.INT_TYPE.getDescriptor());

        context.callDelegateSingle(m, inputX);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        m.store(shiftX, Type.INT_TYPE);
        context.callDelegateSingle(m, inputY);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        m.store(shiftY, Type.INT_TYPE);
        context.callDelegateSingle(m, inputZ);
        m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);
        m.store(shiftZ, Type.INT_TYPE);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.load(shiftX, Type.INT_TYPE);
        m.load(shiftY, Type.INT_TYPE);
        m.load(shiftZ, Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokevirtual(context.className, input.generatedMethod(), BytecodeGen.Context.SINGLE_DESC, false);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(ShiftNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD input = context.newSingleMethod(node.input);
        ValuesMethodDefD inputX = context.newMultiMethod(node.inputX);
        ValuesMethodDefD inputY = context.newMultiMethod(node.inputY);
        ValuesMethodDefD inputZ = context.newMultiMethod(node.inputZ);

        if (input.isConst()) {
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.dconst(input.constValue());
            m.invokestatic(Type.getInternalName(Arrays.class), "fill", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class), Type.DOUBLE_TYPE), false);
            m.areturn(Type.VOID_TYPE);
            return;
        }

        boolean eliminatedX = inputX.isConst();
        boolean eliminatedY = inputY.isConst();
        boolean eliminatedZ = inputZ.isConst();
        int arraysNeeded = (!eliminatedX ? 1 : 0) + (!eliminatedY ? 1 : 0) + (!eliminatedZ ? 1 : 0);

        int[] arrays = new int[arraysNeeded];
        if (arraysNeeded >= 1) {
            arrays[0] = 1;
        }
        if (arraysNeeded >= 2) {
            arrays[1] = localVarConsumer.createLocalVariable("shiftArr1", Type.getDescriptor(double[].class));
            m.load(6, InstructionAdapter.OBJECT_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.arraylength();
            m.iconst(0);
            m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getDoubleArray", Type.getMethodDescriptor(Type.getType(double[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
            m.store(arrays[1], InstructionAdapter.OBJECT_TYPE);
        }
        if (arraysNeeded >= 3) {
            arrays[2] = localVarConsumer.createLocalVariable("shiftArr2", Type.getDescriptor(double[].class));
            m.load(6, InstructionAdapter.OBJECT_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.arraylength();
            m.iconst(0);
            m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "getDoubleArray", Type.getMethodDescriptor(Type.getType(double[].class), Type.INT_TYPE, Type.BOOLEAN_TYPE));
            m.store(arrays[2], InstructionAdapter.OBJECT_TYPE);
        }

        {
            int arrIdx = 0;
            if (!eliminatedX) {
                context.callDelegateMulti(m, inputX, arrays[arrIdx ++]);
            }
            if (!eliminatedY) {
                context.callDelegateMulti(m, inputY, arrays[arrIdx ++]);
            }
            if (!eliminatedZ) {
                context.callDelegateMulti(m, inputZ, arrays[arrIdx ++]);
            }
        }

        context.doCountedLoop(m, localVarConsumer, idx -> {
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            {
                m.load(0, InstructionAdapter.OBJECT_TYPE);

                int arrIdx = 0;
                if (!eliminatedX) {
                    m.load(arrays[arrIdx ++], InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);
                    m.aload(Type.DOUBLE_TYPE);
                } else {
                    m.dconst(inputX.constValue());
                }
                m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);

                if (!eliminatedY) {
                    m.load(arrays[arrIdx ++], InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);
                    m.aload(Type.DOUBLE_TYPE);
                } else {
                    m.dconst(inputY.constValue());
                }
                m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);

                if (!eliminatedZ) {
                    m.load(arrays[arrIdx ++], InstructionAdapter.OBJECT_TYPE);
                    m.load(idx, Type.INT_TYPE);
                    m.aload(Type.DOUBLE_TYPE);
                } else {
                    m.dconst(inputZ.constValue());
                }
                m.cast(Type.DOUBLE_TYPE, Type.INT_TYPE);

                m.load(5, InstructionAdapter.OBJECT_TYPE);
                m.load(6, InstructionAdapter.OBJECT_TYPE);
                m.invokevirtual(context.className, input.generatedMethod(), BytecodeGen.Context.SINGLE_DESC, false);
            }

            m.astore(Type.DOUBLE_TYPE);
        });

        for (int i = 1; i < arrays.length; i ++) {
            m.load(6, InstructionAdapter.OBJECT_TYPE);
            m.load(arrays[i], InstructionAdapter.OBJECT_TYPE);
            m.invokeinterface(Type.getInternalName(DfcObjectCache.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class)));
        }

        m.areturn(Type.VOID_TYPE);
    }
}
