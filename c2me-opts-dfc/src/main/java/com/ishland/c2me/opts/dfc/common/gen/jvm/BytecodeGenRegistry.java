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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.GenericFastNoiseF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.SelectF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.GradientF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.GradientF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.LerpNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RoundingDFNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.GenericShiftedF64NoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.BinaryNodeBytecodeEmitters;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.UnaryNodeBytecodeEmitters;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.conversion.ToF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.conversion.ToF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.GenericFastNoiseNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.MixNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.SelectF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.GradientF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.GradientF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.RepositionNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.CacheLikeF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.ConstantF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.ConstantF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.CoordinateF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.CoordinateF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.DelegateNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.FindTopSurfaceNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.GenericShiftedF64NoiseNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.IntervalSelectF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.IntervalSelectF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.LerpNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.Multi2SingleNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.RangeChoiceF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.RangeChoiceF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.RoundingDFNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.SplineNormalNodeBytecodeEmitter;
import org.objectweb.asm.commons.InstructionAdapter;

public class BytecodeGenRegistry {

    public static final CodeGenRegistry<BytecodeEmitter<? extends AstNode>> REGISTRY = new CodeGenRegistry<>();

    static {
        BinaryNodeBytecodeEmitters.register(REGISTRY);
        UnaryNodeBytecodeEmitters.register(REGISTRY);

        REGISTRY.registerExactMatch(ToF32Node.class, ToF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(ToF64Node.class, ToF64NodeBytecodeEmitter.INSTANCE);

        REGISTRY.registerExactMatch(CacheLikeF32Node.class, CacheLikeF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(ConstantF32Node.class, ConstantF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(ConstantF64Node.class, ConstantF64NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(CoordinateF32Node.class, CoordinateF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(CoordinateF64Node.class, CoordinateF64NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(FindTopSurfaceNode.class, FindTopSurfaceNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(GenericShiftedF64NoiseNode.class, GenericShiftedF64NoiseNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(GradientF32Node.class, GradientF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(GradientF64Node.class, GradientF64NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(IntervalSelectF32Node.class, IntervalSelectF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(IntervalSelectF64Node.class, IntervalSelectF64NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(LerpNode.class, LerpNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(Multi2SingleNode.class, Multi2SingleNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RangeChoiceF32Node.class, RangeChoiceF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RangeChoiceF64Node.class, RangeChoiceF64NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RepositionNode.class, RepositionNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RoundingDFNode.class, RoundingDFNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(SplineNormalNode.class, SplineNormalNodeBytecodeEmitter.INSTANCE);
//        REGISTRY.registerExactMatch(SplineAstNode.class, SplineAstNodeBytecodeEmitter.INSTANCE);

        REGISTRY.registerExactMatch(DelegateNode.class, DelegateNodeBytecodeEmitter.instance());
        REGISTRY.registerExactMatch(BeardifierNode.class, DelegateNodeBytecodeEmitter.instance());
        REGISTRY.registerExactMatch(EndIslandsNode.class, DelegateNodeBytecodeEmitter.instance());

        REGISTRY.registerExactMatch(GenericFastNoiseF64Node.class, GenericFastNoiseNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(MixNode.class,  MixNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(SelectF64Node.class, SelectF64NodeBytecodeEmitter.INSTANCE);
    }

    public static <T extends AstNode> void doBytecodeGenSingle(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        BytecodeEmitter<T> emitter = (BytecodeEmitter<T>) REGISTRY.get(node.getClass());
        emitter.doBytecodeGenSingle(node, context, m, localVarConsumer);
    }

    public static <T extends AstNode> void doBytecodeGenMulti(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        BytecodeEmitter<T> emitter = (BytecodeEmitter<T>) REGISTRY.get(node.getClass());
        emitter.doBytecodeGenMulti(node, context, m, localVarConsumer);
    }

}
