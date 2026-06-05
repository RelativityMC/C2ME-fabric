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

package com.ishland.c2me.opts.dfc.common.gen.dot;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.InterpolatedNoiseSamplerNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RootNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.dot.emitters.BinaryNodeDotEmitters;
import com.ishland.c2me.opts.dfc.common.gen.dot.emitters.UnaryNodeDotEmitters;
import com.ishland.c2me.opts.dfc.common.gen.dot.emitters.misc.SplineAstNodeDotEmitter;

import java.util.Arrays;

public class DotGenRegistry {

    public static final CodeGenRegistry<DotEmitter<? extends AstNode>> REGISTRY = new CodeGenRegistry<>();

    static {
        BinaryNodeDotEmitters.register(REGISTRY);
        UnaryNodeDotEmitters.register(REGISTRY);

        //        REGISTRY.registerExactMatch(CacheLikeNode.class, CacheLikeNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(ConstantNode.class, ConstantNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(CoordinateNode.class, CoordinateNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(FindTopSurfaceNode.class, FindTopSurfaceNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(GenericShiftedNoiseNode.class, GenericShiftedNoiseNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(RangeChoiceNode.class, RangeChoiceNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(RootNode.class, RootNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(YClampedGradientNode.class, YClampedGradientNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(DFTWeirdScaledSamplerNode.class, DFTWeirdScaledSamplerNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(SplineAstNode.class, SplineAstNodeBytecodeEmitter.INSTANCE);

        REGISTRY.registerExactMatch(
                CacheLikeNode.class,
                (DotEmitter<CacheLikeNode>) (node, context, builder) ->
                        builder
                                .folderShape()
                                .label("CacheLike\\n" + node.getCacheLike().c2me$describeCacheLike())
                                .edge(context.generate(node.getDelegate())).label("delegate").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                ConstantNode.class,
                (DotEmitter<ConstantNode>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label(String.valueOf(node.getValue()))
                                .build()
        );
        REGISTRY.registerExactMatch(
                CoordinateNode.class,
                (DotEmitter<CoordinateNode>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label("Coordinate " + node.axis)
                                .build()
        );
        REGISTRY.registerExactMatch(
                FindTopSurfaceNode.class,
                (DotEmitter<FindTopSurfaceNode>) (node, context, builder) ->
                        builder
                                .cdsShape()
                                .label("FindTopSurface\\ncellHeight=" + node.cellHeight)
                                .edge(context.generate(node.upperBound)).label("upper bound").finish()
                                .edge(context.generate(node.lowerBound)).label("lower bound").finish()
                                .edge(context.generate(node.density)).label("density").finish()
                                .build()

        );
        REGISTRY.registerExactMatch(
                GenericShiftedNoiseNode.class,
                (DotEmitter<GenericShiftedNoiseNode>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label("GenericShiftedNoise")
                                .edge(context.generate(node.inputX)).label("inputX").finish()
                                .edge(context.generate(node.inputY)).label("inputY").finish()
                                .edge(context.generate(node.inputZ)).label("inputZ").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                RangeChoiceNode.class,
                (DotEmitter<RangeChoiceNode>) (node, context, builder) ->
                        builder
                                .diamondShape()
                                .label("RangeChoice [" + node.minInclusive + ", " + node.maxExclusive + ")")
                                .edge(context.generate(node.input)).label("input").color("blue").finish()
                                .edge(context.generate(node.whenInRange)).label("true").finish()
                                .edge(context.generate(node.whenOutOfRange)).label("false").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                RootNode.class,
                (DotEmitter<RootNode>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label("identity")
                                .edge(context.generate(node.next)).label("next").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                YClampedGradientNode.class,
                (DotEmitter<YClampedGradientNode>) (node, context, builder) ->
                        builder
                                .boxShape()
                                .label("YClampedGradient\\ny=(" + node.fromY + ',' + node.toY + ")\\nvalue=(" + node.fromValue + ',' + node.toValue + ')')
                                .build()
        );
        REGISTRY.registerExactMatch(
                IntervalSelectNode.class,
                (DotEmitter<IntervalSelectNode>) (node, context, builder) -> {
                    builder
                            .boxShape()
                            .label("IntervalSelect\\nthresholds=" + Arrays.toString(node.thresholds));

                    AstNode[] functions = node.functions;
                    for (int i = 0, functionsLength = functions.length; i < functionsLength; i++) {
                        AstNode function = functions[i];
                        builder.edge(context.generate(function)).label(String.valueOf(i)).finish();
                    }
                    return builder.build();
                }
        );
        REGISTRY.registerExactMatch(SplineAstNode.class, SplineAstNodeDotEmitter.INSTANCE);

        REGISTRY.registerExactMatch(
                DelegateNode.class,
                (DotEmitter<DelegateNode>) (node, context, builder) ->
                        builder
                                .trapeziumShape()
                                .label(String.format("delegate %s", node.getDelegate()))
                                .build()
        );
        REGISTRY.registerExactMatch(
                BeardifierNode.class,
                (DotEmitter<BeardifierNode>) (node, context, builder) ->
                        builder
                                .trapeziumShape()
                                .label("Beardifier")
                                .build()
        );
        REGISTRY.registerExactMatch(
                EndIslandsNode.class,
                (DotEmitter<EndIslandsNode>) (node, context, builder) ->
                        builder
                                .trapeziumShape()
                                .label("EndIslands")
                                .build()
        );
        REGISTRY.registerExactMatch(
                InterpolatedNoiseSamplerNode.class,
                (DotEmitter<InterpolatedNoiseSamplerNode>) (node, context, builder) -> {
                    final StringBuilder sb = new StringBuilder();
                    node.sampler.addDebugInfo(sb);
                    return builder
                            .trapeziumShape()
                            .label("InterpolatedNoiseSampler")
                            .tooltip(sb.toString())
                            .build();
                }
        );
    }

    public static <T extends AstNode> int doDotGen(T node, DotGen.Context context, DotGen.Context.Builder builder) {
        DotEmitter<T> emitter = (DotEmitter<T>) REGISTRY.get(node.getClass());
        return emitter.doDotGen(node, context, builder);
    }

}
