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

import com.ishland.c2me.opts.dfc.common.ast.misc.GradientF32Node;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class GradientF32NodeBytecodeEmitter implements BytecodeEmitter<GradientF32Node> {
    public static final GradientF32NodeBytecodeEmitter INSTANCE = new GradientF32NodeBytecodeEmitter();

    private GradientF32NodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(GradientF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        int coordinateVar = switch (node.axis) {
            case X -> 1;
            case Y -> 2;
            case Z -> 3;
        };

        int coordRange = node.toCoord - node.fromCoord;
        int relativeCoordVar = localVarConsumer.createLocalVariable("relativeCoord", Type.INT_TYPE.getDescriptor());
        int mappedCoordVar = localVarConsumer.createLocalVariable("mappedCoord", Type.INT_TYPE.getDescriptor());

        m.load(coordinateVar, Type.INT_TYPE);
        m.iconst(node.fromCoord);
        m.sub(Type.INT_TYPE);
        m.store(relativeCoordVar, Type.INT_TYPE);

        switch (node.tiling) {
            case CLAMP_TO_EDGE -> {
                m.load(relativeCoordVar, Type.INT_TYPE);
                m.store(mappedCoordVar, Type.INT_TYPE);
            }
            case REPEAT -> {
                m.load(relativeCoordVar, Type.INT_TYPE);
                m.iconst(coordRange);
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "floorMod",
                        Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE),
                        false
                );
                m.store(mappedCoordVar, Type.INT_TYPE);
            }
            case MIRRORED_REPEAT -> {
                int tileIndexVar = localVarConsumer.createLocalVariable("tileIndex", Type.INT_TYPE.getDescriptor());
                int localCoordVar = localVarConsumer.createLocalVariable("localCoord", Type.INT_TYPE.getDescriptor());
                m.load(relativeCoordVar, Type.INT_TYPE);
                m.iconst(coordRange);
                m.invokestatic(
                        Type.getInternalName(Math.class),
                        "floorDiv",
                        Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE),
                        false
                );
                m.store(tileIndexVar, Type.INT_TYPE);

                m.load(relativeCoordVar, Type.INT_TYPE);
                m.load(tileIndexVar, Type.INT_TYPE);
                m.iconst(coordRange);
                m.mul(Type.INT_TYPE);
                m.sub(Type.INT_TYPE);
                m.store(localCoordVar, Type.INT_TYPE);

                Label cmpFail = new Label();
                Label epilogue = new Label();
                m.load(tileIndexVar, Type.INT_TYPE);
                m.iconst(1);
                m.and(Type.INT_TYPE);
                m.ifne(cmpFail);
                // (tileVar & 1) == 0
                m.load(localCoordVar, Type.INT_TYPE);
                m.goTo(epilogue);
                // else
                m.visitLabel(cmpFail);
                m.iconst(coordRange);
                m.load(localCoordVar, Type.INT_TYPE);
                m.sub(Type.INT_TYPE);
                m.visitLabel(epilogue);
                m.store(mappedCoordVar, Type.INT_TYPE);
            }
        }

        m.load(mappedCoordVar, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.FLOAT_TYPE);
        m.fconst(coordRange);
        m.div(Type.FLOAT_TYPE);
        m.fconst(node.fromValue);
        m.fconst(node.toValue);
        m.invokestatic(
                Type.getInternalName(InvocationShim.class),
                "invokeMathHelperClampedLerp",
                Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                false
        );
        m.areturn(Type.FLOAT_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(GradientF32Node node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
