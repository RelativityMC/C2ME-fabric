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

package com.ishland.c2me.opts.dfc.common.gen.dot.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGen;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.List;

public class SplineAstNodeDotEmitter implements DotEmitter<SplineAstNode> {

    public static final SplineAstNodeDotEmitter INSTANCE = new SplineAstNodeDotEmitter();

    private SplineAstNodeDotEmitter() {
    }

    @Override
    public int doDotGen(SplineAstNode node, DotGen.Context context, DotGen.Context.Builder builder) {
        return builder.ovalShape()
                .label("Spline entry")
                .edge(doDotGenSpline(node, context, node.spline)).label("spline").finish()
                .build();
    }

    private static int doDotGenSpline(SplineAstNode node, DotGen.Context context, Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
        if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> a1) {
            return context.generate(new ConstantNode(a1.value()));
        } else if (spline instanceof Spline.Implementation<DensityFunctionTypes.Spline.DensityFunctionWrapper> a1) {
            DotGen.Context.Builder builder = context.getSplineBuilder(spline);
            if (builder.isFrozen()) {
                return builder.getId();
            }

            builder
                    .hexagonShape()
                    .label("Spline")
//                    .label(String.format("Spline\\nderivatives=%s\\nlocations=%s", Arrays.toString(a1.derivatives()), Arrays.toString(a1.locations())))
                    .edge(context.generate(node.children.get(a1.locationFunction()))).label("locationFunction").finish();

            DotGen.Context.Builder tableBuilder = context.createExtraBuilder();

            StringBuilder table = new StringBuilder();
            table.append('<');
            table.append("<TABLE>");
            table.append("<TR><TD>idx</TD><TD>derivatives</TD><TD>locations</TD><TD>values</TD></TR>");

            List<Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper>> values = a1.values();
            for (int i = 0, valuesSize = values.size(); i < valuesSize; i++) {
                Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> child = values.get(i);
                table.append("<TR>")
                        .append("<TD>").append(i).append("</TD>")
                        .append("<TD>").append(a1.derivatives()[i]).append("</TD>")
                        .append("<TD>").append(a1.locations()[i]).append("</TD>");

                if (child instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> fixedFloatFunction) {
                    table.append("<TD>").append(fixedFloatFunction.value()).append("</TD>");
                } else {
                    int childId = doDotGenSpline(node, context, child);
                    tableBuilder.edge(childId).label(String.format("children[%d]", i)).finish();
                    table.append("<TD>").append("children.id=").append(DotGen.Context.base26(childId)).append("</TD>");
                }

                table.append("</TR>");
            }

            table.append("</TABLE>");
            table.append(">");

            int tableId = tableBuilder
                    .boxShape()
                    .label(table.toString())
                    .build();

            builder.edge(tableId).label("SplineTable").finish();

            return builder.build();
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported spline implementation: %s", spline.getClass().getName()));
        }
    }

}
