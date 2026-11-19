/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

    private static int doDotGenSpline(SplineAstNode node, DotGen.Context context, Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
        if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> a1) {
            return context.generate(new ConstantNode(a1.value()));
        } else if (spline instanceof Spline.Implementation<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> a1) {
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

            List<Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper>> values = a1.values();
            for (int i = 0, valuesSize = values.size(); i < valuesSize; i++) {
                Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> child = values.get(i);
                table.append("<TR>")
                        .append("<TD>").append(i).append("</TD>")
                        .append("<TD>").append(a1.derivatives()[i]).append("</TD>")
                        .append("<TD>").append(a1.locations()[i]).append("</TD>");

                if (child instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> fixedFloatFunction) {
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
