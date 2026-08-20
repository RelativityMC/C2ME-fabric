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
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.GenericFastNoiseF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.SelectF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.GradientF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.GradientF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.LerpNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RoundingDFNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.GenericShiftedF64NoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.dot.emitters.BinaryNodeDotEmitters;
import com.ishland.c2me.opts.dfc.common.gen.dot.emitters.UnaryNodeDotEmitters;
import com.ishland.c2me.opts.dfc.common.gen.dot.emitters.misc.SplineNormalNodeDotEmitter;

public class DotGenRegistry {

    public static final CodeGenRegistry<DotEmitter<? extends AstNode>> REGISTRY = new CodeGenRegistry<>();

    static {
        BinaryNodeDotEmitters.register(REGISTRY);
        UnaryNodeDotEmitters.register(REGISTRY);

        //        REGISTRY.registerExactMatch(CacheLikeF32Node.class, CacheLikeNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(ConstantF64Node.class, ConstantNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(CoordinateF64Node.class, CoordinateNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(FindTopSurfaceNode.class, FindTopSurfaceNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(GenericShiftedNoiseNode.class, GenericShiftedNoiseNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(RangeChoiceF32Node.class, RangeChoiceNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(RootNode.class, RootNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(YClampedGradientNode.class, YClampedGradientNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(DFTWeirdScaledSamplerNode.class, DFTWeirdScaledSamplerNodeBytecodeEmitter.INSTANCE);
        //        REGISTRY.registerExactMatch(SplineAstNode.class, SplineAstNodeBytecodeEmitter.INSTANCE);

        REGISTRY.registerExactMatch(ToF32Node.class, (DotEmitter<ToF32Node>) (node, context, builder) ->
                builder
                        .triangleShape()
                        .label("ToF32")
                        .edge(context.generate(node.next)).label("next").finish()
                        .build()
        );
        REGISTRY.registerExactMatch(ToF64Node.class, (DotEmitter<ToF64Node>) (node, context, builder) ->
                builder
                        .triangleShape()
                        .label("ToF64")
                        .edge(context.generate(node.next)).label("next").finish()
                        .build()
        );

        REGISTRY.registerExactMatch(
                CacheLikeF32Node.class,
                (DotEmitter<CacheLikeF32Node>) (node, context, builder) ->
                        builder
                                .folderShape()
                                .label("CacheLikeF32\\n" + node.getCacheLike().c2me$describeCacheLike())
                                .edge(context.generate(node.getDelegate())).label("delegate").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                ConstantF64Node.class,
                (DotEmitter<ConstantF64Node>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label("constF64\\n" + node.getValue())
                                .build()
        );
        REGISTRY.registerExactMatch(
                ConstantF32Node.class,
                (DotEmitter<ConstantF32Node>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label("constF32\\n" + node.getValue())
                                .build()
        );
        REGISTRY.registerExactMatch(
                CoordinateF64Node.class,
                (DotEmitter<CoordinateF64Node>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label("CoordinateF64 " + node.axis)
                                .build()
        );
        REGISTRY.registerExactMatch(
                CoordinateF32Node.class,
                (DotEmitter<CoordinateF32Node>) (node, context, builder) ->
                        builder
                                .triangleShape()
                                .label("CoordinateF32 " + node.axis)
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
                GenericShiftedF64NoiseNode.class,
                (DotEmitter<GenericShiftedF64NoiseNode>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label("GenericShiftedF64Noise")
                                .edge(context.generate(node.inputX)).label("inputX").finish()
                                .edge(context.generate(node.inputY)).label("inputY").finish()
                                .edge(context.generate(node.inputZ)).label("inputZ").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                GradientF32Node.class,
                (DotEmitter<GradientF32Node>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label(
                                        "GradientF32\\n" +
                                                "axis=" + node.axis + ",tiling=" + node.tiling + "\\n" +
                                                "coord=[" + node.fromCoord + "," + node.toCoord + ")\\n" +
                                                "value=[" + node.fromValue + "," + node.toValue + ")"
                                )
                                .build()
        );
        REGISTRY.registerExactMatch(
                GradientF64Node.class,
                (DotEmitter<GradientF64Node>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label(
                                        "GradientF64\\n" +
                                                "axis=" + node.axis + ",tiling=" + node.tiling + "\\n" +
                                                "coord=[" + node.fromCoord + "," + node.toCoord + ")\\n" +
                                                "value=[" + node.fromValue + "," + node.toValue + ")"
                                )
                                .build()
        );
        REGISTRY.registerExactMatch(
                IntervalSelectF32Node.class,
                (DotEmitter<IntervalSelectF32Node>) (node, context, builder) -> {
                    builder
                            .boxShape()
                            .label("IntervalSelectF32");

                    DotGen.Context.Builder tableBuilder = context.createExtraBuilder();

                    StringBuilder table = new StringBuilder();
                    table.append('<');
                    table.append("<TABLE>");
                    table.append("<TR><TD>idx</TD><TD>thresholds</TD><TD>functions</TD></TR>");

                    AstNode[] functions = node.functions;
                    for (int i = 0, functionsLength = functions.length; i < functionsLength; i++) {
                        table.append("<TR>")
                                .append("<TD>").append(i).append("</TD>")
                                .append("<TD>").append("</TD>");

                        AstNode function = functions[i];
                        int childId = context.generate(function);
                        tableBuilder.edge(childId).label(String.format("children[%d]", i)).finish();
                        table.append("<TD>").append("children.id=").append(DotGen.Context.base26(childId)).append("</TD>");
                        table.append("</TR>");

                        if (i < functionsLength - 1) {
                            table.append("<TR>")
                                    .append("<TD>").append(i).append("</TD>")
                                    .append("<TD>").append(node.thresholds[i]).append("</TD>")
                                    .append("<TD>").append("</TD>");
                            table.append("</TR>");
                        }
                    }

                    table.append("</TABLE>");
                    table.append(">");

                    int tableId = tableBuilder
                            .boxShape()
                            .label(table.toString())
                            .build();

                    builder.edge(tableId).label("IntervalSelectTable").finish();

                    return builder.build();
                }
        );
        REGISTRY.registerExactMatch(
                IntervalSelectF64Node.class,
                (DotEmitter<IntervalSelectF64Node>) (node, context, builder) -> {
                    builder
                            .boxShape()
                            .label("IntervalSelectF64");

                    DotGen.Context.Builder tableBuilder = context.createExtraBuilder();

                    StringBuilder table = new StringBuilder();
                    table.append('<');
                    table.append("<TABLE>");
                    table.append("<TR><TD>idx</TD><TD>thresholds</TD><TD>functions</TD></TR>");

                    AstNode[] functions = node.functions;
                    for (int i = 0, functionsLength = functions.length; i < functionsLength; i++) {
                        table.append("<TR>")
                                .append("<TD>").append(i).append("</TD>")
                                .append("<TD>").append("</TD>");

                        AstNode function = functions[i];
                        int childId = context.generate(function);
                        tableBuilder.edge(childId).label(String.format("children[%d]", i)).finish();
                        table.append("<TD>").append("children.id=").append(DotGen.Context.base26(childId)).append("</TD>");
                        table.append("</TR>");

                        if (i < functionsLength - 1) {
                            table.append("<TR>")
                                    .append("<TD>").append(i).append("</TD>")
                                    .append("<TD>").append(node.thresholds[i]).append("</TD>")
                                    .append("<TD>").append("</TD>");
                            table.append("</TR>");
                        }
                    }

                    table.append("</TABLE>");
                    table.append(">");

                    int tableId = tableBuilder
                            .boxShape()
                            .label(table.toString())
                            .build();

                    builder.edge(tableId).label("IntervalSelectTable").finish();

                    return builder.build();
                }
        );
        REGISTRY.registerExactMatch(
                LerpNode.class,
                (DotEmitter<LerpNode>) (node, context, builder) ->
                        builder
                                .boxShape()
                                .label("Lerp")
                                .edge(context.generate(node.delta)).label("delta").finish()
                                .edge(context.generate(node.start)).label("start").finish()
                                .edge(context.generate(node.end)).label("end").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(Multi2SingleNode.class, (DotEmitter<Multi2SingleNode>) (node, context, builder) ->
                builder
                        .triangleShape()
                        .label("Multi2Single")
                        .edge(context.generate(node.next)).label("next").finish()
                        .build()
        );
        REGISTRY.registerExactMatch(
                RangeChoiceF32Node.class,
                (DotEmitter<RangeChoiceF32Node>) (node, context, builder) ->
                        builder
                                .diamondShape()
                                .label("RangeChoiceF32 [" + node.minInclusive + ", " + node.maxExclusive + ")")
                                .edge(context.generate(node.input)).label("input").color("blue").finish()
                                .edge(context.generate(node.whenInRange)).label("true").finish()
                                .edge(context.generate(node.whenOutOfRange)).label("false").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                RangeChoiceF64Node.class,
                (DotEmitter<RangeChoiceF64Node>) (node, context, builder) ->
                        builder
                                .diamondShape()
                                .label("RangeChoiceF64 [" + node.minInclusive + ", " + node.maxExclusive + ")")
                                .edge(context.generate(node.input)).label("input").color("blue").finish()
                                .edge(context.generate(node.whenInRange)).label("true").finish()
                                .edge(context.generate(node.whenOutOfRange)).label("false").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                RepositionNode.class,
                (DotEmitter<RepositionNode>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label("Reposition")
                                .edge(context.generate(node.input)).label("input").finish()
                                .edge(context.generate(node.inputX)).label("inputX").finish()
                                .edge(context.generate(node.inputY)).label("inputY").finish()
                                .edge(context.generate(node.inputZ)).label("inputZ").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                RoundingDFNode.class,
                (DotEmitter<RoundingDFNode>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label("RoundingDF, operation=" + node.operation)
                                .edge(context.generate(node.input)).label("input").finish()
                                .edge(context.generate(node.multiple)).label("multiple").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(SplineNormalNode.class, SplineNormalNodeDotEmitter.INSTANCE);
//        REGISTRY.registerExactMatch(SplineAstNode.class, SplineAstNodeDotEmitter.INSTANCE);

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
                GenericFastNoiseF64Node.class,
                (DotEmitter<GenericFastNoiseF64Node>) (node, context, builder) ->
                        builder
                                .hexagonShape()
                                .label("FastNoise")
                                .edge(context.generate(node.inputX)).label("inputX").finish()
                                .edge(context.generate(node.inputY)).label("inputY").finish()
                                .edge(context.generate(node.inputZ)).label("inputZ").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                MixNode.class,
                (DotEmitter<MixNode>) (node, context, builder) ->
                        builder
                                .diamondShape()
                                .label("Mix")
                                .edge(context.generate(node.input)).label("input").color("blue").finish()
                                .edge(context.generate(node.argument1)).label("left").finish()
                                .edge(context.generate(node.argument2)).label("right").finish()
                                .build()
        );
        REGISTRY.registerExactMatch(
                SelectF64Node.class,
                (DotEmitter<SelectF64Node>) (node, context, builder) -> {
                    builder
                            .boxShape()
                            .label("Select");

                    DotGen.Context.Builder tableBuilder = context.createExtraBuilder();

                    StringBuilder table = new StringBuilder();
                    table.append('<');
                    table.append("<TABLE>");
                    table.append("<TR><TD>idx</TD><TD>minima</TD><TD>maxima</TD><TD>functions</TD></TR>");

                    AstNode[] functions = node.functions;
                    for (int i = 0, functionsLength = functions.length; i < functionsLength; i++) {
                        table.append("<TR>")
                                .append("<TD>").append(i).append("</TD>")
                                .append("<TD>").append(i < node.mins.length ? node.mins[i] : "").append("</TD>")
                                .append("<TD>").append(i < node.maxs.length ? node.maxs[i] : "").append("</TD>");

                        AstNode function = functions[i];
                        int childId = context.generate(function);
                        tableBuilder.edge(childId).label(String.format("children[%d]", i)).finish();
                        table.append("<TD>").append("children.id=").append(DotGen.Context.base26(childId)).append("</TD>");
                        table.append("</TR>");
                    }

                    table.append("</TABLE>");
                    table.append(">");

                    int tableId = tableBuilder
                            .boxShape()
                            .label(table.toString())
                            .build();

                    builder.edge(tableId).label("SelectTable").finish();

                    return builder.build();
                }
        );
    }

    public static <T extends AstNode> int doDotGen(T node, DotGen.Context context, DotGen.Context.Builder builder) {
        DotEmitter<T> emitter = (DotEmitter<T>) REGISTRY.get(node.getClass());
        return emitter.doDotGen(node, context, builder);
    }

}
