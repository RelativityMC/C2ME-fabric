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

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.InvocationShim;
import com.ishland.c2me.opts.dfc.common.gen.jvm.SplineSupport;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.List;

public class SplineAstNodeBytecodeEmitter implements BytecodeEmitter<SplineAstNode> {
    public static final SplineAstNodeBytecodeEmitter INSTANCE = new SplineAstNodeBytecodeEmitter();

    public static final String SPLINE_METHOD_DESC = Type.getMethodDescriptor(Type.getType(float.class), Type.getType(int.class), Type.getType(int.class), Type.getType(int.class), Type.getType(EvalType.class));
    public static final String SPLINE_METHOD_DESC_CACHE1 = Type.getMethodDescriptor(Type.getType(float.class), Type.getType(int.class), Type.getType(int.class), Type.getType(int.class), Type.getType(EvalType.class), Type.getType(float.class));

    private SplineAstNodeBytecodeEmitter() {
    }

    @Override
    public void doBytecodeGenSingle(SplineAstNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        ValuesMethodDefF splineMethod = doBytecodeGenSpline(node, context, node.spline, false);
        callSplineSingle(context, m, splineMethod, -1);
        m.cast(Type.FLOAT_TYPE, Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
    }

    private static ValuesMethodDefF doBytecodeGenSpline(SplineAstNode node, BytecodeGen.Context context, Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, boolean cache1) {
        {
            String cachedSplineMethod = context.getCachedSplineMethod(spline, cache1);
            if (cachedSplineMethod != null) {
                return new ValuesMethodDefF(cachedSplineMethod);
            }
        }
        if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline1) {
            return new ValuesMethodDefF(spline1.value());
        }
        String name = context.nextMethodName("Spline");
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                        name,
                        cache1 ? SPLINE_METHOD_DESC_CACHE1 : SPLINE_METHOD_DESC,
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                                name,
                                cache1 ? SPLINE_METHOD_DESC_CACHE1 : SPLINE_METHOD_DESC,
                                null,
                                null
                        )
                )
        );
        List<IntObjectPair<Pair<String, String>>> extraLocals = new ArrayList<>();
        Label start = new Label();
        Label end = new Label();
        m.visitLabel(start);
        BytecodeGen.Context.LocalVarConsumer localVarConsumer = (localName, localDesc) -> {
            int ordinal = extraLocals.size() + (cache1 ? 6 : 5);
            extraLocals.add(IntObjectPair.of(ordinal, Pair.of(localName, localDesc)));
            return ordinal;
        };

        if (spline instanceof Spline.Implementation<DensityFunctionTypes.Spline.DensityFunctionWrapper> impl) {
//            BytecodeGen.Context.ValuesMethodDefF[] valuesMethods = impl.values().stream()
//                    .map(spline1 -> doBytecodeGenSpline(context, spline1))
//                    .toArray(BytecodeGen.Context.ValuesMethodDefF[]::new);

            String locations = context.newField(float[].class, impl.locations());
            String derivatives = context.newField(float[].class, impl.derivatives());

            int point = cache1 ? 5 : localVarConsumer.createLocalVariable("point", Type.FLOAT_TYPE.getDescriptor());
            int rangeForLocation = localVarConsumer.createLocalVariable("rangeForLocation", Type.INT_TYPE.getDescriptor());

            int lastConst = impl.locations().length - 1;

            if (!cache1) {
                ValuesMethodDefD locationFunction = context.newSingleMethod(node.children.get(impl.locationFunction()));
                context.callDelegateSingle(m, locationFunction);
                m.cast(Type.DOUBLE_TYPE, Type.FLOAT_TYPE);
                m.store(point, Type.FLOAT_TYPE);
            }

            int valuesMethodsLength = impl.values().size();
            if (valuesMethodsLength == 1) {
                m.load(point, Type.FLOAT_TYPE);
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        locations,
                        Type.getDescriptor(float[].class)
                );
                callSplineSingle(context, m, doBytecodeGenSpline(node, context, impl.values().getFirst(), false), -1);
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        derivatives,
                        Type.getDescriptor(float[].class)
                );
                m.iconst(0);
                m.invokestatic(
                        Type.getInternalName(SplineSupport.class),
                        "sampleOutsideRange",
                        Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.getType(float[].class), Type.FLOAT_TYPE, Type.getType(float[].class), Type.INT_TYPE),
                        false
                );
                m.areturn(Type.FLOAT_TYPE);
            } else {
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        locations,
                        Type.getDescriptor(float[].class)
                );
                m.load(point, Type.FLOAT_TYPE);
                m.invokestatic(
                        Type.getInternalName(SplineSupport.class),
                        "findRangeForLocation",
                        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(float[].class), Type.FLOAT_TYPE),
                        false
                );
                m.store(rangeForLocation, Type.INT_TYPE);

                Label label1 = new Label();
                Label label2 = new Label();

                m.load(rangeForLocation, Type.INT_TYPE);
                m.ifge(label1);
                // rangeForLocation < 0
                m.load(point, Type.FLOAT_TYPE);
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        locations,
                        Type.getDescriptor(float[].class)
                );
                callSplineSingle(context, m, doBytecodeGenSpline(node, context, impl.values().getFirst(), false), -1);
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        derivatives,
                        Type.getDescriptor(float[].class)
                );
                m.iconst(0);
                m.invokestatic(
                        Type.getInternalName(SplineSupport.class),
                        "sampleOutsideRange",
                        Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.getType(float[].class), Type.FLOAT_TYPE, Type.getType(float[].class), Type.INT_TYPE),
                        false
                );
                m.areturn(Type.FLOAT_TYPE);

                m.visitLabel(label1);
                m.load(rangeForLocation, Type.INT_TYPE);
                m.iconst(lastConst);
                m.ificmpne(label2);
                // rangeForLocation == last
                m.load(point, Type.FLOAT_TYPE);
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        locations,
                        Type.getDescriptor(float[].class)
                );
                callSplineSingle(context, m, doBytecodeGenSpline(node, context, impl.values().get(lastConst), false), -1);
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        derivatives,
                        Type.getDescriptor(float[].class)
                );
                m.iconst(lastConst);
                m.invokestatic(
                        Type.getInternalName(SplineSupport.class),
                        "sampleOutsideRange",
                        Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.getType(float[].class), Type.FLOAT_TYPE, Type.getType(float[].class), Type.INT_TYPE),
                        false
                );
                m.areturn(Type.FLOAT_TYPE);

                m.visitLabel(label2);

                int loc0 = localVarConsumer.createLocalVariable("loc0", Type.FLOAT_TYPE.getDescriptor());
                int loc1 = localVarConsumer.createLocalVariable("loc1", Type.FLOAT_TYPE.getDescriptor());
                int locDist = localVarConsumer.createLocalVariable("locDist", Type.FLOAT_TYPE.getDescriptor());
                int k = localVarConsumer.createLocalVariable("k", Type.FLOAT_TYPE.getDescriptor());
                int n = localVarConsumer.createLocalVariable("n", Type.FLOAT_TYPE.getDescriptor());
                int o = localVarConsumer.createLocalVariable("o", Type.FLOAT_TYPE.getDescriptor());
                int onDist = localVarConsumer.createLocalVariable("onDist", Type.FLOAT_TYPE.getDescriptor());
                int p = localVarConsumer.createLocalVariable("p", Type.FLOAT_TYPE.getDescriptor());
                int q = localVarConsumer.createLocalVariable("q", Type.FLOAT_TYPE.getDescriptor());

                int cache1Local = localVarConsumer.createLocalVariable("cache1Local", Type.FLOAT_TYPE.getDescriptor());

                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        locations,
                        Type.getDescriptor(float[].class)
                );
                m.load(rangeForLocation, Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);
                m.store(loc0, Type.FLOAT_TYPE);

                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        locations,
                        Type.getDescriptor(float[].class)
                );
                m.load(rangeForLocation, Type.INT_TYPE);
                m.iconst(1);
                m.add(Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);
                m.store(loc1, Type.FLOAT_TYPE);

                m.load(loc1, Type.FLOAT_TYPE);
                m.load(loc0, Type.FLOAT_TYPE);
                m.sub(Type.FLOAT_TYPE);
                m.store(locDist, Type.FLOAT_TYPE);

                m.load(point, Type.FLOAT_TYPE);
                m.load(loc0, Type.FLOAT_TYPE);
                m.sub(Type.FLOAT_TYPE);
                m.load(locDist, Type.FLOAT_TYPE);
                m.div(Type.FLOAT_TYPE);
                m.store(k, Type.FLOAT_TYPE);

                Label[] jumpLabels = new Label[valuesMethodsLength - 1];
                boolean[] jumpGenerated = new boolean[valuesMethodsLength - 1];
                for (int i = 0; i < valuesMethodsLength - 1; i++) {
                    jumpLabels[i] = new Label();
                }
                Label defaultLabel = new Label();
                Label label3 = new Label();

                m.load(rangeForLocation, Type.INT_TYPE);
                m.tableswitch(
                        0,
                        valuesMethodsLength - 2,
                        defaultLabel,
                        jumpLabels
                );

                for (int i = 0; i < valuesMethodsLength - 1; i++) {
                    if (jumpGenerated[i]) continue;
                    m.visitLabel(jumpLabels[i]);
                    jumpGenerated[i] = true;
                    for (int j = i + 1; j < valuesMethodsLength - 1; j++) { // deduplication
                        if (impl.values().get(i).equals(impl.values().get(j)) && impl.values().get(i + 1).equals(impl.values().get(j + 1))) {
                            m.visitLabel(jumpLabels[j]);
                            jumpGenerated[j] = true;
                        }
                    }

                    boolean optimizePure = impl.values().get(i).equals(impl.values().get(i + 1));
                    AstNode sameLocationFunction = SplineAstNode.needOptimizeSameLocationFunction(node, impl.values().get(i), impl.values().get(i + 1));
                    boolean doCache1 = !optimizePure && sameLocationFunction != null;
                    if (doCache1) {
                        ValuesMethodDefD locationFunction = context.newSingleMethod(sameLocationFunction);
                        context.callDelegateSingle(m, locationFunction);
                        m.cast(Type.DOUBLE_TYPE, Type.FLOAT_TYPE);
                        m.store(cache1Local, Type.FLOAT_TYPE);
                    }
                    callSplineSingle(context, m, doBytecodeGenSpline(node, context, impl.values().get(i), doCache1), doCache1 ? cache1Local : -1);
                    if (optimizePure) { // splines are pure
                        m.dup();
                        m.store(n, Type.FLOAT_TYPE);
                        m.store(o, Type.FLOAT_TYPE);
                    } else {
                        m.store(n, Type.FLOAT_TYPE);
                        callSplineSingle(context, m, doBytecodeGenSpline(node, context, impl.values().get(i + 1), doCache1), doCache1 ? cache1Local : -1);
                        m.store(o, Type.FLOAT_TYPE);
                    }
                    m.goTo(label3);
                }

                m.visitLabel(defaultLabel);
                m.iconst(0);
                m.aconst("boom");
                m.invokestatic(
                        Type.getInternalName(Assertions.class),
                        "assertTrue",
                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.BOOLEAN_TYPE, Type.getType(String.class)),
                        false
                );
                m.fconst(Float.NaN); // unreachable code
                m.areturn(Type.FLOAT_TYPE);

                m.visitLabel(label3);

                m.load(o, Type.FLOAT_TYPE);
                m.load(n, Type.FLOAT_TYPE);
                m.sub(Type.FLOAT_TYPE);
                m.store(onDist, Type.FLOAT_TYPE);

                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        derivatives,
                        Type.getDescriptor(float[].class)
                );
                m.load(rangeForLocation, Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);
                m.load(locDist, Type.FLOAT_TYPE);
                m.mul(Type.FLOAT_TYPE);
                m.load(onDist, Type.FLOAT_TYPE);
                m.sub(Type.FLOAT_TYPE);
                m.store(p, Type.FLOAT_TYPE);

                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.getfield(
                        context.className,
                        derivatives,
                        Type.getDescriptor(float[].class)
                );
                m.load(rangeForLocation, Type.INT_TYPE);
                m.iconst(1);
                m.add(Type.INT_TYPE);
                m.aload(Type.FLOAT_TYPE);
                m.neg(Type.FLOAT_TYPE);
                m.load(locDist, Type.FLOAT_TYPE);
                m.mul(Type.FLOAT_TYPE);
                m.load(onDist, Type.FLOAT_TYPE);
                m.add(Type.FLOAT_TYPE);
                m.store(q, Type.FLOAT_TYPE);

                m.load(k, Type.FLOAT_TYPE);
                m.load(n, Type.FLOAT_TYPE);
                m.load(o, Type.FLOAT_TYPE);
                m.invokestatic(
                        Type.getInternalName(InvocationShim.class),
                        "invokeMathHelperLerp",
                        "(FFF)F",
                        false
                );
                m.load(k, Type.FLOAT_TYPE);
                m.fconst(1.0F);
                m.load(k, Type.FLOAT_TYPE);
                m.sub(Type.FLOAT_TYPE);
                m.mul(Type.FLOAT_TYPE);
                m.load(k, Type.FLOAT_TYPE);
                m.load(p, Type.FLOAT_TYPE);
                m.load(q, Type.FLOAT_TYPE);
                m.invokestatic(
                        Type.getInternalName(InvocationShim.class),
                        "invokeMathHelperLerp",
                        "(FFF)F",
                        false
                );
                m.mul(Type.FLOAT_TYPE);
                m.add(Type.FLOAT_TYPE);
                m.areturn(Type.FLOAT_TYPE);
            }

        } else if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> floatFunction) {
            m.fconst(floatFunction.value());
            m.areturn(Type.FLOAT_TYPE);
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported spline implementation: %s", spline.getClass().getName()));
        }

        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("x", Type.INT_TYPE.getDescriptor(), null, start, end, 1);
        m.visitLocalVariable("y", Type.INT_TYPE.getDescriptor(), null, start, end, 2);
        m.visitLocalVariable("z", Type.INT_TYPE.getDescriptor(), null, start, end, 3);
        m.visitLocalVariable("evalType", Type.getType(EvalType.class).getDescriptor(), null, start, end, 4);
        if (cache1) {
            m.visitLocalVariable("pointCached", Type.FLOAT_TYPE.getDescriptor(), null, start, end, 5);
        }
        for (IntObjectPair<Pair<String, String>> local : extraLocals) {
            m.visitLocalVariable(local.right().left(), local.right().right(), null, start, end, local.leftInt());
        }
        m.visitMaxs(0, 0);

        context.cacheSplineMethod(spline, name, cache1);

        return new ValuesMethodDefF(name);
    }

    private static void callSplineSingle(BytecodeGen.Context context, InstructionAdapter m, ValuesMethodDefF target, int cache1Local) {
        if (target.isConst()) {
            m.fconst(target.constValue());
        } else {
            boolean doCache1 = cache1Local != -1;
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.load(1, Type.INT_TYPE);
            m.load(2, Type.INT_TYPE);
            m.load(3, Type.INT_TYPE);
            m.load(4, InstructionAdapter.OBJECT_TYPE);
            if (doCache1) {
                m.load(cache1Local, Type.FLOAT_TYPE);
            }
            m.invokevirtual(context.className, target.generatedMethod(), doCache1 ? SPLINE_METHOD_DESC_CACHE1 : SPLINE_METHOD_DESC, false);
        }
    }

    @Override
    public void doBytecodeGenMulti(SplineAstNode node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.delegateAllToSingle(m, localVarConsumer, node);
        m.areturn(Type.VOID_TYPE);
    }
}
