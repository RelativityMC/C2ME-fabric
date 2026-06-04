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

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.IMultiMethod;
import com.ishland.c2me.opts.dfc.common.gen.jvm.ISingleMethod;
import com.ishland.c2me.opts.dfc.common.gen.jvm.SubCompiledDensityFunction;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class CacheLikeNodeBytecodeEmitter implements BytecodeEmitter<CacheLikeNode> {
    public static final CacheLikeNodeBytecodeEmitter INSTANCE = new CacheLikeNodeBytecodeEmitter();

    private CacheLikeNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(CacheLikeNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD delegateMethod = context.newSingleMethod(node.getDelegate());
        String cacheLikeField = context.newField(IFastCacheLike.class, node.getCacheLike());
        genPostprocessingMethod(node, context, cacheLikeField);

        int eval = localVarConsumer.createLocalVariable("eval", Type.DOUBLE_TYPE.getDescriptor());

        Label cacheExists = new Label();
        Label cacheMiss = new Label();

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.ifnonnull(cacheExists);
        context.callDelegateSingle(m, delegateMethod);
        m.areturn(Type.DOUBLE_TYPE);

        m.visitLabel(cacheExists);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, Type.INT_TYPE);
        m.load(2, Type.INT_TYPE);
        m.load(3, Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$getCached", Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getType(EvalType.class)));
        m.dup2();
        m.invokestatic(Type.getInternalName(Double.class), "doubleToRawLongBits", Type.getMethodDescriptor(Type.LONG_TYPE, Type.DOUBLE_TYPE), false);
        m.lconst(IFastCacheLike.CACHE_MISS_NAN_BITS);
        m.lcmp();
        m.ifeq(cacheMiss); // operand1 == operand2, branched with cache res
        m.areturn(Type.DOUBLE_TYPE);

        m.visitLabel(cacheMiss);
        m.pop2();

        context.callDelegateSingle(m, delegateMethod);
        m.store(eval, Type.DOUBLE_TYPE);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, Type.INT_TYPE);
        m.load(2, Type.INT_TYPE);
        m.load(3, Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(eval, Type.DOUBLE_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$cache", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getType(EvalType.class), Type.DOUBLE_TYPE));

        m.load(eval, Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(CacheLikeNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefD delegateMethod = context.newMultiMethod(node.getDelegate());
        String cacheLikeField = context.newField(IFastCacheLike.class, node.getCacheLike());

        genPostprocessingMethod(node, context, cacheLikeField);

        Label cacheExists = new Label();
        Label cacheMiss = new Label();

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.ifnonnull(cacheExists);
        context.callDelegateMulti(m, delegateMethod);
        m.areturn(Type.VOID_TYPE);

        m.visitLabel(cacheExists);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$getCached", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(double[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class)));
        m.ifeq(cacheMiss);
        m.areturn(Type.VOID_TYPE);

        m.visitLabel(cacheMiss);
        context.callDelegateMulti(m, delegateMethod);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$cache", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class)));
        m.areturn(Type.VOID_TYPE);
    }

    private void genPostprocessingMethod(CacheLikeNode node, BytecodeGen.Context context, String cacheLikeField) {
        String methodName = String.format("postProcessing_%s", cacheLikeField);
        String delegateSingle = context.newSingleMethodUnoptimized(node.getDelegate());
        String delegateMulti = context.newMultiMethodUnoptimized(node.getDelegate());
        context.genPostprocessingMethod(methodName, m -> {
            Label cacheExists = new Label();

            m.load(0, InstructionAdapter.OBJECT_TYPE);

            {
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
                m.dup();
                m.ifnonnull(cacheExists);
                m.pop();
                m.pop();
                m.areturn(Type.VOID_TYPE);

                m.visitLabel(cacheExists);

                {
                    m.anew(Type.getType(SubCompiledDensityFunction.class));
                    m.dup();

                    m.load(0, InstructionAdapter.OBJECT_TYPE);
                    m.invokedynamic(
                            "evalSingle",
                            Type.getMethodDescriptor(Type.getType(ISingleMethod.class), Type.getType(context.classDesc)),
                            new Handle(
                                    Opcodes.H_INVOKESTATIC,
                                    "java/lang/invoke/LambdaMetafactory",
                                    "metafactory",
                                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                                    false
                            ),
                            new Object[]{
                                    Type.getMethodType(BytecodeGen.Context.SINGLE_DESC),
                                    new Handle(
                                            Opcodes.H_INVOKEVIRTUAL,
                                            context.className,
                                            delegateSingle,
                                            BytecodeGen.Context.SINGLE_DESC,
                                            false
                                    ),
                                    Type.getMethodType(BytecodeGen.Context.SINGLE_DESC)
                            }
                    );

                    m.load(0, InstructionAdapter.OBJECT_TYPE);
                    m.invokedynamic(
                            "evalMulti",
                            Type.getMethodDescriptor(Type.getType(IMultiMethod.class), Type.getType(context.classDesc)),
                            new Handle(
                                    Opcodes.H_INVOKESTATIC,
                                    "java/lang/invoke/LambdaMetafactory",
                                    "metafactory",
                                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                                    false
                            ),
                            new Object[]{
                                    Type.getMethodType(BytecodeGen.Context.MULTI_DESC),
                                    new Handle(
                                            Opcodes.H_INVOKEVIRTUAL,
                                            context.className,
                                            delegateMulti,
                                            BytecodeGen.Context.MULTI_DESC,
                                            false
                                    ),
                                    Type.getMethodType(BytecodeGen.Context.MULTI_DESC)
                            }
                    );

                    m.load(0, InstructionAdapter.OBJECT_TYPE);
                    m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
                    m.checkcast(Type.getType(DensityFunction.class));

                    m.invokespecial(
                            Type.getInternalName(SubCompiledDensityFunction.class),
                            "<init>",
                            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ISingleMethod.class), Type.getType(IMultiMethod.class), Type.getType(DensityFunction.class)),
                            false
                    );

                    m.checkcast(Type.getType(DensityFunction.class));
                }

                m.invokeinterface(
                        Type.getInternalName(IFastCacheLike.class),
                        "c2me$withDelegate",
                        Type.getMethodDescriptor(Type.getType(DensityFunction.class), Type.getType(DensityFunction.class))
                );
            }

            m.putfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));

            m.areturn(Type.VOID_TYPE);
        });
    }
}
