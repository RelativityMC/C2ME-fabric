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
